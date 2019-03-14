package com.example.periodic_location_update;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private Button start;
    private TextView textView;

    private String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
    private String displayText;
    final private String defaultText = "Location Services Stopped.\nPress Start to Start the services.";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    final private long UPDATE_INTERVAL = 4 * 1000;  /* 10 secs */
    final private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        textView = findViewById(R.id.textView);
        displayText = "Longitude: \n\n\nLatitude: ";

        textView.setText(defaultText);

        mFusedLocationClient = getFusedLocationProviderClient(this);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_SHORT).show();
                requestLocationPermission();
                textView.setText(displayText);
            }
        });
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Latitude: " +
                Double.toString(location.getLatitude()) + ",\n\nLongitude: " +
                Double.toString(location.getLongitude()) + "\n\nCounter: " + counter;
        counter++;
        textView.setText(msg);
        String counterStr = String.valueOf(counter);
        Toast.makeText(this, counterStr, Toast.LENGTH_SHORT).show();
    }

    private void requestLocationPermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
            startLocationUpdates();
        } else {
            EasyPermissions.requestPermissions(this, "We need permissions For Location Services.",
                    123, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Thanks For Permissions", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> list) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, list)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        requestLocationPermission();
    }
}