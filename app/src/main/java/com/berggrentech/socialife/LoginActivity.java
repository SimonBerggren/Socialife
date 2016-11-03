package com.berggrentech.socialife;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */
public class LoginActivity extends ActivityListener {

    private AutoCompleteTextView mUsernameText;
    private AutoCompleteTextView mGroupText;
    private Button mLoginButton;
    private Spinner mGroupsSpinner;
    private View mProgressView;
    private View mLoginFormView;
    private static int REQUEST_CODE_LOCATION = 0;
    private Timer mLoginTimer;
    private boolean loginSucceeded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mUsernameText = (AutoCompleteTextView) findViewById(R.id.username);
        mGroupText = (AutoCompleteTextView) findViewById(R.id.group);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Controller.getInstance().isConnected()) {
                    tryLogin();
                } else {
                    Controller.getInstance().bindService(new Intent(LoginActivity.this, TCPService.class));
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mGroupsSpinner = (Spinner) findViewById(R.id.groups_spinner);
        mGroupsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mGroupsSpinner.getCount() > 1 || !mGroupsSpinner.getSelectedItem().toString().equals(getString(R.string.no_existing_groups)))
                    mGroupText.setText(mGroupsSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        (findViewById(R.id.refresh_button)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Controller.getInstance().isConnected()) {
                    Controller.getInstance().requestGroups();
                } else {
                    Controller.getInstance().bindService(new Intent(LoginActivity.this, TCPService.class));
                }
            }
        });

        // for test cases
        String memberName = "Simon";
        String group = "Test Group";

        Controller.getInstance().setListener(this);
        Controller.getInstance().bindService(new Intent(this, TCPService.class));

        if(!hasPermissions()) {
            requestPermissions();
        } else {
            if (!Controller.getInstance().isConnected()){
                showAnimation(true);
            } else {
                memberName = Controller.getInstance().getMemberName();
                Controller.getInstance().requestGroups();
            }
        }

        mUsernameText.setText(memberName);
        if(mGroupText.getText().toString().isEmpty()) {
            mGroupText.setText(group);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                askUser("", getString(R.string.rationale_location_permissions),
                        new Runnable() {    // if clicked yes
                            @Override
                            public void run() {
                                requestPermissions();
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {     // if clicked no
                                Controller.getInstance().disconnect();
                                finish();
                            }
                        });
            } else {
                showAnimation(true);
            }
        }
    }

    /**
     * Tries to login the user.
     */
    private void tryLogin() {
        if(mUsernameText.getText().length() == 0) {
            mUsernameText.setError(getString(R.string.error_empty_field));
            mUsernameText.requestFocus();
        } else if (mGroupText.getText().length() == 0) {
            mGroupText.setError(getString(R.string.error_empty_field));
            mGroupText.requestFocus();
        } else {

            Controller.getInstance().register(mUsernameText.getText().toString(), mGroupText.getText().toString());
            getCurrentFocus().clearFocus();
            showAnimation(true);

            mLoginTimer = new Timer();
            mLoginTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if(loginSucceeded) {
                        return;
                    }

                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAnimation(false);
                            notifyUser("ERROR", getString(R.string.error_connecting), new Runnable() {
                                @Override
                                public void run() {
                                    Controller.getInstance().requestGroups();
                                }
                            }, null);
                        }
                    });
                }
            }, TCPService.CONNECTION_TIMEOUT);
        }
    }

    public void showAnimation(final boolean _AnimationEnabled) {
        mProgressView.animate().alpha(_AnimationEnabled ? 1 : 0).withStartAction(new Runnable() {
            @Override
            public void run() {

                mUsernameText.clearFocus();

                mProgressView.setVisibility(_AnimationEnabled ? View.VISIBLE : View.GONE);
                mLoginFormView.setAlpha(_AnimationEnabled ? 0.5f : 1.0f);
                mLoginButton.setEnabled(!_AnimationEnabled);
                mUsernameText.setEnabled(!_AnimationEnabled);
            }
        });
    }

    private boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
    }

    private void notifyUser(String title, String message, final Runnable onOk, final Runnable onCancel) {

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(onOk != null)
                    onOk.run();
            }
        });
        if(onCancel != null) {
            dlgAlert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onCancel.run();
                }
            });
        }
        dlgAlert.create().show();
    }

    private void askUser(String title, String message, final Runnable onYes, final Runnable onNo) {

        AlertDialog.Builder askDialog  = new AlertDialog.Builder(this);
        askDialog.setTitle(title);
        askDialog.setMessage(message);
        askDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onYes.run();
            }
        });
        askDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onNo.run();
            }
        });
        askDialog.create().show();
    }

    @Override
    public void onGroupsReceived(final ArrayList<String> groups) {

        if(groups.size() == 0)
            groups.add(getString(R.string.no_existing_groups));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGroupsSpinner.setAdapter(adapter);
                showAnimation(false);
            }
        });
    }

    @Override
    public void onConnected() {
        Controller.getInstance().requestGroups();
    }

    @Override
    public void onRegistered(String id) {
        loginSucceeded = true;
        if(mLoginTimer != null) {
            mLoginTimer.cancel();
            mLoginTimer = null;
        }
        Controller.getInstance().unbindService();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onErrorReceived(final String _Message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAnimation(false);
                notifyUser(getString(R.string.error), _Message, null, null);
            }
        });
    }
}