package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Fragment.CheckinFragment;
import com.example.myapplication.Fragment.HomeFragment;
import com.example.myapplication.Fragment.RewardsFragment;
import com.example.myapplication.Fragment.SearchFragment;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private boolean isManualNavigation = false; // Cờ để kiểm soát xung đột
    private String currentFragmentTag = "HomeFragment"; // Theo dõi fragment hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra đăng nhập
        FirebaseUser user = mAuth.getCurrentUser();
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

        // Thiết lập Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý sự kiện Navigation Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(this, ChatbotActivity.class));
            } else if (itemId == R.id.nav_logout) {
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Lấy tên người dùng từ Firestore và hiển thị trên Toolbar
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        TextView toolbarTitle = findViewById(R.id.toolbar_title);
                        toolbarTitle.setText("Xin chào, " + (name != null ? name : "Khách") + " ☕");

                        // Hiển thị thông báo khi đăng nhập thành công
                        showAndSaveNotification("Đừng bỏ lỡ những ưu đãi hot!");
                    }
                });

        // Xử lý sự kiện cho các icon trên Toolbar
        findViewById(R.id.notification_icon).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        findViewById(R.id.account_icon).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Thiết lập Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (isManualNavigation) {
                isManualNavigation = false; // Reset cờ sau khi xử lý
                return true; // Không thực hiện thay đổi fragment nếu là điều hướng thủ công
            }

            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                currentFragmentTag = "HomeFragment";
            } else if (itemId == R.id.nav_checkin) {
                selectedFragment = new CheckinFragment();
                currentFragmentTag = "CheckinFragment";
                // Hiển thị thông báo khi chuyển sang CheckinFragment
                showAndSaveNotification("Hãy khám phá thêm nhiều quán cafe hot gần đây!");
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

    // Phương thức công khai để fragment gọi
    public void setManualNavigation(boolean value) {
        isManualNavigation = value;
    }
}