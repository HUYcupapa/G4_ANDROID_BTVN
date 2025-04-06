package com.example.myapplication.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {
    private EditText edphone, edotp;
    private Button btnloginotp, btngetotp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String smsVerifyId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        edotp = findViewById(R.id.edotp);
        edphone = findViewById(R.id.edphone);
        btngetotp = findViewById(R.id.btngetotp);
        btnloginotp = findViewById(R.id.btnloginotp);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        LottieAnimationView back = findViewById(R.id.back);
        back.setOnClickListener(view -> finish());

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                edotp.setText(phoneAuthCredential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(LoginOtpActivity.this, "Gửi OTP thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                smsVerifyId = verificationId;
                resendToken = token;
                Toast.makeText(LoginOtpActivity.this, "OTP đã được gửi!", Toast.LENGTH_SHORT).show();
            }
        };

        btngetotp.setOnClickListener(view -> {
            String phoneNumber = edphone.getText().toString().trim();
            if (phoneNumber.isEmpty()) {
                Toast.makeText(LoginOtpActivity.this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!phoneNumber.matches("[0-9]+")) {
                Toast.makeText(LoginOtpActivity.this, "Số điện thoại chỉ được chứa các kí tự số", Toast.LENGTH_SHORT).show();
                return;
            }
            sendOTP(phoneNumber);
        });

        btnloginotp.setOnClickListener(view -> {
            String userOTP = edotp.getText().toString();
            if (userOTP.isEmpty()) {
                Toast.makeText(LoginOtpActivity.this, "Vui lòng nhập OTP!", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(userOTP);
        });
    }

    private void sendOTP(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84" + phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp(String code) {
        if (smsVerifyId == null) {
            Toast.makeText(this, "Bạn chưa nhận được mã OTP!", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(smsVerifyId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginOtpActivity.this, "Đăng Nhập Thành Công!", Toast.LENGTH_SHORT).show();
                        String userId = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String role = documentSnapshot.getString("role");
                                        if (role != null && role.equals("Admin")) {
                                            startActivity(new Intent(LoginOtpActivity.this, AdminActivity.class));
                                        } else {
                                            startActivity(new Intent(LoginOtpActivity.this, HomeActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("phone", edphone.getText().toString().trim());
                                        userData.put("role", "Customer");
                                        db.collection("users").document(userId)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    startActivity(new Intent(LoginOtpActivity.this, ProfileActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(LoginOtpActivity.this, "Lỗi khi lưu thông tin!", Toast.LENGTH_SHORT).show();
                                                    mAuth.signOut();
                                                    startActivity(new Intent(LoginOtpActivity.this, ChoiceLoginActivity.class));
                                                    finish();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginOtpActivity.this, "Lỗi khi kiểm tra vai trò!", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    startActivity(new Intent(LoginOtpActivity.this, ChoiceLoginActivity.class));
                                    finish();
                                });
                    } else {
                        edotp.setError("OTP Không Đúng");
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginOtpActivity.this, "Mã OTP không hợp lệ!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}