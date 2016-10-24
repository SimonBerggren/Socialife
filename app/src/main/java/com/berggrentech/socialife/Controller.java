package com.berggrentech.socialife;

import android.content.Intent;
import android.util.JsonWriter;

import org.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by Simon on 2016-10-20.
 */
class Controller {

    private static Controller mInstance = new Controller();
    private ArrayList<Group> mGroups = new ArrayList<>();
    private TCPService.Connection mConnection;
    private ActivityListener mActivity;
    private Intent mServiceIntent;
    private TCPService mService;
    private String mMemberName;
    private String mID;
    private String mGroup;
    private ArrayList<Member> mCurrentMembers;

    static Controller getInstance() { return mInstance; }

    String getMemberName() { return mMemberName; }

    public ArrayList<Group> getGroups() { return mGroups; }

    void setListener(ActivityListener listener) {
        mActivity = listener;
    }

    boolean isConnected() {
        return mService != null && mService.isConnected();
    }

    void bindService(Intent _Intent) {
        if(mConnection == null) {
            mConnection = new TCPService.Connection();
            mActivity.startService(_Intent);
        }

        mServiceIntent = _Intent;
        mActivity.bindService(_Intent, mConnection, 0);
    }

    void unbindService() {
        mActivity.unbindService(mConnection);
    }

    void disconnect() {
        mService.disconnect();
        mActivity.unbindService(mConnection);
        mActivity.stopService(mServiceIntent);
    }

    void register(String _MemberName, String _GroupName) {
        mMemberName = _MemberName;
        try {
            StringWriter result = new StringWriter();
            new JsonWriter(result).beginObject()
                    .name("type").value("register")
                    .name("group").value(_GroupName)
                    .name("member").value(_MemberName)
                    .endObject();
            mService.send(result.toString());
        } catch (IOException ignored) { }
    }

    void unregister() {
        try {
            StringWriter result = new StringWriter();
            new JsonWriter(result).beginObject()
                    .name("type").value("unregister")
                    .name("id").value(mID)
                    .endObject();
            mService.send(result.toString());
        } catch (IOException ignored) { }
    }

    void requestGroups() {
        try {
            StringWriter result = new StringWriter();
            new JsonWriter(result).beginObject()
                    .name("type").value("groups")
                    .endObject();
            mService.send(result.toString());
        } catch (IOException ignored) { }
    }

    /**
     * Callbacks.
     */

    void onConnected() {
        mService = mConnection.getService();
        mActivity.onConnected();
    }

    void onRegister(String _ID) {
        mID = _ID;
        mGroup = _ID.split(",")[0];
        mGroups.add(new Group(mGroup));
        mGroups.get(mGroups.size() - 1).getMembers().add(new Member(getMemberName(), "NaN", "NaN"));
        mActivity.onRegistered(_ID);
    }

    void onGroupsReceived(ArrayList<String> _GroupNames) {
        mGroups.clear();
        for (String groupName : _GroupNames) {
            mGroups.add(new Group(groupName));
            if(groupName.equals(mGroup)) {
                mGroups.get(mGroups.size() - 1).fill(mCurrentMembers);
            }
        }
        mActivity.onGroupsReceived(_GroupNames);
    }

    void onLocationChanged(String _Long, String _Lat) {
        try {
            StringWriter result = new StringWriter();
            new JsonWriter(result).beginObject()
                    .name("type").value("location")
                    .name("id").value(mID)
                    .name("longitude").value(_Long)
                    .name("latitude").value(_Lat)
                    .endObject();
            mService.send(result.toString());
        } catch (IOException ignored) { }
    }

    void onMembersUpdated(String _GroupName, ArrayList<Member> _Members) {
        for (Group group : mGroups) {
            if (group.getName().equals(_GroupName)) {
                group.fill(_Members);
            }
        }
        mCurrentMembers = _Members;
        mActivity.onMembersReceived(_GroupName, _Members);
    }

    void onErrorReceived(String _Message) {
        mActivity.onErrorReceived(_Message);
    }
}