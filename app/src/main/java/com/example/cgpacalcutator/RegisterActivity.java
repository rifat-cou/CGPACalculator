package com.example.cgpacalcutator;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText etName, etStudentID, etRegNo, etSession, etDob, etEmail, etPassword, etConfirmPassword;
    private ImageView togglePassword, toggleConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false, isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI Elements
        etName = findViewById(R.id.etName);
        etStudentID = findViewById(R.id.etStudentID);
        etRegNo = findViewById(R.id.etRegNo);
        etSession = findViewById(R.id.etSession);
        etDob = findViewById(R.id.etDob);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        togglePassword = findViewById(R.id.togglePassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Date Picker for Date of Birth
        etDob.setOnClickListener(view -> showDatePicker());

        // Toggle password visibility
        togglePassword.setOnClickListener(view -> togglePasswordVisibility(etPassword, togglePassword));
        toggleConfirmPassword.setOnClickListener(view -> togglePasswordVisibility(etConfirmPassword, toggleConfirmPassword));

        // Register Button Click
        btnRegister.setOnClickListener(view -> registerUser());
    }

    // Method to show Date Picker Dialog
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String dob = yearSelected + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    etDob.setText(dob);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    // Toggle Password Visibility
    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        } else {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        }
        passwordField.setSelection(passwordField.getText().length());
    }

    // Register User in Firebase
    private void registerUser() {
        String name = etName.getText().toString().trim();
        String studentID = etStudentID.getText().toString().trim();
        String regNo = etRegNo.getText().toString().trim();
        String session = etSession.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate Inputs
        if (name.isEmpty() || studentID.isEmpty() || regNo.isEmpty() || session.isEmpty() ||
                dob.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Save additional details in Firestore
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    user.sendEmailVerification()
                            .addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Verification email sent!", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                                }
                            });
                }
                saveUserData(task.getResult().getUser().getUid(), name, studentID, regNo, session, dob, email);
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Save User Data in Firestore
    private void saveUserData(String uid, String name, String studentID, String regNo, String session, String dob, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", etName.getText().toString().trim());
        userData.put("studentID", etStudentID.getText().toString().trim());
        userData.put("regNo", etRegNo.getText().toString().trim());
        userData.put("session", etSession.getText().toString().trim());
        userData.put("dob", etDob.getText().toString().trim());
        userData.put("email", etEmail.getText().toString().trim());

// Use the authenticated user’s UID as document ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("RegisterActivity", "Error saving user data", e);
                });

    }

}
