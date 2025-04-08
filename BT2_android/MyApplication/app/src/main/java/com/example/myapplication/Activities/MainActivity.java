package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LottieAnimationView animationView = findViewById(R.id.animationView);
        animationView.setAnimation("loading.json");
        animationView.loop(true);
        animationView.playAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
                                if (role != null && role.equals("Admin")) {
                                    startActivity(new Intent(MainActivity.this, AdminActivity.class));
                                } else {
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                }
                            } else {
                                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                            }
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        });
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, 3000);
    }
}