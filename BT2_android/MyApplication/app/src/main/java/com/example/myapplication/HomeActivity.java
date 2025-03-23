package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.profile.ViewProfileActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback, CityAdapter.OnItemClickListener {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CityAdapter cityAdapter;
    private List<City> cityList;
    private FirebaseUser user;
    private Button btnViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityList = new ArrayList<>();
        cityAdapter = new CityAdapter(cityList, this);
        recyclerView.setAdapter(cityAdapter);

        btnViewProfile = findViewById(R.id.btnViewProfile);
        btnViewProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ViewProfileActivity.class));
        });

        checkUserProfile();

        deleteAllCities(() -> {
            addDefaultCities();
            loadCities();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void deleteAllCities(Runnable onComplete) {
        db.collection("cities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    db.collection("cities").document(document.getId()).delete()
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Đã xóa thành phố: " + document.getId()))
                            .addOnFailureListener(e -> Log.e("Firestore", "Lỗi khi xóa thành phố", e));
                }
            }
            onComplete.run();
        });
    }

    private void addDefaultCities() {
        String[] cityNames = {"Los Angeles", "Beijing", "Tokyo"};
        double[][] coordinates = {
                {34.0522, -118.2437},
                {39.9042, 116.4074},
                {35.6895, 139.6917}
        };

        for (int i = 0; i < cityNames.length; i++) {
            String cityName = cityNames[i];
            double lat = coordinates[i][0];
            double lng = coordinates[i][1];

            Map<String, Object> cityData = new HashMap<>();
            cityData.put("name", cityName);
            cityData.put("latitude", lat);
            cityData.put("longitude", lng);

            db.collection("cities").document(cityName).set(cityData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Thêm thành phố: " + cityName))
                    .addOnFailureListener(e -> Log.e("Firestore", "Lỗi thêm thành phố", e));
        }
    }

    private void loadCities() {
        db.collection("cities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cityList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    City city = document.toObject(City.class);
                    cityList.add(city);
                }
                cityAdapter.notifyDataSetChanged();
            } else {
                Log.e("Firestore", "Lỗi khi tải dữ liệu", task.getException());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(21.0285, 105.8542);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5));
    }

    @Override
    public void onItemClick(City city) {
        LatLng cityLocation = new LatLng(city.getLatitude(), city.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 10));
        mMap.addMarker(new MarkerOptions().position(cityLocation).title(city.getName()));
    }

    private void checkUserProfile() {
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ChoiceLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "Chào mừng bạn!", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lấy dữ liệu!", Toast.LENGTH_SHORT).show());
    }
}
