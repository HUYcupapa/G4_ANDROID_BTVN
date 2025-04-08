package com.example.myapplication.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.List;

public class CheckinFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private List<Cafe> cafeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin, container, false);

        // Khởi tạo FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Lấy danh sách quán cà phê
        cafeList = getCafeList();

        // Xử lý nút Check-in
        Button btnCheckin = view.findViewById(R.id.btn_checkin);
        btnCheckin.setOnClickListener(v -> checkin());

        // Lấy vị trí hiện tại
        getCurrentLocation();

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
        cafes.add(new Cafe("Cafe Thái Hà", 21.014708100199694, 105.81942573865054));
        return cafes;
    }

    private void checkin() {
        if (currentLocation == null) {
            Toast.makeText(requireContext(), "Vị trí hiện tại chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean canCheckin = false;
        String cafeName = "";
        for (Cafe cafe : cafeList) {
            Location cafeLocation = new Location("");
            cafeLocation.setLatitude(cafe.getLat());
            cafeLocation.setLongitude(cafe.getLng());

            float distance = currentLocation.distanceTo(cafeLocation);
            if (distance < 5000) { // Giới hạn 50m
                canCheckin = true;
                cafeName = cafe.getName();
                break;
            }
        }

        if (canCheckin) {
            Toast.makeText(requireContext(), "Check-in thành công tại " + cafeName, Toast.LENGTH_SHORT).show();
            // TODO: Lưu thông tin check-in vào Firestore
        } else {
            Toast.makeText(requireContext(), "Không có quán cà phê nào trong vòng 50m!", Toast.LENGTH_SHORT).show();
        }
    }
}