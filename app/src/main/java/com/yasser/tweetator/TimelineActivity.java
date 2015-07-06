package com.yasser.tweetator;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import TwitterHelper.App;
import TwitterHelper.TweetStatus;
import TwitterHelper.TweetStatusAdapter;
import TwitterHelper.UserInfo;
import twitter4j.AccountSettings;
import twitter4j.Paging;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
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
    TweetStatus tweetInAction,profileClicked;
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
    ImageButton signoutButton,refreshButton;
    int pageNumber;
    User clickedUser;
    String tweetOrReply;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_start_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        App app=new App();
        tweetOrReply="tweet";
        app.setContext(this);
        currentActivity=app.getContext();
      /*  android.app.ActionBar actionBar=getActionBar();
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle=(TextView)findViewById(titleId);
        actionBarTitle.setTextColor(Color.WHITE);*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_layout);
        refreshButton=(ImageButton)findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(this);
        refreshButton.setTag("refreshTimeline");
        tweetsPage=new Paging();
        newTweetEditText=(EditText)findViewById(R.id.newTweetEditText);

        newTweetButton=(Button)findViewById(R.id.newTweetButton);
        newTweetButton.setOnClickListener(this);
        newTweetButton.setTag("newTweet");
        loadingBar=(ProgressBar)findViewById(R.id.loadingBar);
        tweetsListView=(ListView)findViewById(R.id.tweetsListView);
        View footerView = ((LayoutInflater) currentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.timeline_listview_footer, null, false);
       /* menuFragment=(OptionMenuFragment)getFragmentManager().findFragmentById(R.id.menuFragment);
        View optionMenuFragment=menuFragment.getView();
        signoutButton=(ImageButton)optionMenuFragment.findViewById(R.id.signoutButton);
        signoutButton.setOnClickListener(this);
        signoutButton.setTag("signout");*/
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
        if(intent.hasExtra("buttonType"))
        {
            tweetInAction = new TweetStatus();
            super.onNewIntent(intent);
            String buttonType = intent.getStringExtra("buttonType");
            userName = (String) intent.getStringExtra("userName");
            tweetID = (long) intent.getLongExtra("tweetID", 1);
            currentUserRetweetId = (long) intent.getLongExtra("curretUserRetweetId", 0);
            isFavouritedOrIsRetweeted = intent.getBooleanExtra("isFavouritedOrIsRetweeted", false);
            for (int i = 0; i < tweets.size(); i++) {
                if (tweetID == tweets.get(i).tweetID) {
                    tweetInAction = tweets.get(i);
                    tweetInAction.index = i;
                }
            }

            switch (buttonType) {
                case "favourite":
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected void onPreExecute() {
                            loadingBar.setVisibility(View.VISIBLE);
                            super.onPreExecute();
                        }

                        @Override
                        protected String doInBackground(Void... params) {
                            String actionType = "";
                            try {
                                if (!isFavouritedOrIsRetweeted) {
                                    actionType = "favourite";
                                    twitter4j.Status status = tweetator.createFavorite(tweetID);
                                    actionType += "&done";
                                } else {
                                    actionType = "unFavourite";
                                    twitter4j.Status status = tweetator.destroyFavorite(tweetID);
                                    actionType += "&done";
                                }
                            } catch (TwitterException exc) {
                                actionType += "&failed";
                            }
                            return actionType;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            String[] state = result.split("&");
                            loadingBar.setVisibility(View.INVISIBLE);
                            if (state[1].equals("failed")) {
                                if (state[0].equals("unFavourite")) {
                                    Toast.makeText(currentActivity, "Can't do Action now, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                    tweetInAction.isFavourited = true;
                                } else if (state[0].equals("favourite")) {
                                    Toast.makeText(currentActivity, "Can't do Action now, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                    tweetInAction.isFavourited = false;
                                }
                                tweets.set(tweetInAction.index, tweetInAction);
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(currentActivity, "Done", Toast.LENGTH_SHORT).show();
                            }
                            super.onPostExecute(result);
                        }
                    }.execute();
                    break;
                case "reply":
                    tweetOrReply="reply";
                    String reply="@"+userName+" ";
                    newTweetEditText.setText(reply);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(newTweetEditText, InputMethodManager.SHOW_IMPLICIT);
                    newTweetEditText.setSelection(reply.length());
                    break;
                case "retweet":

                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected void onPreExecute() {
                            loadingBar.setVisibility(View.VISIBLE);
                            super.onPreExecute();
                        }

                        @Override
                        protected String doInBackground(Void... params) {
                            String actionType = null;
                            try {
                                if (!isFavouritedOrIsRetweeted) {
                                    actionType = "retweet";
                                    twitter4j.Status s = tweetator.retweetStatus(tweetID);
                                    s=tweetator.showStatus(tweetID);
                                    TweetStatus ts=new TweetStatus(s);
                                    for(int i=0;i<tweets.size();i++)
                                    {
                                        if(tweets.get(i).tweetID==tweetID)
                                        {
                                            ts=tweets.get(i);
                                            ts.isRetweeted=s.isRetweeted();
                                            ts.currentUserRetweetId=s.getCurrentUserRetweetId();
                                            tweets.set(i,ts);
                                        }
                                    }
                                    actionType += "&done";
                                } else {
                                    actionType = "unRetweet";
                                    tweetator.destroyStatus(currentUserRetweetId);
                                    twitter4j.Status s=tweetator.showStatus(tweetID);
                                    TweetStatus ts=new TweetStatus(s);
                                    for(int i=0;i<tweets.size();i++)
                                    {
                                        if(tweets.get(i).tweetID==tweetID)
                                        {
                                            ts=tweets.get(i);
                                            ts.isRetweeted=s.isRetweeted();
                                            ts.currentUserRetweetId=s.getCurrentUserRetweetId();
                                            tweets.set(i,ts);
                                        }
                                    }
                                    actionType += "&done";
                                }
                            } catch (TwitterException exc) {
                                actionType += "&failed";
                            }
                            return actionType;
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            String[] state = result.split("&");
                            loadingBar.setVisibility(View.INVISIBLE);
                            if (state[1].equals("failed")) {
                                if (state[0].equals("unRetweet")) {
                                    Toast.makeText(currentActivity, "Can't do Action now, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                    tweetInAction.isRetweeted = true;
                                } else if (state[0].equals("retweet")) {
                                    Toast.makeText(currentActivity, "Can't do Action now, Please Try Again Later", Toast.LENGTH_SHORT).show();
                                    tweetInAction.isRetweeted = false;
                                }
                                tweets.set(tweetInAction.index, tweetInAction);
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(currentActivity, "Done", Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                            }
                            super.onPostExecute(result);
                        }
                    }.execute();
                    break;
            }
        }
        else if(intent.hasExtra("profileClicked"))
        {
            profileClicked=(TweetStatus)intent.getParcelableExtra("profileClicked");
            new AsyncTask<Void,Void, UserInfo>()
            {
                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();
                    loadingBar.setVisibility(View.VISIBLE);

                }

                @Override
                protected UserInfo doInBackground(Void... params)
                {
                    UserInfo currentUserInfo=new UserInfo();
                    String errorType="getUserError";
                    try
                    {
                        clickedUser=tweetator.showUser(profileClicked.userName);
                        errorType="getProfilePictureError";
                        currentUserInfo=new UserInfo(clickedUser);
                        URL url = new URL(clickedUser.getOriginalProfileImageURL());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        currentUserInfo.setProfilePicture(myBitmap);
                        errorType="getHeaderError";
                        url = new URL(clickedUser.getProfileBannerMobileURL());
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        input = connection.getInputStream();
                        myBitmap = BitmapFactory.decodeStream(input);
                        currentUserInfo.setHeader(myBitmap);
                    }
                    catch (Exception exc)
                    {
                        switch (errorType)
                        {

                            case "getProfilePictureError":
                                try
                                {
                                    currentUserInfo.setProfilePicture(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_profile_picture));
                                }
                                catch (Exception ex)
                                {

                                }
                                break;
                            case "getHeaderError":
                                try
                                {
                                    currentUserInfo.setHeader(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_header));
                                }
                                catch (Exception e)
                                {

                                }
                                break;
                        }
                    }
                    return currentUserInfo;
                }
                @Override
                protected void onPostExecute(UserInfo ui)
                {
                    super.onPostExecute(ui);
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.animation_slide_in_top, R.anim.animation_slide_out_bottom,R.anim.animation_slide_in_left,R.anim.animation_slide_out_right);
                    ProfileFragment hello = new ProfileFragment();
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("userInfo",ui);
                    hello.setArguments(bundle);
                    fragmentTransaction.add(R.id.listViewLayout, hello, "HELLO");
                    fragmentTransaction.addToBackStack("HELLO");
                    fragmentTransaction.commit();
                    loadingBar.setVisibility(View.INVISIBLE);
                }

            }.execute();

        }
    }

    @Override
    public void onBackPressed()
    {
       //super.onBackPressed();
        if(getFragmentManager().getBackStackEntryCount()>0)
        {
            ExitProifle();
        }
        else
        {
            Intent intent2 = new Intent(Intent.ACTION_MAIN);
            intent2.addCategory(Intent.CATEGORY_HOME);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent2);
        }
    }
    public void ExitProifle()
    {
        getFragmentManager().popBackStack();
    }
    @Override
    public void onClick(View v)
    {
        if(getFragmentManager().getBackStackEntryCount()>0)
        {
            ExitProifle();
        }
        String s=v.getTag().toString();
        switch (s)
        {
            case "refreshTimeline":
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
                            Paging refreshPage=new Paging();
                            refreshPage.setSinceId(tweets.get(0).tweetID);
                            List<twitter4j.Status> homeTimeline = tweetator.getHomeTimeline(refreshPage);
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
                                boolean isDuplicated=false;
                                for(int k=0;k<tweets.size();k++)
                                {
                                    if(tweets.get(k).tweetID==ts.tweetID)
                                        isDuplicated=true;
                                }
                                if(!isDuplicated)
                                tweets.add(i,ts);
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
                        adapter.notifyDataSetChanged();
                        super.onPostExecute(aVoid);
                    }
                }.execute();
                /*FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
               fragmentTransaction.setCustomAnimations(R.anim.animation_fade_in, R.anim.animation_fade_out);
                ProfileFragment hello = new ProfileFragment();
                fragmentTransaction.replace(R.id.profileFragment, hello, "HELLO");
                fragmentTransaction.commit();*/
                break;
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
                        adapter.notifyDataSetChanged();
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
                                            if(tweetOrReply.equals("tweet")) {
                                                tweetator.updateStatus(tweetToSend);
                                            }
                                            else
                                            {
                                                tweetator.updateStatus(new StatusUpdate(tweetToSend).inReplyToStatusId(tweetID));
                                            }
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
                                                    tweetsPage.setCount(1);
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
                                                        tweets.add(0, ts);
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
                                                adapter.notifyDataSetChanged();
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
