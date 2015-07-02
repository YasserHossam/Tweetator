package com.yasser.tweetator;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import TwitterHelper.App;
import TwitterHelper.TweetStatus;
import TwitterHelper.TweetStatusAdapter;
import twitter4j.AccountSettings;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by yasser on 6/28/2015.
 */


public class TimelineActivity extends FragmentActivity implements View.OnClickListener
{
    private Twitter tweetator;
    /**request token for accessing user account*/
    private RequestToken tweetatorRequestToken;
    /**shared preferences to store user details*/
    private SharedPreferences tweetatorPrefs;
    //for error logging
    private String LOG_TAG = "TwitNiceActivity";//alter for your Activity name
    public final static String TWIT_KEY = "GStTnDEoLeokkNBEj3GQ3d4M5";
    /**developer secret for the app*/
    public final static String TWIT_SECRET = "xifPgBhM2IUiqsIq45zvpqeanltUmfBiuEJZW7UietraxX8OoR";
    /**app url*/
    public final static String TWIT_URL = "oauth2://t4jsample";
    ListView tweetsListView;
    TweetStatusAdapter adapter;
    ArrayList<TweetStatus> tweets;
    ProgressBar loadingBar;
    AsyncTask updateTimeline;
    Context currentActivity;
    Button newTweetButton;
    EditText newTweetEditText;
    String tweetToSend;
    Button moreTweets;
    View.OnClickListener listener;
    long tweetID,currentUserRetweetId;
    boolean tweetState;
    String userName;
    boolean isFavouritedOrIsRetweeted;
    Paging tweetsPage;
    OptionMenuFragment menuFragment;
    ImageButton signoutButton;
    int pageNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        App app=new App();
        app.setContext(this);
        currentActivity=app.getContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_layout);
        tweetsPage=new Paging();
        newTweetEditText=(EditText)findViewById(R.id.newTweetEditText);
        newTweetButton=(Button)findViewById(R.id.newTweetButton);
        newTweetButton.setOnClickListener(this);
        newTweetButton.setTag("newTweet");
        loadingBar=(ProgressBar)findViewById(R.id.loadingBar);
        tweetsListView=(ListView)findViewById(R.id.tweetsListView);
        View footerView = ((LayoutInflater) currentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.timeline_listview_footer, null, false);
        menuFragment=(OptionMenuFragment)getFragmentManager().findFragmentById(R.id.menuFragment);
        View optionMenuFragment=menuFragment.getView();
        signoutButton=(ImageButton)optionMenuFragment.findViewById(R.id.signoutButton);
        signoutButton.setOnClickListener(this);
        signoutButton.setTag("signout");
        moreTweets=(Button)footerView.findViewById(R.id.moreTweets);
        moreTweets.setTag("moreTweets");
        moreTweets.setOnClickListener(this);
        tweetsListView.addFooterView(footerView);
        tweetatorPrefs=getSharedPreferences("TweetatorPrefs",0);
        String userToken = tweetatorPrefs.getString("user_token", null);
        String userSecret = tweetatorPrefs.getString("user_secret", null);
        tweets=new ArrayList<TweetStatus>();
        //create new configuration
        Configuration twitConf = new ConfigurationBuilder()
                .setOAuthConsumerKey(TWIT_KEY)
                .setOAuthConsumerSecret(TWIT_SECRET)
                .setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret)
                .build();
        //instantiate new twitter
        tweetator= new TwitterFactory(twitConf).getInstance();
        updateTimeline= new AsyncTask<Void,Void,Void>()
        {
            @Override
            protected void onPreExecute()
            {
                loadingBar.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                try
                {
                    pageNumber=1;
                    tweetsPage.setPage(1);
                    tweetsPage.setCount(20);
                    List<twitter4j.Status> homeTimeline = tweetator.getHomeTimeline(tweetsPage);
                    for(int i=0 ;i<homeTimeline.size();i++)
                    {
                        TweetStatus ts=new TweetStatus(homeTimeline.get(i));
                        URL url = new URL(ts.profilePictureURL);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        ts.setProfilePicture(myBitmap);
                        tweets.add(ts);
                    }
                }
                catch (Exception exc)
                {
                    Log.e(LOG_TAG,"Exception "+ exc);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                loadingBar.setVisibility(View.INVISIBLE);
                adapter=new TweetStatusAdapter(currentActivity,R.layout.onetweet_layout,tweets);
                tweetsListView.setAdapter(adapter);
               super.onPostExecute(aVoid);
            }
        }.execute();
        /*
        TweetStatus status_data[] = new TweetStatus[]
                {
                        new TweetStatus(R.drawable.ic_launcher,"im really happy today cause i am prince","yasserHossam","4h"),
                        new TweetStatus(R.drawable.ic_launcher,"im really happy today cause i am queen","mernaKhalil","4h"),
                        new TweetStatus(R.drawable.ic_launcher,"im really happy today cause i am king","YosThePrince","4h"),
                        new TweetStatus(R.drawable.ic_launcher,"im really happy today cause i am mother of pride","???? ???????","4h"),
                        new TweetStatus(R.drawable.ic_launcher,"im really happy today cause i am yasser","yasserNumber2","4h"),
                };
        TweetStatusAdapter adapter =new TweetStatusAdapter(this,R.layout.onetweet_layout,status_data);

        tweetsListView.setAdapter(adapter);*/
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        String buttonType=intent.getStringExtra("buttonType");
        userName=(String)intent.getStringExtra("userName");
        tweetID=(long)intent.getLongExtra("tweetID", 1);
        currentUserRetweetId=(long)intent.getLongExtra("curretUserRetweetId",0);
        isFavouritedOrIsRetweeted=intent.getBooleanExtra("isFavouritedOrIsRetweeted",false);
        switch (buttonType)
        {
            case "favourite":
                new AsyncTask<Void,Void,Void>()
                {
                    @Override
                    protected void onPreExecute()
                    {
                        loadingBar.setVisibility(View.VISIBLE);
                        super.onPreExecute();
                    }
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        try
                        {
                            if(!isFavouritedOrIsRetweeted)
                            {
                                twitter4j.Status status = tweetator.createFavorite(tweetID);
                            }
                            else
                            {
                                twitter4j.Status status = tweetator.destroyFavorite(tweetID);
                            }
                        }
                        catch (TwitterException exc)
                        {

                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid)
                    {
                        loadingBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(currentActivity, "Done", Toast.LENGTH_SHORT).show();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
                break;
            case "reply"    :
                newTweetEditText.setText("@"+userName);
                newTweetEditText.clearFocus();
                if(newTweetEditText.requestFocus())
                {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
                break;
            case "retweet"  :
                new AsyncTask<Void,Void,Void>()
                {
                    @Override
                    protected void onPreExecute()
                    {
                        loadingBar.setVisibility(View.VISIBLE);
                        super.onPreExecute();
                    }
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        try
                        {
                            if(!isFavouritedOrIsRetweeted)
                            {
                                tweetator.retweetStatus(tweetID);
                            }
                            else
                            {
                               tweetator.destroyStatus(currentUserRetweetId);
                            }
                        }
                        catch (TwitterException exc)
                        {

                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid)
                    {
                        loadingBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(currentActivity,"Done",Toast.LENGTH_SHORT).show();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent2 = new Intent(Intent.ACTION_MAIN);
        intent2.addCategory(Intent.CATEGORY_HOME);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent2);
    }

    @Override
    public void onClick(View v)
    {
        String s=v.getTag().toString();
        switch (s)
        {
            case "signout":
                tweetatorPrefs.edit().clear().commit();
                Intent intent=new Intent(TimelineActivity.this,StartPage.class);
                startActivity(intent);
                break;
            case "moreTweets":
                updateTimeline= new AsyncTask<Void,Void,Void>()
                {
                    @Override
                    protected void onPreExecute()
                    {
                        loadingBar.setVisibility(View.VISIBLE);
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        try
                        {
                            pageNumber++;
                            tweetsPage.setPage(pageNumber);
                            tweetsPage.setCount(20);
                            List<twitter4j.Status> homeTimeline = tweetator.getHomeTimeline(tweetsPage);
                            for(int i=0 ;i<homeTimeline.size();i++)
                            {
                                TweetStatus ts=new TweetStatus(homeTimeline.get(i));
                                URL url = new URL(ts.profilePictureURL);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                                ts.setProfilePicture(myBitmap);
                                tweets.add(ts);
                            }
                        }
                        catch (Exception exc)
                        {
                            Log.e(LOG_TAG,"Exception "+ exc);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid)
                    {
                        loadingBar.setVisibility(View.INVISIBLE);
                        adapter=new TweetStatusAdapter(currentActivity,R.layout.onetweet_layout,tweets);
                        tweetsListView.setAdapter(adapter);
                        super.onPostExecute(aVoid);
                    }
                }.execute();
                break;
            case "newTweet":
                            tweetToSend=newTweetEditText.getText().toString();
                            if(tweetToSend.length()>140)
                                Toast.makeText(this,"Too many characters , Can't send tweet",Toast.LENGTH_SHORT);
                            else if(tweetToSend.length()>0)
                            {
                                new AsyncTask<Void, Void, Void>()
                                {
                                    @Override
                                    protected void onPreExecute()
                                    {
                                        loadingBar.setVisibility(View.VISIBLE);
                                        super.onPreExecute();
                                    }

                                    @Override
                                    protected Void doInBackground(Void... params)
                                    {
                                        try
                                        {
                                            tweetator.updateStatus(tweetToSend);
                                        } catch (TwitterException e)
                                        {
                                            Log.e(LOG_TAG, "TE " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid)
                                    {
                                        new AsyncTask<Void,Void,Void>()
                                        {
                                            @Override
                                            protected void onPreExecute()
                                            {
                                                loadingBar.setVisibility(View.VISIBLE);
                                                super.onPreExecute();
                                            }

                                            @Override
                                            protected Void doInBackground(Void... params)
                                            {
                                                try
                                                {
                                                    pageNumber=1;
                                                    tweetsPage.setPage(1);
                                                    tweetsPage.setCount(20);
                                                    List<twitter4j.Status> homeTimeline = tweetator.getHomeTimeline(tweetsPage);
                                                    tweets=new ArrayList<TweetStatus>();
                                                    for(int i=0 ;i<homeTimeline.size();i++)
                                                    {
                                                        TweetStatus ts=new TweetStatus(homeTimeline.get(i));
                                                        URL url = new URL(ts.profilePictureURL);
                                                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                                        connection.setDoInput(true);
                                                        connection.connect();
                                                        InputStream input = connection.getInputStream();
                                                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                                                        ts.setProfilePicture(myBitmap);
                                                        tweets.add(ts);
                                                    }
                                                }
                                                catch (Exception exc)
                                                {
                                                    Log.e(LOG_TAG,"Exception "+ exc);
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Void aVoid)
                                            {

                                                loadingBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(getApplicationContext(), "Tweet is sent Successfully", Toast.LENGTH_SHORT).show();
                                                newTweetEditText.setText("");
                                                adapter = new TweetStatusAdapter(currentActivity,R.layout.onetweet_layout,tweets);
                                                adapter.notifyDataSetChanged();
                                                tweetsListView.setAdapter(adapter);
                                                super.onPostExecute(aVoid);
                                            }
                                        }.execute();


                                    }
                                }.execute();
                                break;
                            }
        }
    }
}
