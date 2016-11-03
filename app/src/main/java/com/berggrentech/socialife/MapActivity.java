package com.berggrentech.socialife;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Simon Berggren for assignment 2 in the course Development of Mobile Devices.
 */
public class MapActivity extends ActivityListener
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Timer mTimer;
    private boolean inTime = true;
    private boolean shouldGoToUser = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = new Intent(this, TCPService.class);
        Controller.getInstance().setListener(this);
        Controller.getInstance().bindService(intent);

        NavigationView left = (NavigationView) findViewById(R.id.drawer_left);
        left.setNavigationItemSelectedListener(this);

        NavigationView right = (NavigationView) findViewById(R.id.drawer_right);
        right.setNavigationItemSelectedListener(this);
        ExpandableListView groupView = (ExpandableListView) right.findViewById(R.id.groups);
        groupView.setAdapter(new GroupViewAdapter(this, Controller.getInstance().getGroups()));

        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller.getInstance().requestGroups();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.slider_left_groups) {
            Controller.getInstance().requestGroups();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.slider_left_groups) {
            NavigationView left = (NavigationView) findViewById(R.id.drawer_left);
            NavigationView right = (NavigationView) findViewById(R.id.drawer_right);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(left);
            drawer.openDrawer(right);
        } else if (id == R.id.slider_left_logout) {
            Controller.getInstance().unbindService();
            Controller.getInstance().unregister();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        String longitude = String.valueOf(location.getLongitude());
        String latitude = String.valueOf(location.getLatitude());
        Controller.getInstance().onLocationChanged(longitude, latitude);
        if(shouldGoToUser) {
            CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)), 15.0f);
            mMap.animateCamera(loc);
            shouldGoToUser = false;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("API CONNECTION", "FAILED");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(20000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.build();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mClient, mLocationRequest, this);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || !lm.isProviderEnabled(LOCATION_SERVICE) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("API CONNECTION", "SUSPENDED");
    }

    @Override
    protected void onPause() {
        Log.e("STATUS", "PAUSE");

        if(mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!inTime) {
                    Controller.getInstance().unregister();
                    Controller.getInstance().disconnect();
                    Controller.getInstance().unbindService();
                    startActivity(new Intent(MapActivity.this, LoginActivity.class));
                    finish();
                }
                inTime = false;
            }
        }, 1000 * 120); // 2 minute delay before quitting service

        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e("STATUS", "RESUME");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(mTimer != null)
                mTimer.cancel();
            return;
        }

        if(mTimer != null) {
            inTime = true;
            mTimer.cancel();
            mTimer = null;
        }

        if (mClient != null && mClient.isConnected()) {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(30000);
            mLocationRequest.setFastestInterval(20000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            builder.build();

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mClient, mLocationRequest, this);
        }
        super.onResume();
    }

    @Override
    public void onGroupsReceived(ArrayList<String> _Groups) {

        final ArrayList groups = Controller.getInstance().getGroups();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ExpandableListView groupView = (ExpandableListView) findViewById(R.id.groups);
                groupView.setAdapter(new GroupViewAdapter(MapActivity.this, groups));
            }
        });
    }

    @Override
    public void onMembersReceived(String _GroupName, final ArrayList<Member> _Members) {

        Controller.getInstance().requestGroups();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mMap.clear();

                for(int i = 0; i < _Members.size(); ++i) {
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(Double.parseDouble(_Members.get(i).getLat()), Double.parseDouble(_Members.get(i).getLong())));
                    marker.title(_Members.get(i).getName());
                    mMap.addMarker(marker);
                }
            }
        });
    }
}