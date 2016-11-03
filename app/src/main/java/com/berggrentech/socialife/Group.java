package com.berggrentech.socialife;

import java.util.ArrayList;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

class Group {

    private String mName;
    private ArrayList<Member> mMembers;

    Group(String _Name) {
        mName = _Name;
        mMembers = new ArrayList<>();
    }

    String getName() { return mName; }

    ArrayList<Member> getMembers() { return mMembers; }

    void fill(ArrayList<Member> _Members) {
        mMembers.clear();
        mMembers.addAll(_Members);
    }
}
