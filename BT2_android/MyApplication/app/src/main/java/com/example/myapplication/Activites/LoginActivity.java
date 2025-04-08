package com.example.myapplication.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edEmail, edPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private TextView txtSignup, txtForgerPass;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        edEmail = findViewById(R.id.edemailLg);
        edPassword = findViewById(R.id.edpasswordLg);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignup = findViewById(R.id.txtSignup);
        txtForgerPass = findViewById(R.id.txtForgetPass);

        btnLogin.setOnClickListener(v -> loginUser());

        txtSignup.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        txtForgerPass.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPassActivity.class)));
    }

    private void loginUser() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = mAuth.getCurrentUser().getUid();
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String role = documentSnapshot.getString("role");
                                    if (role != null && role.equals("Admin")) {
                                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, CheckProfileActivity.class));
                                    }
                                    finish();
                                } else {
                                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(LoginActivity.this, "Lỗi khi kiểm tra vai trò!", Toast.LENGTH_SHORT).show();

                                finish();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Sai Tài Khoản Hoặc Mật khẩu!", Toast.LENGTH_SHORT).show());
    }
}