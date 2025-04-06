package com.example.myapplication.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private EditText editTextName, editTextPhone;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void saveUserProfile() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> userProfile = new HashMap<>();
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        userProfile.put("role", role != null ? role : "Customer");
                    } else {
                        userProfile.put("role", "Customer");
                    }
                    userProfile.put("name", name);
                    userProfile.put("phone", phone);

                    db.collection("users").document(user.getUid()).set(userProfile)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ProfileActivity.this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Lỗi khi lưu hồ sơ!", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Lỗi khi lấy thông tin!", Toast.LENGTH_SHORT).show());
    }
}