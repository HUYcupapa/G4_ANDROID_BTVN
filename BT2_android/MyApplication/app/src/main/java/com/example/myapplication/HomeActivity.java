package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityList = new ArrayList<>();
        cityAdapter = new CityAdapter(cityList, this);
        recyclerView.setAdapter(cityAdapter);

        deleteAllCities(() -> {
            addDefaultCities();
            loadCities();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Xóa toàn bộ dữ liệu cũ trong Firestore
     */
    private void deleteAllCities(Runnable onComplete) {
        db.collection("cities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    db.collection("cities").document(document.getId()).delete()
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Đã xóa thành phố: " + document.getId()))
                            .addOnFailureListener(e -> Log.e("Firestore", "Lỗi khi xóa thành phố", e));
                }
            }
            onComplete.run();  // Chạy tiếp khi xóa xong
        });
    }

    /**
     * Thêm danh sách thành phố mặc định vào Firestore
     */
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

    /**
     * Load danh sách thành phố từ Firestore lên RecyclerView
     */
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
}
