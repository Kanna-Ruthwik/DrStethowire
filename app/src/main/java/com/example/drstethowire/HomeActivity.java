package com.example.drstethowire;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    private Button digitalStethoscopeButton;
    private Button patientReportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find buttons by their IDs
        digitalStethoscopeButton = findViewById(R.id.button);
        patientReportButton = findViewById(R.id.report_btn);

        // Set click listeners for buttons
        digitalStethoscopeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        patientReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PatientReportActivity.class);
                startActivity(intent);
            }
        });
    }
}
