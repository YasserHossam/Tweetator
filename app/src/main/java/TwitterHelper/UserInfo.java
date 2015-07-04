package TwitterHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.yasser.tweetator.R;
import com.yasser.tweetator.TimelineActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import twitter4j.User;

/**
 * Created by yasser on 7/4/2015.
 */
public class UserInfo implements Parcelable
{
    public int tweetsNumber,follwingNumber,followersNumber;
    public byte[]header,profilePicture;
    public String userName,twitterName;
    public boolean isFollwingYou;
    public UserInfo()
    {

    }
    public UserInfo(User u)
    {
        tweetsNumber=u.getStatusesCount();
        follwingNumber=u.getFriendsCount();
        followersNumber=u.getFollowersCount();
        userName=u.getName();
        twitterName=u.getScreenName();
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
        return BitmapFactory.decodeByteArray(profilePicture, 0, profilePicture.length);
    }
    public void setHeader(Bitmap bm) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] array = stream.toByteArray();
        stream.close();
        header= array;
    }
    public Bitmap getHeader()
    {
        return BitmapFactory.decodeByteArray(header,0,header.length);
    }
    public Bitmap getHeader(Bitmap defaultHeader)
    {
        if(header!=null)
            return BitmapFactory.decodeByteArray(header, 0, header.length);
        return defaultHeader;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeStringArray(new String[]
                {
                        String.valueOf(this.tweetsNumber),
                        String.valueOf(this.follwingNumber),
                        String.valueOf(this.followersNumber),
                        this.userName,
                        this.twitterName,
                        new String(this.profilePicture),
                        new String(this.header),
                });
    }
    public UserInfo(Parcel in)
    {
        String[] data = new String[11];
        in.readStringArray(data);
        this.tweetsNumber= Integer.parseInt(data[0]);
        this.follwingNumber=Integer.parseInt(data[1]);
        this.followersNumber=Integer.parseInt(data[2]);
        this.userName=data[3];
        this.twitterName=data[4];
        this.profilePicture=data[5].getBytes();
        this.header=data[6].getBytes();
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public UserInfo createFromParcel(Parcel in)
        {
            return new UserInfo(in);
        }

        public UserInfo[] newArray(int size)
        {
            return new UserInfo[size];
        }
    };
}
