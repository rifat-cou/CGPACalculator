package com.example.cgpacalcutator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemesterActivity extends AppCompatActivity {

    private TextView tvSemesterTitle, tvAddCourseHint;
    private RecyclerView recyclerViewCourses;
    private FloatingActionButton btnAddCourse;
    private Button btnShowSGPA;
    private List<Course> courseList;
    private CourseAdapter courseAdapter;
    private FirebaseFirestore db;
    private String semesterName, userId;
    private Map<String, Double> letterToGradePoint = new HashMap<>();
    private Map<Double, String> gradePointToLetter = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester);

        tvSemesterTitle = findViewById(R.id.tvSemesterTitle);
        recyclerViewCourses = findViewById(R.id.recyclerViewCourses);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        tvAddCourseHint = findViewById(R.id.tvAddCourseHint);
        btnShowSGPA = findViewById(R.id.btnShowSGPA);

        letterToGradePoint = new HashMap<>();
        letterToGradePoint.put("A+", 4.0);
        letterToGradePoint.put("A", 3.75);
        letterToGradePoint.put("A-", 3.5);
        letterToGradePoint.put("B+", 3.25);
        letterToGradePoint.put("B", 3.0);
        letterToGradePoint.put("B-", 2.75);
        letterToGradePoint.put("C+", 2.5);
        letterToGradePoint.put("C", 2.25);
        letterToGradePoint.put("D", 2.0);
        letterToGradePoint.put("F", 0.0);

        for (Map.Entry<String, Double> entry : letterToGradePoint.entrySet()) {
            gradePointToLetter.put(entry.getValue(), entry.getKey());
        }

        // Firebase setup
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Semester Name from Intent
        semesterName = getIntent().getStringExtra("semesterName");
        if (semesterName != null) {
            tvSemesterTitle.setText(semesterName);
        }

        // Set RecyclerView
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));
        courseList = new ArrayList<>();
        courseAdapter = new CourseAdapter(courseList);
        recyclerViewCourses.setAdapter(courseAdapter);


        // Load Courses from Firebase
        loadCourses();

        // Add Course Button Click
        btnAddCourse.setOnClickListener(v -> showAddCourseDialog());

        // SGPA Button Click
        btnShowSGPA.setOnClickListener(v -> {
            Toast.makeText(this, "Fetching SGPA...", Toast.LENGTH_SHORT).show();
            String selectedSemesterName = tvSemesterTitle.getText().toString().trim(); // Get actual text
            Intent intent = new Intent(SemesterActivity.this, SGPAActivity.class);
            intent.putExtra("semesterName", selectedSemesterName); // Pass correct semester name
            Log.d("SemesterActivity", "Launching SGPAActivity with semester: " + selectedSemesterName);
            startActivity(intent);
        });
    }
    public void showEditDialog(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Course");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        EditText editTitle = view.findViewById(R.id.etCourseTitle);
        EditText editCode = view.findViewById(R.id.etCourseCode);
        EditText editCredit = view.findViewById(R.id.etCourseCredit);

        editTitle.setText(course.getCourseTitle());
        editCode.setText(course.getCourseCode());
        editCredit.setText(String.valueOf(course.getCourseCredit()));

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = editTitle.getText().toString().trim();
            String newCode = editCode.getText().toString().trim();
            double newCredit = Double.parseDouble(editCredit.getText().toString().trim());

            db.collection("Courses").document(course.getCourseCode())
                    .update("title", newTitle, "code", newCode, "credit", newCredit)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Course Updated!", Toast.LENGTH_SHORT).show();
                        loadCourses();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show());
        });
    }
    // 🔴 **DELETE COURSE**
    public void deleteCourse(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Course");
        builder.setMessage("Are you sure you want to delete this course?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            db.collection("Courses").document(course.getCourseCode())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Course Deleted!", Toast.LENGTH_SHORT).show();
                        loadCourses();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Delete Failed!", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_course, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Get references from dialog layout
        EditText etCourseCode = view.findViewById(R.id.etCourseCode);
        AutoCompleteTextView etCourseTitle = view.findViewById(R.id.etCourseTitle);
        EditText etCourseCredit = view.findViewById(R.id.etCourseCredit);
        Spinner spLetterGrade = view.findViewById(R.id.spLetterGrade);
        TextView tvGradePoint = view.findViewById(R.id.tvGradePoint);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        // Set up the spinner with letter grade options
        List<String> letterOptions = Arrays.asList("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "D", "F");
        ArrayAdapter<String> letterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, letterOptions);
        letterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLetterGrade.setAdapter(letterAdapter);

        // Predefined conversion mapping (initialize these maps in your activity, e.g., in onCreate())
        // Example:
        // letterToGradePoint.put("A+", 4.0);
        // letterToGradePoint.put("A", 4.0);
        // letterToGradePoint.put("A-", 3.7);
        // ... and so on.

        spLetterGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLetter = parent.getItemAtPosition(position).toString();
                if(letterToGradePoint.containsKey(selectedLetter)) {
                    double gp = letterToGradePoint.get(selectedLetter);
                    tvGradePoint.setText(String.valueOf(gp));
                } else {
                    tvGradePoint.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String courseCode = etCourseCode.getText().toString().trim();
            String courseTitle = etCourseTitle.getText().toString().trim();
            String courseCreditStr = etCourseCredit.getText().toString().trim();
            String selectedLetter = spLetterGrade.getSelectedItem().toString();
            String gradePointStr = tvGradePoint.getText().toString().trim();
            double finalGradePoint = Double.parseDouble(tvGradePoint.getText().toString());

            if(courseCode.isEmpty() || courseTitle.isEmpty() || courseCreditStr.isEmpty()) {
                Toast.makeText(SemesterActivity.this, "Course Code, Title, and Credit are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double courseCredit;
            try {
                courseCredit = Double.parseDouble(courseCreditStr);
            } catch (NumberFormatException e) {
                Toast.makeText(SemesterActivity.this, "Invalid course credit", Toast.LENGTH_SHORT).show();
                return;
            }

            //double finalGradePoint = Double.parseDouble(gradePointStr);

            // Create a Course object (make sure your Course class includes grade fields)
            Course course = new Course(courseCode, courseTitle, courseCredit, selectedLetter, finalGradePoint);

            // Save course to user's course list and update global courses as before
            db.collection("Users").document(userId)
                    .collection("Semesters").document(semesterName)
                    .collection("Courses").document(courseCode)
                    .set(course)
                    .addOnSuccessListener(aVoid -> {
                        courseList.add(course);
                        courseAdapter.notifyDataSetChanged();
                        Toast.makeText(SemesterActivity.this, "Course Added", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SemesterActivity.this, "Error adding course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }



    private void loadCourses() {
        db.collection("Users").document(userId)
                .collection("Semesters").document(semesterName)
                .collection("Courses")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    courseList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        courseList.add(doc.toObject(Course.class));
                    }
                    courseAdapter.notifyDataSetChanged();
                });
    }

}
