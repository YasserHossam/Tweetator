package TwitterHelper;

import android.graphics.Bitmap;
import android.text.format.DateUtils;

import com.yasser.tweetator.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import twitter4j.Status;

public class TweetStatus
{
    public int profilePictureId;
    boolean isRetweeted,isFavourited;
    public long tweetID;
    Bitmap profilePicture;
    public String status,userName,time,profilePictureURL;
    public TweetStatus()
    {
        profilePictureId= R.drawable.ic_launcher;
    }
    public TweetStatus(Status status)
    {
        profilePictureId= R.drawable.ic_launcher;
        try
        {
            isRetweeted=status.isRetweeted();
            isFavourited=status.isFavorited();
            tweetID=status.getId();
            this.status=status.getText();
            userName=status.getUser().getScreenName();
            long createdAt=status.getCreatedAt().getTime();
            profilePictureURL=status.getUser().getProfileImageURL().toString();
            time=String.format(DateUtils.getRelativeTimeSpanString(createdAt)+" ");
        }
        catch (Exception exc){}
    }
    public TweetStatus(int PPID,String status,String userName,String time)
    {
        profilePictureId=PPID;
        this.status=status;
        this.userName=userName;
        this.time=time;
    }
    public void setProfilePicture(Bitmap bm)
    {
        profilePicture=bm;
    }
};