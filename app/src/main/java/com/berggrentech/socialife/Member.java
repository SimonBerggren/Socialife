package com.berggrentech.socialife;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */
class Member {

    private String mName = "";
    private String mLong = "NaN";
    private String mLat = "NaN";

    Member(String _Name, String _Long, String _Lat) {
        mName = _Name;
        mLong = _Long;
        mLat = _Lat;
    }

    String getName() {
        return mName;
    }

    String getLong() {
        return mLong;
    }

    String getLat() {
        return mLat;
    }
}