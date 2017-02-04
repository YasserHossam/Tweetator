package TwitterHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import com.yasser.tweetator.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import twitter4j.MediaEntity;
import twitter4j.Status;

public class TweetStatus implements Parcelable
{
    public int profilePictureId,index;
    public boolean isRetweeted,isFavourited,isRetweet,hasPhotoAttached,isPhotoAttachedLoaded;
    public long tweetID,currentUserRetweetId;
    byte[] profilePicture,attachedPhoto;
    public String status,userName,time,profilePictureURL,retweetUserName,twitterName,attachedPhotoURL;
    public TweetStatus()
    {
        profilePictureId= R.drawable.ic_launcher;
        hasPhotoAttached=false;
        attachedPhoto=new byte[1];
        retweetUserName="notRetweet";
        userName=null;
        isPhotoAttachedLoaded=false;
    }
    public TweetStatus(TweetStatus status)
    {
        isPhotoAttachedLoaded=false;
        profilePictureId= R.drawable.ic_launcher;
        index=status.index;
        isRetweeted=status.isRetweeted;
        isFavourited=status.isFavourited;
        isRetweet=status.isRetweet;
        hasPhotoAttached=status.hasPhotoAttached;
        profilePicture=status.profilePicture;
        attachedPhoto=status.attachedPhoto;
        tweetID=status.tweetID;
        this.status=status.status;
        userName=status.userName;
        profilePictureURL=status.profilePictureURL;
        time=status.time;
        retweetUserName=status.retweetUserName;
        twitterName=status.twitterName;
        attachedPhotoURL=status.attachedPhotoURL;
        currentUserRetweetId=status.currentUserRetweetId;

    }
    public TweetStatus(Status status)
    {
        isPhotoAttachedLoaded=false;
        retweetUserName="notRetweet";
        userName=null;
        profilePictureId= R.drawable.ic_launcher;
        hasPhotoAttached=false;
        attachedPhoto=new byte[1];
        try
        {
            isRetweet=status.isRetweet();
            if(isRetweet)
            {
                MediaEntity[] medias=status.getRetweetedStatus().getMediaEntities();
                for(int k=0;k<medias.length;k++)
                {
                    if(medias[k].getType().equals("photo"))
                    {
                        attachedPhotoURL=medias[k].getMediaURL();
                        hasPhotoAttached=true;
                        break;
                    }
                }
                retweetUserName=status.getUser().getName();
                twitterName=status.getRetweetedStatus().getUser().getScreenName();
                userName=status.getRetweetedStatus().getUser().getName();
                if(hasPhotoAttached)
                    this.status=status.getRetweetedStatus().getText().substring(0,status.getRetweetedStatus().getText().indexOf("http://"));
                else
                    this.status=status.getRetweetedStatus().getText();
                tweetID=status.getRetweetedStatus().getId();
                long createdAt=status.getRetweetedStatus().getCreatedAt().getTime();
                if(String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ").contains("ago"))
                    time=String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ").substring(0,String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ").indexOf("ago"));
                else
                    time=String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ");
                isRetweeted=status.getRetweetedStatus().isRetweeted();
                isFavourited=status.getRetweetedStatus().isFavorited();
                profilePictureURL=status.getRetweetedStatus().getUser().getBiggerProfileImageURL().toString();

            } else
            {
                MediaEntity[] medias=status.getMediaEntities();
                for(int k=0;k<medias.length;k++)
                {
                    if(medias[k].getType().equals("photo"))
                    {
                        attachedPhotoURL=medias[k].getMediaURL();
                        hasPhotoAttached=true;
                        break;
                    }
                }
                userName=status.getUser().getName();
                twitterName=status.getUser().getScreenName();
                if(hasPhotoAttached)
                    this.status=status.getText().substring(0,status.getText().indexOf("http://"));
                else
                    this.status=status.getText();
                tweetID=status.getId();
                long createdAt = status.getCreatedAt().getTime();
                if(String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ").contains("ago"))
                    time=String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ").substring(0,String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ").indexOf("ago"));
                else
                    time=String.format(DateUtils.getRelativeTimeSpanString(createdAt) + " ");
                isRetweeted=status.isRetweeted();
                isFavourited=status.isFavorited();
                profilePictureURL=status.getUser().getBiggerProfileImageURL().toString();
            }
            currentUserRetweetId=status.getCurrentUserRetweetId();
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
        }
    }

    public TweetStatus(Parcel in)
    {
        String[] data = new String[18];
        in.readStringArray(data);
        this.profilePictureId= Integer.parseInt(data[0]);
        this.index= Integer.parseInt(data[1]);
        this.isRetweeted = Boolean.valueOf(data[2]);
        this.isFavourited = Boolean.valueOf(data[3]);
        this.tweetID= Long.valueOf(data[4]);
        this.currentUserRetweetId= Long.valueOf(data[5]);
        this.profilePicture= data[6].getBytes();
        this.status= data[7];
        this.userName= data[8];
        this.time= data[9];
        this.profilePictureURL=data[10];
        this.isRetweet=Boolean.valueOf(data[11]);
        this.twitterName=data[12];
        this.retweetUserName=data[13];
        this.hasPhotoAttached=Boolean.valueOf(data[14]);
        this.attachedPhoto=data[15].getBytes();
        this.attachedPhotoURL=data[16];
        this.isPhotoAttachedLoaded=Boolean.valueOf(data[17]);
    }
    public void setPhotoAttched(Bitmap bm) throws IOException
    {
        hasPhotoAttached=true;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] array = stream.toByteArray();
        stream.close();
        attachedPhoto=array;
    }
    public Bitmap getPhotoAttached()
    {
        return BitmapFactory.decodeByteArray(attachedPhoto,0,attachedPhoto.length);
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
                        profilePictureURL,
                        String.valueOf(isRetweet),
                        twitterName,
                        retweetUserName,
                        String.valueOf(hasPhotoAttached),
                        new String(attachedPhoto),
                        attachedPhotoURL,
                        String.valueOf(isPhotoAttachedLoaded)
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