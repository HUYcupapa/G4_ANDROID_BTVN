package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LottieAnimationView animationView = findViewById(R.id.animationView);
        animationView.setAnimation("loading.json");
        animationView.loop(true);
        animationView.playAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    // Nếu đã đăng nhập, chuyển đến HomeActivity
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                } else {
                    // Nếu chưa đăng nhập, chuyển đến ChoiceLoginActivity
                    startActivity(new Intent(MainActivity.this, ChoiceLoginActivity.class));
                }
                finish(); // Đóng MainActivity để không quay lại khi nhấn Back
            }
        }, 3000);
    }
}
