package com.berggrentech.socialife;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */
public class ActivityListener extends AppCompatActivity {
    public void onConnected() { }

    public void onRegistered(String _ID) { }

    public void onGroupsReceived(ArrayList<String> _Groups) { }

    public void onMembersReceived(String _GroupName, ArrayList<Member> _Members) { }

    public void onErrorReceived(String _Message) { }
}