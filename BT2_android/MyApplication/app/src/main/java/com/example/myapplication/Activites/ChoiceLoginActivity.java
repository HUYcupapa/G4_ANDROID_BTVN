package com.example.myapplication.Activites;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;

public class ChoiceLoginActivity extends AppCompatActivity {
    private Button btnemail,btnOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_login);

        btnemail = findViewById(R.id.btnemail);
        btnOtp = findViewById(R.id.btnOtp);

        btnemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(ChoiceLoginActivity.this, LoginActivity.class);
                startActivity(in);
            }
        });
        btnOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(ChoiceLoginActivity.this, LoginOtpActivity.class);
                startActivity(in);
            }
        });
    }
}