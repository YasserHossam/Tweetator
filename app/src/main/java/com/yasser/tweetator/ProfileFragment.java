package com.yasser.tweetator;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import TwitterHelper.UserInfo;

/**
 * Created by yasser on 7/3/2015.
 */
public class ProfileFragment extends Fragment
{
    View fragmentView;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       fragmentView=getView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        fragmentView=inflater.inflate(R.layout.profile_fragment,container,false);
        TextView tweetsTextView=(TextView)fragmentView.findViewById(R.id.tweetsNumberTextView);
        TextView followingTextView=(TextView)fragmentView.findViewById(R.id.followingNumberTextView);
        TextView follwersTextView=(TextView)fragmentView.findViewById(R.id.followersNumberTextView);
        TextView userNameTextView=(TextView)fragmentView.findViewById(R.id.userNameTextView);
        TextView twitterNameTextView=(TextView)fragmentView.findViewById(R.id.twitterNameTextView);
        ImageView profilePictureImageView=(ImageView)fragmentView.findViewById(R.id.profiePictureImageView);
        ImageView headerImageView=(ImageView)fragmentView.findViewById(R.id.headerImageView);
        UserInfo userInfo=getArguments().getParcelable("userInfo");
        tweetsTextView.setText(""+userInfo.tweetsNumber);
        followingTextView.setText(""+userInfo.follwingNumber);
        follwersTextView.setText(""+userInfo.followersNumber);
        userNameTextView.setText(userInfo.userName);
        twitterNameTextView.setText("@"+userInfo.twitterName);
        profilePictureImageView.setImageBitmap(userInfo.getProfilePicture());
        headerImageView.setImageBitmap(userInfo.getHeader());
        return fragmentView;
    }
}
