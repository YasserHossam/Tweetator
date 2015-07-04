package TwitterHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import com.yasser.tweetator.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import twitter4j.Status;

public class TweetStatus implements Parcelable
{
    public int profilePictureId,index;
    public boolean isRetweeted,isFavourited;
    public long tweetID,currentUserRetweetId;
    byte[] profilePicture;
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
            profilePictureURL=status.getUser().getBiggerProfileImageURL().toString();
            time=String.format(DateUtils.getRelativeTimeSpanString(createdAt)+" ");
            currentUserRetweetId=status.getCurrentUserRetweetId();
        }
        catch (Exception exc){}
    }
    public TweetStatus(TweetStatus status)
    {
        profilePictureId= R.drawable.ic_launcher;
        try
        {
            isRetweeted=status.isRetweeted;
            isFavourited=status.isFavourited;
            tweetID=status.tweetID;
            this.status=status.status;
            userName=status.userName;
            profilePictureURL=status.profilePictureURL;
            time=status.time;
            currentUserRetweetId=status.currentUserRetweetId;
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
    public TweetStatus(Parcel in)
    {
        String[] data = new String[11];
        in.readStringArray(data);
        this.profilePictureId= Integer.parseInt(data[0]);
        this.index= Integer.parseInt(data[1]);
        this.isRetweeted= Boolean.valueOf(data[2]);
        this.isFavourited= Boolean.valueOf(data[3]);
        this.tweetID= Long.valueOf(data[4]);
        this.currentUserRetweetId= Long.valueOf(data[5]);
        this.profilePicture= data[6].getBytes();
        this.status= data[7];
        this.userName= data[8];
        this.time= data[9];
        this.profilePictureURL=data[10];
    }
    public void setProfilePicture(Bitmap bm) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] array = stream.toByteArray();
        stream.close();
        profilePicture= array;
    }
    public Bitmap getProfilePicture()
    {
        return BitmapFactory.decodeByteArray(profilePicture,0,profilePicture.length);
    }
    @Override
    public int describeContents()
    {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeStringArray(new String[]
                {
                        String.valueOf(profilePictureId),
                        String.valueOf(index),
                        String.valueOf(isRetweeted),
                        String.valueOf(isFavourited),
                        String.valueOf(tweetID),
                        String.valueOf(currentUserRetweetId),
                        new String(profilePicture),
                        status,
                        userName,
                        time,
                        profilePictureURL
                });
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public TweetStatus createFromParcel(Parcel in)
        {
            return new TweetStatus(in);
        }

        public TweetStatus[] newArray(int size)
        {
            return new TweetStatus[size];
        }
    };

};