package com.example.drstethowire;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class PatientReportActivity extends AppCompatActivity {

    private EditText etPatientName, etPatientAge, etPatientAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_report);

        etPatientName = findViewById(R.id.etPatientName);
        etPatientAge = findViewById(R.id.etPatientAge);
        etPatientAddress = findViewById(R.id.etPatientAddress);
        Button btnGenerateReport = findViewById(R.id.btnGenerateReport);

        btnGenerateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport();
            }
        });
    }

    private void generateReport() {
        String patientName = etPatientName.getText().toString();
        String patientAge = etPatientAge.getText().toString();
        String patientAddress = etPatientAddress.getText().toString();

        // Check if patient details are valid
        if (patientName.isEmpty() || patientAge.isEmpty() || patientAddress.isEmpty()) {
            Toast.makeText(this, "Please fill in all patient details", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass patient details to MainActivity2
        Intent intent = new Intent(PatientReportActivity.this, MainActivity2.class);
        intent.putExtra("patientName", patientName);
        intent.putExtra("patientAge", patientAge);
        intent.putExtra("patientAddress", patientAddress);
        startActivity(intent);
    }
}
