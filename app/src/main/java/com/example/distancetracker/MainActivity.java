package com.example.distancetracker;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import android.location.Location;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.os.Looper;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button startButton;
    private Button stopButton;
    private TextView distanceTravelledText;
    private boolean isTracking = false;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private float totalDistance = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        distanceTravelledText = findViewById(R.id.distanceTravelled);
        NumberPicker numberPicker = findViewById(R.id.targetDistancePicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPicker.setValue(1);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();
            }
        });
    }

    private void startTracking() {
        if (!isTracking) {
            isTracking = true;
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    for (Location location : locationResult.getLocations()) {
                        if (lastLocation != null) {
                            totalDistance += lastLocation.distanceTo(location);
                        }
                        lastLocation = location;

                        //convert to kilometers
                        float totalDistanceKm = totalDistance / 1000f;
                        distanceTravelledText.setText(String.format("%.2f", totalDistanceKm) + " km");
                    }
                }
            };
            LocationRequest locationRequest = new LocationRequest.Builder(10000)
                    .setMinUpdateIntervalMillis(5000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build();
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException e) {
                //Handle SecurityException
            }
        }

    }

    private void stopTracking() {
        if (isTracking) {
            isTracking = false;
            fusedLocationClient.removeLocationUpdates(locationCallback);
            //display total distance or something else
            totalDistance = 0f; //reset total distance
            lastLocation = null; //reset last location
        }
    }
}