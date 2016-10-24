package com.berggrentech.socialife;

import java.util.ArrayList;

/**
 * Created by Simon on 2016-10-22.
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
