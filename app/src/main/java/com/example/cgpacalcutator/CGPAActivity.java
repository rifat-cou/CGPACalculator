package com.example.cgpacalcutator;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CGPAActivity extends AppCompatActivity {

    // Header Views
    private ImageView ivUniversityLogo, ivDeptLogo;
    private TextView tvUniversityName, tvDeptName;

    // Student Details Views
    private TextView tvStudentName, tvStudentId, tvRegNo, tvSession, tvDOB;

    // Section Title & Overall CGPA
    private TextView tvSectionTitle, tvOverallCGPA;

    // RecyclerView for Semester Results
    private RecyclerView rvSemesterResults;
    private SemesterResultAdapter semesterResultAdapter;
    private List<SemesterResult> semesterResultsList = new ArrayList<>();

    // Firebase
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cgpa);

        // Initialize views
        ivUniversityLogo = findViewById(R.id.ivAppLogo);
        tvUniversityName = findViewById(R.id.tvAppName);
        tvDeptName = findViewById(R.id.tvDeptName);

        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvRegNo = findViewById(R.id.tvRegNo);
        tvSession = findViewById(R.id.tvSession);
        tvDOB = findViewById(R.id.tvDOB);

        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        rvSemesterResults = findViewById(R.id.rvSemesterResults);
        tvOverallCGPA = findViewById(R.id.tvOverallCGPA);

        // Set static header texts (logos should be set via drawable resources)
        //tvUniversityName.setText("Comilla University");
        //tvDeptName.setText("Department of Information &amp; Communication Technology");
        //tvSectionTitle.setText("Semester Wise Result");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Setup RecyclerView
        rvSemesterResults.setLayoutManager(new LinearLayoutManager(this));
        semesterResultAdapter = new SemesterResultAdapter(semesterResultsList);
        rvSemesterResults.setAdapter(semesterResultAdapter);

        // Load student details and semester results
        loadStudentDetails();
        loadSemesterResults();

    }

    // Load student details from /Users/{userId}
    private void loadStudentDetails() {
        db.collection("Users").document(userId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        String name = documentSnapshot.getString("name");
                        String studentId = documentSnapshot.getString("studentID");
                        String regNo = documentSnapshot.getString("regNo");
                        String session = documentSnapshot.getString("session");
                        String dob = documentSnapshot.getString("dob");

                        tvStudentName.setText("Name: " + (name != null ? name : "N/A"));
                        tvStudentId.setText("ID No.: " + (studentId != null ? studentId : "N/A"));
                        tvRegNo.setText("Registration No.: " + (regNo != null ? regNo : "N/A"));
                        tvSession.setText("Session: " + (session != null ? session : "N/A"));
                        tvDOB.setText("Date of Birth: " + (dob != null ? dob : "N/A"));
                    } else {
                        tvStudentName.setText("Student details not found.");
                    }
                }).addOnFailureListener(e -> {
                    Log.e("CGPAActivity", "Error loading student details", e);
                });
    }

    // Load semester results from /Users/{userId}/Semesters and calculate overall CGPA.
    private void loadSemesterResults() {
        CollectionReference semestersRef = db.collection("Users").document(userId).collection("Semesters");
        semestersRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            semesterResultsList.clear();
            double sumWeightedSgpa = 0;
            double sumCredits = 0;
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    // Assume each semester document ID is the semester name.
                    // Each document should have fields: sgpa (Number), letterGrade (String), totalCredits (Number)
                    String semesterName = doc.getId();
                    Double sgpa = doc.getDouble("sgpa");
                    String letterGrade = doc.getString("letterGrade");
                    Double totalCredits = doc.getDouble("totalCredits");
                    Long order = doc.contains("order") ? doc.getLong("order") : null;
                    if(sgpa != null && totalCredits != null) {
                        SemesterResult result = new SemesterResult(semesterName, sgpa, letterGrade, totalCredits, order);
                        semesterResultsList.add(result);
                        sumWeightedSgpa += sgpa * totalCredits;
                        sumCredits += totalCredits;
                    }
                }
            } else {
                Log.w("CGPAActivity", "No semester results found.");
            }

            // Sort results: if order field is available, sort by it; otherwise, sort by semester name
            Collections.sort(semesterResultsList, new Comparator<SemesterResult>() {
                @Override
                public int compare(SemesterResult s1, SemesterResult s2) {
                    if (s1.getOrder() != null && s2.getOrder() != null) {
                        return s1.getOrder().compareTo(s2.getOrder());
                    }
                    return s1.getSemesterName().compareTo(s2.getSemesterName());
                }
            });
            semesterResultAdapter.notifyDataSetChanged();

            if(sumCredits > 0) {
                double cgpa = sumWeightedSgpa / sumCredits;
                String overallLetterGrade = convertToLetterGrade(cgpa);
                tvOverallCGPA.setText("Your Obtained CGPA: " + String.format("%.2f", cgpa) + " (" + overallLetterGrade + ")");
            } else {
                tvOverallCGPA.setText("CGPA: N/A");
            }
        }).addOnFailureListener(e -> {
            Log.e("CGPAActivity", "Error loading semester results", e);
        });
    }


    // Convert numeric grade to a letter grade (adjust thresholds as needed)
    private String convertToLetterGrade(double grade) {
        if (grade == 4.0) return "A+";
        else if (grade >= 3.75) return "A";
        else if (grade >= 3.5) return "A-";
        else if (grade >= 3.25) return "B+";
        else if (grade >= 3.0) return "B";
        else if (grade >= 2.75) return "B-";
        else if (grade >= 2.5) return "C+";
        else if (grade >= 2.25) return "C";
        else if (grade >= 2.0) return "D";
        else return "F";
    }

    // Model class for a semester result
    public static class SemesterResult {
        private String semesterName;
        private double sgpa;
        private String letterGrade;
        private double totalCredits;
        private Long order; // Optional for ordering

        public SemesterResult() { }

        public SemesterResult(String semesterName, double sgpa, String letterGrade, double totalCredits, Long order) {
            this.semesterName = semesterName;
            this.sgpa = sgpa;
            this.letterGrade = letterGrade;
            this.totalCredits = totalCredits;
            this.order = order;
        }

        public String getSemesterName() { return semesterName; }
        public double getSgpa() { return sgpa; }
        public String getLetterGrade() { return letterGrade; }
        public double getTotalCredits() { return totalCredits; }
        public Long getOrder() { return order; }
    }

    // RecyclerView Adapter for semester results
    public class SemesterResultAdapter extends RecyclerView.Adapter<SemesterResultAdapter.SemesterResultViewHolder> {
        private List<SemesterResult> results;

        public SemesterResultAdapter(List<SemesterResult> results) {
            this.results = results;
        }

        @NonNull
        @Override
        public SemesterResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_semester_result, parent, false);
            return new SemesterResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SemesterResultViewHolder holder, int position) {
            SemesterResult result = results.get(position);
            holder.tvSemesterName.setText(result.getSemesterName());
            holder.tvSemesterLetterGrade.setText(result.getLetterGrade());
            holder.tvSemesterGradePoint.setText(String.valueOf(result.getSgpa()));
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        public class SemesterResultViewHolder extends RecyclerView.ViewHolder {
            TextView tvSemesterName, tvSemesterLetterGrade, tvSemesterGradePoint;

            public SemesterResultViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSemesterName = itemView.findViewById(R.id.tvSemesterName);
                tvSemesterLetterGrade = itemView.findViewById(R.id.tvSemesterLetterGrade);
                tvSemesterGradePoint = itemView.findViewById(R.id.tvSemesterGradePoint);
            }
        }
    }
}
