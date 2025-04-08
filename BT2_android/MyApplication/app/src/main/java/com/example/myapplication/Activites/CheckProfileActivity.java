package com.example.myapplication.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CheckProfileActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(CheckProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        checkUserProfile(user.getUid());
    }

    private void checkUserProfile(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // ✅ Đã có hồ sơ → vào HomeActivity
                        startActivity(new Intent(CheckProfileActivity.this, HomeActivity.class));
                    } else {
                        // ❌ Chưa có hồ sơ → vào ProfileActivity
                        startActivity(new Intent(CheckProfileActivity.this, ProfileActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(CheckProfileActivity.this, LoginActivity.class));
                    finish();
                });
    }

    public static class ViewProfileActivity extends AppCompatActivity {
        private TextView tvName, tvPhone;
        private FirebaseFirestore db;
        private FirebaseUser user;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_view_profile);

            tvName = findViewById(R.id.tvName);
            tvPhone = findViewById(R.id.tvPhone);

            db = FirebaseFirestore.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                loadUserProfile();
            }
        }

        private void loadUserProfile() {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String phone = documentSnapshot.getString("phone");
                    tvName.setText("Tên: " + name);
                    tvPhone.setText("SĐT: " + phone);
                } else {
                    Toast.makeText(this, "Không tìm thấy hồ sơ!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải hồ sơ!", Toast.LENGTH_SHORT).show());
        }
    }
}
