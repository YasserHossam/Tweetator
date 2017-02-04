package com.yasser.tweetator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import TwitterHelper.App;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.net.URI;
import java.security.Signature;

public class StartPage extends Activity implements View.OnClickListener
{
    /**Twitter instance*/
    private Twitter tweetator;
    /**request token for accessing user account*/
    private RequestToken tweetatorRequestToken;
    /**shared preferences to store user details*/
    private SharedPreferences tweetatorPrefs;
    //for error logging
    private String LOG_TAG = "TwitNiceActivity";//alter for your Activity namepublic final static String
    public final static String TWIT_KEY = "GStTnDEoLeokkNBEj3GQ3d4M5";
    /**developer secret for the app*/
    public final static String TWIT_SECRET = "xifPgBhM2IUiqsIq45zvpqeanltUmfBiuEJZW7UietraxX8OoR";
    /**app url*/
    /**app url*/
    public final static String TWIT_URL = "oauth2://t4jsample";
    boolean isSignedIn=true;
    String userName;
    String password;
    Button signInButton;
    TextView textView;
    ProgressBar loadingBar;
    String oaVerifier;
    int mId=1;
    Context currentActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //get the preferences for the app
        App app=new App();
        app.setContext(this);
        currentActivity=app.getContext();

        tweetatorPrefs = getSharedPreferences("TweetatorPrefs", 0);

//find out if the user preferences are set
        if(tweetatorPrefs.getString("user_token", null)==null) {

            //no user preferences so prompt to sign in
            setContentView(R.layout.activity_start_page);
            signInButton=(Button)findViewById(R.id.signInButton);
            loadingBar=(ProgressBar)findViewById(R.id.loadingBar);
            signInButton.setOnClickListener(this);

        }
        else
        {
            Intent intent=new Intent(StartPage.this,TimelineActivity.class);
            startActivity(intent);
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
    protected void onDestroy() {
        Log.i("asd", "onDestroy()");
        super.onDestroy();
    }
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        //get the retrieved data
        Uri twitURI = intent.getData();
        //make sure the url is correct
        if(twitURI!=null && twitURI.toString().startsWith(TWIT_URL))
        {
            //is verifcation - get the returned data
             oaVerifier = twitURI.getQueryParameter("oauth_verifier");
            new AsyncTask<Void, Void, Void>(){
                int done=1;

                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();
                    loadingBar.setVisibility(View.VISIBLE);
                    signInButton.getBackground().setAlpha(64);
                    signInButton.setClickable(false);
                    signInButton.setAlpha(0.25f);
                }

                @Override
                protected Void doInBackground(Void... params)
                {
                    try
                    {
                        //try to get an access token using the returned data from the verification page

                        AccessToken accToken = tweetator.getOAuthAccessToken(tweetatorRequestToken, oaVerifier);

                        //add the token and secret to shared prefs for future reference
                        tweetatorPrefs.edit()
                                .putString("user_token", accToken.getToken())
                                .putString("user_secret", accToken.getTokenSecret())
                                .commit();


                    }
                    catch (TwitterException te)
                    {
                        Log.e(LOG_TAG, "Failed to get access token: " + te.getMessage());
                        done=0;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid)
                {

                    Intent intent=new Intent(StartPage.this, TimelineActivity.class);
                    if(done==1)
                        startActivity(intent);
                    else
                    {
                        loadingBar.setVisibility(View.INVISIBLE);
                        signInButton.getBackground().setAlpha(255);
                        signInButton.setClickable(true);
                        signInButton.setAlpha(0.1f);
                        Toast.makeText(currentActivity,"Something went wrong, please try again later",Toast.LENGTH_LONG).show();
                    }
                    super.onPostExecute(aVoid);
                }
            }.execute();

        }
    }

    @Override
    public void onClick(View v)
    {
        tweetator=new TwitterFactory().getInstance();
        tweetator.setOAuthConsumer(TWIT_KEY, TWIT_SECRET);
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                loadingBar.setVisibility(View.VISIBLE);
                signInButton.getBackground().setAlpha(64);
                signInButton.setAlpha(0.25f);
                signInButton.setClickable(false);
            }

            @Override
            protected String doInBackground(Void... params)
            {

                String authURL=null;
                try
                {   //get authentication request token

                    tweetatorRequestToken = tweetator.getOAuthRequestToken(TWIT_URL);
                    authURL = tweetatorRequestToken.getAuthenticationURL();


                }
                catch(Exception te)
                {
                    isSignedIn=false;
                    Log.e(LOG_TAG, "TE " + te.getMessage());

                }
                return authURL;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(s!=null)
                {
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else
                {
                    loadingBar.setVisibility(View.INVISIBLE);
                    signInButton.getBackground().setAlpha(255);
                    signInButton.setClickable(true);
                    signInButton.setAlpha(1.0f);
                    Toast.makeText(currentActivity,"Something went wrong, please try again later",Toast.LENGTH_LONG).show();
                }
            }
        }.execute();

    }
}
