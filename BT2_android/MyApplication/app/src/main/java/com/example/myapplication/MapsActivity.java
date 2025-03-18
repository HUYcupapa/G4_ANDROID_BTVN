// MapsActivity.java
package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnOpenMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnOpenMap.setOnClickListener(v -> {
            if (mMap != null) {
                LatLng location = new LatLng(37.7749, -122.4194);
                mMap.addMarker(new MarkerOptions().position(location).title("San Francisco"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(21.0285, 105.8542);
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Hà Nội, Việt Nam"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
    }
}
