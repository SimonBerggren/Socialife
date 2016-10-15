package com.berggrentech.socialife;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private LoginTask mLoginTask = null;

    private AutoCompleteTextView mUsernameText;
    private Button mLoginButton;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameText = (AutoCompleteTextView) findViewById(R.id.username);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tryLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }

        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Groups");
        adapter.add("Item 1");
        adapter.add("Item 2");


        Spinner spinner = (Spinner) findViewById(R.id.groups_spinner);
        spinner.setAdapter(adapter);
    }

    /**
     * Tries to login the user.
     */
    private void tryLogin() {
        if (mLoginTask != null) {
            return;
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // if user haven't given permission to use location, disallow login
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  || !lm.isProviderEnabled(LOCATION_SERVICE) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionCheck == PackageManager.PERMISSION_DENIED) {

                return;
            }
        }

        showProgress(true);
        mLoginTask = new LoginTask(this, mUsernameText.getText().toString());
        mLoginTask.execute((Void) null);
    }

    /**
     * Shows the login progress, though it is simulated to take 2 seconds.
     */
    public void showProgress(final boolean show) {

        // clear focus
        mUsernameText.clearFocus();

        // animate progress
        int animTime = getResources().getInteger(android.R.integer.config_longAnimTime);
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(animTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                // disable and grey out login form
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mLoginFormView.setAlpha(show ? 0.5f : 1.0f);
                mLoginButton.setEnabled(!show);
                mUsernameText.setEnabled(!show);
            }
        });

        // indicate that the login task should start
        if(mLoginTask != null)
            mLoginTask = null;
    }

    /**
     * Called by LoginTask when the login was successful.
     * Starts MainActivity and destroys this (LoginActivity).
     */
    public void loginSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}