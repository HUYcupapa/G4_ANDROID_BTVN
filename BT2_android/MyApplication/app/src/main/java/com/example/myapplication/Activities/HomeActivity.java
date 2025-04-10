package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.myapplication.Fragment.CheckinFragment;
import com.example.myapplication.Fragment.HomeFragment;
import com.example.myapplication.Fragment.RewardsFragment;
import com.example.myapplication.Fragment.SearchFragment;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentFragmentTag = "HomeFragment"; // Theo dõi fragment hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Kiểm tra đăng nhập
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Lấy tên người dùng từ Firestore
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        TextView toolbarTitle = findViewById(R.id.toolbar_title);
                        toolbarTitle.setText("Xin chào, " + (name != null ? name : "Khách") + " ☕");

                        // Hiển thị thông báo khi đăng nhập thành công
                        showAndSaveNotification("Đừng bỏ lỡ những ưu đãi hot........");
                    }
                });

        // Xử lý sự kiện cho icon trên toolbar
        findViewById(R.id.logout_icon).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.notification_icon).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        findViewById(R.id.account_icon).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Thiết lập Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                currentFragmentTag = "HomeFragment";
            } else if (itemId == R.id.nav_checkin) {
                selectedFragment = new CheckinFragment();
                currentFragmentTag = "CheckinFragment";
                // Hiển thị thông báo khi chuyển sang CheckinFragment
                showAndSaveNotification("Hãy khám phá thêm nhiều quán cafe hot gần đây");
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
                currentFragmentTag = "SearchFragment";
            } else if (itemId == R.id.nav_rewards) {
                selectedFragment = new RewardsFragment();
                currentFragmentTag = "RewardsFragment";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Mặc định hiển thị HomeFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void showAndSaveNotification(String message) {
        // Hiển thị thông báo bằng Toast
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Lưu thông báo vào Firestore
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Notification notification = new Notification(message, System.currentTimeMillis());

        db.collection("notifications")
                .document(userId)
                .collection("user_notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    // Lưu thành công
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu thông báo!", Toast.LENGTH_SHORT).show();
                });
    }
}