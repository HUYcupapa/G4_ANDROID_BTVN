package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
            startActivity(new Intent(CheckProfileActivity.this, ChoiceLoginActivity.class));
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
                    startActivity(new Intent(CheckProfileActivity.this, ChoiceLoginActivity.class));
                    finish();
                });
    }
}
