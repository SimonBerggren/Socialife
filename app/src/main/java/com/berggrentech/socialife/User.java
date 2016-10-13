package com.berggrentech.socialife;

import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

public class User {

    private String mID = "-1";
    private String mName = "";
    private String mLong = "NaN";
    private String mLat = "NaN";
    private List<String> mGroups;

    public User(String _ID, String _Name, String _Long, String _Lat, String[] _Groups) {
        mID = _ID;
        mName = _Name;
        mLong = _Long;
        mLat = _Lat;
        mGroups = Arrays.asList(_Groups);
    }

    public String getID() {
        return mID;
    }

    public String getmName() {
        return mName;
    }

    public String getLong() {
        return mLong;
    }

    public String getLat() {
        return mLat;
    }

    public List<String> getGroups() {
        return mGroups;
    }

    public void setPosition(String _Lat, String _Long) {
        mLat = _Lat;
        mLong = _Long;
    }

    public void removeFromGroup(String _ID) {
        mGroups.remove(_ID);
    }

    public void addToGroup(String _ID) {
        mGroups.add(_ID);
    }

    public String getJson_Members(String _GroupName) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value("members")
                    .name("group").value(_GroupName)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public String getJson_Register() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value("register")
                    .name("id").value(mID)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public String getJson_UnRegister() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value("unregister")
                    .name("id").value(mID)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    public String getJson_SetPosition() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value("location")
                    .name("id").value(mID)
                    .name("longitude").value(mLong)
                    .name("latitude").value(mLat)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }
}