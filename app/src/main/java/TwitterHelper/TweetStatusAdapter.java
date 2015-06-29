package TwitterHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.yasser.tweetator.R;
import com.yasser.tweetator.TimelineActivity;

import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
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
            holder.profilePicture = (ImageView)row.findViewById(R.id.profiePictureImageView);
            holder.status = (TextView)row.findViewById(R.id.statusTextView);
            holder.userName = (TextView)row.findViewById(R.id.userNameTextView);
            holder.time = (TextView)row.findViewById(R.id.timeTextView);
            holder.reply=(ImageButton)row.findViewById(R.id.replyButton);
            holder.retweet=(ImageButton)row.findViewById(R.id.retweetButton);
            holder.favourite=(ImageButton)row.findViewById(R.id.favouriteButton);
            row.setTag(holder);
        }
        else
        {
            holder = (TweetStatusHolder)row.getTag();
        }
        TweetStatus tweetStatus = data.get(position);

        holder.profilePicture.setImageBitmap(tweetStatus.profilePicture);
        holder.status.setText(tweetStatus.status);
        holder.userName.setText(tweetStatus.userName);
        holder.time.setText(tweetStatus.time);
        if(tweetStatus.isFavourited)
            holder.favourite.setImageResource(R.drawable.favorite_on);
        if(tweetStatus.isRetweeted)
            holder.retweet.setImageResource(R.drawable.retweet_on);
        holder.favourite.setOnClickListener(tweetListener);
        holder.favourite.setTag("favourite%" + tweetStatus.tweetID);
        holder.reply.setOnClickListener(tweetListener);
        holder.reply.setTag("reply%" + tweetStatus.tweetID+"%"+tweetStatus.userName);
        holder.retweet.setOnClickListener(tweetListener);
        holder.retweet.setTag("retweet%"+tweetStatus.tweetID);
        return row;
    }
    static class TweetStatusHolder
    {
        ImageView profilePicture;
        TextView status,userName,time;
        ImageButton favourite,reply,retweet;
    }
    private View.OnClickListener tweetListener= new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String[] tags;
            String userName=null;
            ImageButton clickedButton=(ImageButton)v;
            tags=((String)(v.getTag())).split("%");
            String buttonType=tags[0];
            long tweetID=Long.valueOf(tags[1]).longValue();
            if(tags.length>=3)
            {
                userName=tags[2];
            }
            if(buttonType.equals("favourite"))
            {
                clickedButton.setImageResource(R.drawable.favorite_on);
            }
                if(buttonType.equals("retweet"))
                {
                    clickedButton.setImageResource(R.drawable.retweet_on);
                }

            Intent intent=new Intent(v.getContext(),TimelineActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("buttonType",buttonType);
            intent.putExtra("tweetID",tweetID);
            intent.putExtra("userName",userName);
            v.getContext().startActivity(intent);
        }
    };
}

