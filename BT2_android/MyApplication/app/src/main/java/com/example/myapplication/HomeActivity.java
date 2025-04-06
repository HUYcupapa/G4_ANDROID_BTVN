package com.example.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.location.Location;
import android.Manifest;

import com.example.myapplication.Adapter.CityAdapter;
import com.example.myapplication.Model.City;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import com.example.myapplication.Model.Cafe;
import com.example.myapplication.profile.ViewProfileActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;


public class HomeActivity extends FragmentActivity implements OnMapReadyCallback, CityAdapter.OnItemClickListener {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CityAdapter cityAdapter;
    private List<City> cityList;
    private FirebaseUser user;
    private Button btnViewProfile, btnShowCafes;
    private AutoCompleteTextView autoCompleteSearch;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnCurrentLocation;

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
        autoCompleteSearch = findViewById(R.id.autoCompleteSearch);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        btnShowCafes = findViewById(R.id.btnShowCafes);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityList = new ArrayList<>();
        cityAdapter = new CityAdapter(cityList, this);
        recyclerView.setAdapter(cityAdapter);
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);

        btnViewProfile = findViewById(R.id.btnViewProfile);
        btnViewProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ViewProfileActivity.class));
        });

        btnCurrentLocation.setOnClickListener(v -> {
            getCurrentLocation();  // Lấy vị trí hiện tại
           // Hiển thị quán cà phê gần đó
        });

        btnShowCafes.setOnClickListener(v -> showNearbyCafes());

        checkUserProfile();

        deleteAllCities(() -> {
            addDefaultCities();
            loadCities();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        // Danh sách địa điểm có sẵn
        List<String> cityList = getCityList();

        // Adapter cho AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cityList);
        autoCompleteSearch.setAdapter(adapter);

        // Bắt sự kiện khi người dùng chọn địa điểm
        autoCompleteSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            searchCityOnMap(selectedCity);
        });

        getCurrentLocation();



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

    private List<String> getCityList() {
        return Arrays.asList(
                "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Tokyo", "New York", "London",
                "Los Angeles", "Bangkok", "Singapore", "Paris"
        );
    }


    // Hàm tìm kiếm vị trí trên Google Maps (sửa theo danh sách có sẵn)
    private void searchCityOnMap(String cityName) {
        Map<String, LatLng> cityCoordinates = new HashMap<>();
        cityCoordinates.put("Hà Nội", new LatLng(21.0285, 105.8542));
        cityCoordinates.put("Hồ Chí Minh", new LatLng(10.7769, 106.7009));
        cityCoordinates.put("Đà Nẵng", new LatLng(16.0471, 108.2062));
        cityCoordinates.put("Tokyo", new LatLng(35.6895, 139.6917));
        cityCoordinates.put("New York", new LatLng(40.7128, -74.0060));
        cityCoordinates.put("London", new LatLng(51.5074, -0.1278));
        cityCoordinates.put("Los Angeles", new LatLng(34.0522, -118.2437));
        cityCoordinates.put("Bangkok", new LatLng(13.7563, 100.5018));
        cityCoordinates.put("Singapore", new LatLng(1.3521, 103.8198));
        cityCoordinates.put("Paris", new LatLng(48.8566, 2.3522));

        if (cityCoordinates.containsKey(cityName)) {
            LatLng cityLocation = cityCoordinates.get(cityName);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 12));
            mMap.addMarker(new MarkerOptions().position(cityLocation).title(cityName));
        } else {
            Toast.makeText(this, "Không tìm thấy vị trí!", Toast.LENGTH_SHORT).show();
        }

    }




    private List<Cafe> getCafeList() {
        List<Cafe> cafes = new ArrayList<>();
        cafes.add(new Cafe("The Coffee House", 21.028511, 105.804817)); // Nguyễn Chí Thanh, Hà Nội
        cafes.add(new Cafe("Highlands Coffee", 21.025292, 105.852871)); // Hồ Gươm, Hà Nội
        cafes.add(new Cafe("Cộng Cà Phê", 21.035978, 105.853987)); // 35A Nguyễn Hữu Huân, Hà Nội
        return cafes;
    }



    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Yêu cầu quyền nếu chưa được cấp
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.clear();

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Vị trí của bạn"));


                }
            }
        });
    }




    private void showNearbyCafes() {
        if (currentLocation == null) {
            Toast.makeText(this, "Vị trí hiện tại chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Cafe> cafes = getCafeList();
        for (Cafe cafe : cafes) {
            Location cafeLocation = new Location("");
            cafeLocation.setLatitude(cafe.getLat());
            cafeLocation.setLongitude(cafe.getLng());

            float distance = currentLocation.distanceTo(cafeLocation);
            if (distance < 500000000) { // Giới hạn 5km
                LatLng cafeLatLng = new LatLng(cafe.getLat(), cafe.getLng());
                mMap.addMarker(new MarkerOptions().position(cafeLatLng).title(cafe.getName()));
            }
        }


    }
}
