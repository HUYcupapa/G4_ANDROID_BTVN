package com.example.myapplication.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activities.ChatbotActivity;
import com.example.myapplication.Adapter.HomeCafeAdapter;
import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private RecyclerView recyclerView;
    private HomeCafeAdapter cafeAdapter;
    private List<Cafe> cafeList;
    private FirebaseFirestore db;
    private Marker currentLocationMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Khởi tạo FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Thiết lập bản đồ mini
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_mini);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;

                // Kích hoạt nút "Vị trí" (My Location)
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }

                getCurrentLocation();
            });
        }

        // Thiết lập RecyclerView cho danh sách quán cà phê hot
        recyclerView = view.findViewById(R.id.recycler_view_hot_cafes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cafeList = new ArrayList<>();
        cafeAdapter = new HomeCafeAdapter(requireContext(), cafeList);
        recyclerView.setAdapter(cafeAdapter);

        // Tải danh sách quán cà phê từ Firestore
        loadCafesFromFirestore();

        // Xử lý nút Hỗ trợ (giữ nguyên)
        view.findViewById(R.id.btn_support).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChatbotActivity.class));
        });

        return view;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (mMap != null) {
                    mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    currentLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Vị trí của bạn"));
                    showNearbyCafes();
                }
            } else {
                Toast.makeText(requireContext(), "Không thể lấy vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    Toast.makeText(requireContext(), "Lỗi quyền truy cập vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(requireContext(), "Bạn cần cấp quyền để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCafesFromFirestore() {
        db.collection("cafes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cafeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Cafe cafe = document.toObject(Cafe.class);
                        cafe.setId(document.getId());
                        cafeList.add(cafe);
                    }
                    cafeAdapter.notifyDataSetChanged();
                    showNearbyCafes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi khi tải danh sách quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showNearbyCafes() {
        if (currentLocation == null || mMap == null) {
            Toast.makeText(requireContext(), "Vị trí hiện tại chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        mMap.clear();
        LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Vị trí của bạn"));

        for (Cafe cafe : cafeList) {
            if (cafe.getLat() == 0 || cafe.getLng() == 0) {
                continue;
            }

            Location cafeLocation = new Location("");
            cafeLocation.setLatitude(cafe.getLat());
            cafeLocation.setLongitude(cafe.getLng());

            float distance = currentLocation.distanceTo(cafeLocation);
            if (distance < 5000) { // Giới hạn 5km
                LatLng cafeLatLng = new LatLng(cafe.getLat(), cafe.getLng());
                mMap.addMarker(new MarkerOptions().position(cafeLatLng).title(cafe.getName()));
            }
        }
    }
}