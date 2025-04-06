package com.example.myapplication.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private List<Cafe> cafeList;
    private float maxDistance = 5000; // Mặc định 5km
    private float minRating = 0; // Mặc định tất cả

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Khởi tạo FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Thiết lập bản đồ lớn
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_large);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                getCurrentLocation();
            });
        }

        // Lấy danh sách quán cà phê
        cafeList = getCafeList();

        // Thiết lập bộ lọc
        Spinner spinnerDistance = view.findViewById(R.id.spinner_distance);
        Spinner spinnerRating = view.findViewById(R.id.spinner_rating);

        spinnerDistance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: maxDistance = 1000; break;
                    case 1: maxDistance = 5000; break;
                    case 2: maxDistance = 10000; break;
                    case 3: maxDistance = 20000; break;
                }
                showNearbyCafes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: minRating = 0; break;
                    case 1: minRating = 3; break;
                    case 2: minRating = 4; break;
                    case 3: minRating = 5; break;
                }
                showNearbyCafes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Vị trí của bạn"));
                    showNearbyCafes();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(requireContext(), "Bạn cần cấp quyền để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Cafe> getCafeList() {
        List<Cafe> cafes = new ArrayList<>();
        cafes.add(new Cafe("The Coffee House", 21.028511, 105.804817));
        cafes.add(new Cafe("Highlands Coffee", 21.025292, 105.852871));
        cafes.add(new Cafe("Cộng Cà Phê", 21.035978, 105.853987));
        return cafes;
    }

    private void showNearbyCafes() {
        if (currentLocation == null || mMap == null) {
            Toast.makeText(requireContext(), "Vị trí hiện tại chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        mMap.clear();
        LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Vị trí của bạn"));

        for (Cafe cafe : cafeList) {
            Location cafeLocation = new Location("");
            cafeLocation.setLatitude(cafe.getLat());
            cafeLocation.setLongitude(cafe.getLng());

            float distance = currentLocation.distanceTo(cafeLocation);
            // Giả sử rating của quán cà phê (cần thêm vào model Cafe nếu muốn thực tế)
            float rating = 4.5f; // Giả định
            if (distance <= maxDistance && rating >= minRating) {
                LatLng cafeLatLng = new LatLng(cafe.getLat(), cafe.getLng());
                mMap.addMarker(new MarkerOptions().position(cafeLatLng).title(cafe.getName()));
            }
        }
    }
}