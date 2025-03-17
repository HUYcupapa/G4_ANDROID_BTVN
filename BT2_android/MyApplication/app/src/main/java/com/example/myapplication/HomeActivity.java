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
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback, CityAdapter.OnItemClickListener {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CityAdapter cityAdapter;
    private List<City> cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra đăng nhập
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityList = new ArrayList<>();
        cityAdapter = new CityAdapter(cityList, this);
        recyclerView.setAdapter(cityAdapter);

        // Load dữ liệu từ Firestore
        loadCities();

        // Khởi tạo Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadCities() {
        db.collection("cities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cityList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    City city = document.toObject(City.class);

                    // Nếu Firestore chưa có tọa độ, bỏ qua thành phố đó
                    if (!document.contains("latitude") || !document.contains("longitude")) {
                        Log.e("Firestore", "Thiếu tọa độ cho: " + city.getName());
                        continue;
                    }

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

        // Đặt vị trí mặc định là Hà Nội
        LatLng hanoi = new LatLng(21.0285, 105.8542);
        mMap.addMarker(new MarkerOptions().position(hanoi).title("Hà Nội, Việt Nam"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 5));
    }

    @Override
    public void onItemClick(City city) {
        LatLng cityLocation = new LatLng(city.getLatitude(), city.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 10));
        mMap.addMarker(new MarkerOptions().position(cityLocation).title(city.getName()));
    }
}
