package com.berggrentech.socialife;

import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

class Utils {

    // helper method for validating email input
    static boolean isValidEmail(CharSequence _Input) {
        return !TextUtils.isEmpty(_Input) && android.util.Patterns.EMAIL_ADDRESS.matcher(_Input).matches();
    }

    // helper method for easier logging
    static void Log(String _Msg) {
        Log.w("Simon says", _Msg);
    }

    // helper method for easier logging
    static void Log(int _Msg) {
        Log.w("Simon says", String.valueOf(_Msg));
    }

}