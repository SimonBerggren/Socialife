package com.berggrentech.socialife;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AutoCompleteTextView mUsernameText;
    private Button mLoginButton;
    private View mProgressView;
    private View mLoginFormView;

    boolean everythingOk = false;
    TaskManager tasks;

    TCPService service;
    TCPService.Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // setup service and connect to server
        connection = new TCPService.Connection();
        Intent intent = new Intent(this, TCPService.class);
        startService(intent);
        bindService(intent, connection, 0);

        service = connection.getService();

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
                return super.getCount(); // you don't display last item. It is used as hint.
            }

        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Groups");
        adapter.add("Item 1");
        adapter.add("Item 2");
        adapter.add("Item 3");
        adapter.add("Item 4");
        adapter.add("Item 5");

        Spinner spinner = (Spinner) findViewById(R.id.groups_spinner);
        spinner.setAdapter(adapter);

        tryLogin();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.e("REQUEST CODE", String.valueOf(requestCode));

        for (String n : permissions) {
            Log.e("PERMISSIONS", n);
        }

        for (int n : grantResults) {
            Log.e("GRANT RESULTS", String.valueOf(n));


        }
        everythingOk = true;

        if (everythingOk) {
            showProgress(true);
            tasks = new TaskManager();
            tasks.start();
            tasks.addTask(new Runnable() {
                boolean running = true;

                @Override
                public void run() {
                    while (running) {
                        try {

                            if (service == null) {
                                service = connection.getService();
                                Thread.sleep(1000);
                                Log.e("LOL", "sleeping more");
                            }
                            else {

                                Log.e("LOL", "trying to get groups");

                                StringWriter stringWriter = new StringWriter();
                                JsonWriter writer = new JsonWriter(stringWriter);
                                writer.beginObject()
                                        .name("type").value("groups")
                                        .endObject();
                                String res = stringWriter.toString();
                                service.send(res);


                                mProgressView.animate().cancel();

                                running = false;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while(TCPService.grpList == null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    showProgress(false);


                }
            });
        }
    }



    /**
     * Tries to login the user.
     */
    private void tryLogin() {

        // if the user hasn't turned on internet or location settings, warn them and turn the app off
        // TODO: I want to be able to ask the user to change settings here as well
        if(!hasInternet()) {

            // send an alert dialog to the user and close the app
            notifyUser("NO INTERNET CONNECTION", "The app will not work without internet! Please connect to the internet before using this app.", new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(LoginActivity.this, "OK", Toast.LENGTH_SHORT).show();

                    // if user haven't given permission to use location, disallow login
                    if (!hasPermissions()) {

                        requestPermissions();

                        if(!hasPermissions()) {

                            // send an alert dialog to the user and close the app
                            notifyUser("NO LOCATION PERMISSIONS", "The app will not work without permissions! Please enable location permissions before using this app.", null, null);
                        }

                        everythingOk = true;
                    }
                }
            }, new Runnable() {
                @Override
                public void run() {

                    finish();
                }
            });

        }
        //mLoginTask = new LoginTask(this, mUsernameText.getText().toString());
        //mLoginTask.execute((Void) null);
    }

    /**
     * Shows the login progress, though it is simulated to take 2 seconds.
     */
    public void showProgress(final boolean show) {

        // clear focus

        Log.e("LOL", "show progress");

        mProgressView.animate().alpha(show ? 1 : 0).withStartAction(new Runnable() {
            @Override
            public void run() {

                Log.e("LOL", "start action");
                mUsernameText.clearFocus();

                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mLoginFormView.setAlpha(show ? 0.5f : 1.0f);
                mLoginButton.setEnabled(!show);
                mUsernameText.setEnabled(!show);
            }
        }).withEndAction(new Runnable() {
            @Override
            public void run() {
                Log.e("LOL", "end action");
                Toast.makeText(LoginActivity.this, "lol", Toast.LENGTH_SHORT).show();

                if(TCPService.grpList != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_spinner_dropdown_item) {

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
                            return super.getCount(); // you don't display last item. It is used as hint.
                        }

                    };

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    ArrayList<String> grps = TCPService.grpList;

                    if(grps.size() == 0) {
                        Log.e("OMG", "LIST EMPTY");
                        for(int i = 0; i < 5; ++i) {
                            adapter.add(String.valueOf(i));
                        }
                    } else {
                        Log.e("OMG", "LIST IS NOT EMPTY");
                        for(String n : grps) {
                            adapter.add(n);
                        }
                    }

                    Spinner spinner = (Spinner) findViewById(R.id.groups_spinner);
                    spinner.setAdapter(adapter);
                }
            }
        });

    }

    /**
     * Called by LoginTask when the login was successful.
     * Starts MainActivity and destroys this (LoginActivity).
     */
    public void loginSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        bindService(intent, connection, 0);
        finish();
    }

    private boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasInternet() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LOCATION_SERVICE) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    private void notifyUser(String title, String message, final Runnable onOk, final Runnable onCancel) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onOk != null)
                    onOk.run();
            }
        });
        if(onCancel != null) {
            dlgAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onCancel.run();
                }
            });
        }
        dlgAlert.create().show();
    }
}