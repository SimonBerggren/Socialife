package com.berggrentech.socialife;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */

import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Represents an asynchronous login/registration task used to authenticate the user before allowing him/her to connect to the server.
 * Also asks for location permissions, as the app will not work without them.
 */
class LoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String mUsername;
    private final LoginActivity mActivity;

    LoginTask(LoginActivity _Activity, String _Username) {
        mActivity = _Activity;
        mUsername = _Username;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            // Simulate network access.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return false;
        }

        // register to server

        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mActivity.showProgress(false);

        if(success) {
            mActivity.loginSuccess();
        }
        else
            Toast.makeText(mActivity, "Login was not successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCancelled() {
        mActivity.showProgress(false);
        Toast.makeText(mActivity, "Login was cancelled", Toast.LENGTH_SHORT).show();
    }
}
