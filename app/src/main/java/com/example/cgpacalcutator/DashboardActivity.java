package com.example.cgpacalcutator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSemesters;
    private Button btnShowCGPA;
    private List<String> semesterList;
    private SemesterAdapter semesterAdapter; // Adapter for RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        recyclerViewSemesters = findViewById(R.id.recyclerViewSemesters);
        btnShowCGPA = findViewById(R.id.btnShowCGPA);

        // Initialize semester list
        semesterList = Arrays.asList(
                "First Year First Semester", "First Year Second Semester",
                "Second Year First Semester", "Second Year Second Semester",
                "Third Year First Semester", "Third Year Second Semester",
                "Fourth Year First Semester", "Fourth Year Second Semester"
        );

        // Set RecyclerView
        recyclerViewSemesters.setLayoutManager(new LinearLayoutManager(this));
        semesterAdapter = new SemesterAdapter(semesterList, this);
        recyclerViewSemesters.setAdapter(semesterAdapter);

        // Show CGPA Button Click
        btnShowCGPA.setOnClickListener(v -> {
            Toast.makeText(this, "Fetching CGPA...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DashboardActivity.this, CGPAActivity.class);
            startActivity(intent);
        });


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();  // Ensure DashboardActivity is removed from back stack
    }


}
