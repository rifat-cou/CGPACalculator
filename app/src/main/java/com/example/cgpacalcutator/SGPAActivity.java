package com.example.cgpacalcutator;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SGPAActivity extends AppCompatActivity {

    private ImageView ivAppLogo, ivDeptLogo;
    private TextView tvAppName, tvDeptName, tvStudentName, tvStudentId, tvRegNo, tvSession, tvDOB;
    private TextView tvSemesterResultTitle, tvSGPA;
    private RecyclerView rvCourseList;
    private CourseAdapter courseAdapter;
    private List<Course> courseList = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId, semesterName;

    // Predefined mapping to convert SGPA numeric to letter grade (example mapping)
    private String convertToLetterGrade(double sgpa) {
        if (sgpa == 4.0) return "A+";
        else if (sgpa >= 3.75) return "A";
        else if (sgpa >= 3.5) return "A-";
        else if (sgpa >= 3.25) return "B+";
        else if (sgpa >= 3.0) return "B";
        else if (sgpa >= 2.75) return "B-";
        else if (sgpa >= 2.5) return "C+";
        else if (sgpa >= 2.25) return "C";
        else if (sgpa >= 2.0) return "D";
        else return "F";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sgpa);

        // Initialize views
        ivAppLogo = findViewById(R.id.ivAppLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvDeptName = findViewById(R.id.tvDeptName);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvRegNo = findViewById(R.id.tvRegNo);
        tvSession = findViewById(R.id.tvSession);
        tvDOB = findViewById(R.id.tvDOB);
        tvSemesterResultTitle = findViewById(R.id.tvSemesterResultTitle);
        tvSGPA = findViewById(R.id.tvSGPA);
        rvCourseList = findViewById(R.id.rvCourseList);

        // Initialize Firestore and userId (assumes user is authenticated)
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Assume semesterName is passed via Intent
        semesterName = getIntent().getStringExtra("semesterName");
        if (semesterName == null) {
            semesterName = "First Year First Semester"; // default
        }
        tvSemesterResultTitle.setText(semesterName + " Result");

        // Set up RecyclerView
        rvCourseList.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(courseList);
        rvCourseList.setAdapter(courseAdapter);

        // Load student details and course list from Firestore
        loadStudentDetails();
        loadCoursesAndCalculateSGPA(semesterName);
    }

    private void loadStudentDetails() {
        // Fetch student details from Firestore
        db.collection("Users").document(userId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String studentID = documentSnapshot.getString("studentID");
                        String regNo = documentSnapshot.getString("regNo");
                        String session = documentSnapshot.getString("session");
                        String dob = documentSnapshot.getString("dob");

                        tvStudentName.setText("Name: " + (name != null ? name : "N/A"));
                        tvStudentId.setText("Student ID: " + (studentID != null ? studentID : "N/A"));
                        tvRegNo.setText("Registration No: " + (regNo != null ? regNo : "N/A"));
                        tvSession.setText("Session: " + (session != null ? session : "N/A"));
                        tvDOB.setText("Date of Birth: " + (dob != null ? dob : "N/A"));
                    }
                });
    }

    private void loadCoursesAndCalculateSGPA(String semesterName) {
        // Clear the current list to avoid duplications on reload.
        courseList.clear();
        String path = "Users/" + userId + "/Semesters/" + semesterName + "/Courses";
        Log.d("SGPAActivity", "Loading courses from: " + path);

        db.collection("Users").document(userId)
                .collection("Semesters").document(semesterName)
                .collection("Courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalPoints = 0;
                    double totalCredits = 0;

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Course course = doc.toObject(Course.class);
                            if (course != null) {
                                // Log course details for debugging.
                                Log.d("SGPAActivity", "Fetched Course: " + course.getCourseCode() +
                                        ", Credit: " + course.getCourseCredit() +
                                        ", Grade Point: " + course.getGradePoint());

                                courseList.add(course);
                                totalPoints += course.getGradePoint() * course.getCourseCredit();
                                totalCredits += course.getCourseCredit();
                            } else {
                                Log.w("SGPAActivity", "Null course found in document: " + doc.getId());
                            }
                        }
                    } else {
                        Log.w("SGPAActivity", "No courses found for semester: " + semesterName);
                    }

                    courseAdapter.notifyDataSetChanged();

                    if (totalCredits > 0) {
                        double gpa = totalPoints / totalCredits;
                        String letterGrade = convertToLetterGrade(gpa);
                        double sgpa = Math.round(gpa * 100.0) / 100.0;
                        tvSGPA.setText("Your Obtained SGPA: " + String.format("%.2f", sgpa) + " (" + letterGrade + ")");
                        Log.d("SGPAActivity", "Calculated SGPA: " + sgpa + ", Total Credits: " + totalCredits);

                        // Prepare data to update in Firestore for this semester.
                        Map<String, Object> semesterResultData = new HashMap<>();
                        semesterResultData.put("sgpa", sgpa);
                        semesterResultData.put("letterGrade", letterGrade);
                        semesterResultData.put("totalCredits", totalCredits);

                        // Update the semester document in Firestore using merge option.
                        db.collection("Users").document(userId)
                                .collection("Semesters").document(semesterName)
                                .set(semesterResultData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("SGPAActivity", "Semester result updated in Firestore successfully.");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SGPAActivity", "Error updating semester result in Firestore", e);
                                });
                    } else {
                        tvSGPA.setText("SGPA: N/A");
                        Log.w("SGPAActivity", "Total credits is zero; cannot calculate SGPA");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SGPAActivity", "Error loading courses: ", e);
                });
    }
}
