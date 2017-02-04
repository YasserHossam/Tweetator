package TwitterHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.yasser.tweetator.R;
import com.yasser.tweetator.TimelineActivity;

import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yasser on 6/28/2015.
 */
public class TweetStatusAdapter extends ArrayAdapter<TweetStatus>
{
    private static final String LOG_TAG ="ay7aga" ;
    Context context;
    int resource;
    List<TweetStatus> data=null;
    public TweetStatusAdapter(Context context, int resource, ArrayList<TweetStatus> data)
    {
        super(context, resource, data);
        this.context=context;
        this.resource=resource;
        this.data=data;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row=convertView;
        TweetStatusHolder holder=null;
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(resource, parent, false);

            holder = new TweetStatusHolder();
            holder.showMediaButton=(ImageButton)row.findViewById(R.id.showMediaButton);
            holder.profilePicture = (ImageView)row.findViewById(R.id.profiePictureImageView);
            holder.status = (TextView)row.findViewById(R.id.statusTextView);
            holder.userName = (TextView)row.findViewById(R.id.userNameTextView);
            holder.time = (TextView)row.findViewById(R.id.timeTextView);
            holder.reply=(ImageButton)row.findViewById(R.id.replyButton);
            holder.retweet=(ImageButton)row.findViewById(R.id.retweetButton);
            holder.favourite=(ImageButton)row.findViewById(R.id.favouriteButton);
            holder.retweetState=(TextView)row.findViewById(R.id.retweetState);
            holder.twitterName=(TextView)row.findViewById(R.id.twitterNameTextView);
            holder.attachedPhoto=(ImageView)row.findViewById(R.id.attachedPhoto);
            row.setTag(holder);
        }
        else
        {
            holder = (TweetStatusHolder)row.getTag();
        }
        TweetStatus tweetStatus = data.get(position);
        holder.attachedPhoto.setTag(tweetStatus);
        holder.profilePicture.setImageBitmap(tweetStatus.getProfilePicture());
        holder.profilePicture.setTag(tweetStatus);
        holder.profilePicture.setOnClickListener(this.profilePictureListener);
        holder.status.setText(tweetStatus.status);
        holder.userName.setText(tweetStatus.userName);
        holder.time.setText(tweetStatus.time);
        if(tweetStatus.hasPhotoAttached)
        {
            if(tweetStatus.isPhotoAttachedLoaded)
            {
                holder.showMediaButton.setVisibility(View.GONE);
                holder.attachedPhoto.setVisibility(View.VISIBLE);
                holder.attachedPhoto.setImageBitmap(tweetStatus.getPhotoAttached());
            }
            else
            {
                holder.showMediaButton.setVisibility(View.VISIBLE);
                holder.showMediaButton.setOnClickListener(mediaButtonListener);
                holder.showMediaButton.setTag(holder.attachedPhoto);
                holder.attachedPhoto.setVisibility(View.GONE);
            }
        }
        else
        {
            holder.showMediaButton.setVisibility(View.GONE);
            holder.attachedPhoto.setVisibility(View.GONE);
        }
        if(tweetStatus.isRetweet)
        {
            holder.retweetState.setVisibility(View.VISIBLE);
            holder.retweetState.setText(tweetStatus.retweetUserName+" retweeted");
        }
        else
        {
            holder.retweetState.setVisibility(View.GONE);
        }
        holder.twitterName.setText("@"+tweetStatus.twitterName);
        if(tweetStatus.isFavourited)
            holder.favourite.setImageResource(R.drawable.favorite_on);
        else
            holder.favourite.setImageResource(R.drawable.favorite_hover);
        if(tweetStatus.isRetweeted)
            holder.retweet.setImageResource(R.drawable.retweet_on);
        else
            holder.retweet.setImageResource(R.drawable.retweet_hover);
        holder.favourite.setOnClickListener(tweetListener);
        holder.favourite.setTag("favourite%" + tweetStatus.tweetID + "%" + tweetStatus.isFavourited);
        holder.reply.setOnClickListener(tweetListener);
        holder.reply.setTag("reply%" + tweetStatus.tweetID + "%" + tweetStatus.twitterName);
        holder.retweet.setOnClickListener(tweetListener);
        holder.retweet.setTag("retweet%"+tweetStatus.tweetID+"%"+tweetStatus.isRetweeted+"%"+tweetStatus.currentUserRetweetId);
        return row;
    }
    static class TweetStatusHolder
    {
        ImageView profilePicture,attachedPhoto;
        TextView status,userName,time,twitterName,retweetState;
        ImageButton favourite,reply,retweet,showMediaButton;
    }
    private View.OnClickListener mediaButtonListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            ((TimelineActivity)context).loadAttachedPhoto(v);
        }
    };
    private View.OnClickListener profilePictureListener=new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            TweetStatus profileClicked=(TweetStatus)v.getTag();
            ((TimelineActivity)context).onProifleClicked(profileClicked);
        }
    };
    private View.OnClickListener tweetListener= new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int tweetInAction=0;

            String[] tags;
            String userName=null;
            boolean isFavouritedOrRetweeted=false;
            ImageButton clickedButton=(ImageButton)v;
            tags=((String)(v.getTag())).split("%");
            String buttonType=tags[0];
            long tweetID=Long.valueOf(tags[1]).longValue(),currentUserRetweetId=0;
            for(int i=0;i<data.size();i++)
            {
                if(tweetID==data.get(i).tweetID) {
                    tweetInAction = i;
                    break;
                }
            }
            if(buttonType.equals("reply"))
            {
                userName=tags[2];
            }
            else if(buttonType.equals("favourite")||buttonType.equals("retweet"))
            {
                isFavouritedOrRetweeted=Boolean.valueOf(tags[2]);
            }
            if(buttonType.equals("favourite"))
            {
                if(!isFavouritedOrRetweeted)
                {
                    clickedButton.setImageResource(R.drawable.favorite_on);
                    data.get(tweetInAction).isFavourited=true;
                    v.setTag("favourite%" + tweetID + "%" + true);
                }
                else
                {
                    clickedButton.setImageResource(R.drawable.favorite_hover);
                    data.get(tweetInAction).isFavourited=false;
                    v.setTag("favourite%" + tweetID + "%" + false);
                }
            }
            if(buttonType.equals("retweet"))
            {
                currentUserRetweetId=Long.valueOf(tags[3]);
                if(!isFavouritedOrRetweeted)
                {
                    clickedButton.setImageResource(R.drawable.retweet_on);
                    data.get(tweetInAction).isRetweeted=true;
                    v.setTag("retweet%" + tweetID + "%" + true + "%"+currentUserRetweetId);
                }
                else
                {
                    clickedButton.setImageResource(R.drawable.retweet_hover);
                    data.get(tweetInAction).isRetweeted=false;
                    v.setTag("retweet%" + tweetID + "%" + false+"%"+currentUserRetweetId);
                }
            }
            if(buttonType.equals("reply"))
            {
                ((TimelineActivity)context).onReplyClicked(tweetID,userName);
            }
            else if(buttonType.equals("favourite"))
            {
                ((TimelineActivity)context).onFavouriteClicked(tweetID,isFavouritedOrRetweeted);
            }
            else if(buttonType.equals("retweet"))
            {
                ((TimelineActivity)context).onRetweetClicked(tweetID,currentUserRetweetId,isFavouritedOrRetweeted);
            }
            /*Intent intent=new Intent(v.getContext(),TimelineActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("buttonType",buttonType);
            intent.putExtra("tweetID",tweetID);
            intent.putExtra("userName",userName);
            intent.putExtra("isFavouritedOrIsRetweeted",isFavouritedOrRetweeted);
            intent.putExtra("curretUserRetweetId",currentUserRetweetId);
            v.getContext().startActivity(intent);*/
        }
    };
}

