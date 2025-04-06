package com.example.myapplication.Fragment;

import android.Manifest;
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
import com.example.myapplication.Adapter.CafeAdapter;
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

public class HomeFragment extends Fragment implements CafeAdapter.OnItemClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private RecyclerView recyclerView;
    private CafeAdapter cafeAdapter;
    private List<Cafe> cafeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Thiết lập bản đồ mini
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_mini);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                getCurrentLocation();
            });
        }

        // Thiết lập RecyclerView cho danh sách quán café hot
        recyclerView = view.findViewById(R.id.recycler_view_hot_cafes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cafeList = getCafeList();
        cafeAdapter = new CafeAdapter(cafeList, this);
        recyclerView.setAdapter(cafeAdapter);

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
        if (currentLocation == null) {
            Toast.makeText(requireContext(), "Vị trí hiện tại chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Cafe cafe : cafeList) {
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

    @Override
    public void onItemClick(Cafe cafe) {
        // TODO: Xử lý khi người dùng nhấn vào một quán cà phê (ví dụ: hiển thị chi tiết quán)
        Toast.makeText(requireContext(), "Bạn đã chọn: " + cafe.getName(), Toast.LENGTH_SHORT).show();
    }
}