package com.example.cgpacalcutator;

public class Course {
    private String courseCode;
    private String courseTitle;
    private double courseCredit;
    private String letterGrade;
    private double gradePoint;

    // Empty constructor for Firebase
    public Course() {}

    public Course(String courseCode, String courseTitle, double courseCredit, String letterGrade, double gradePoint) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.courseCredit = courseCredit;
        this.letterGrade = letterGrade;
        this.gradePoint = gradePoint;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public double getCourseCredit() {
        return courseCredit;
    }
    public String getLetterGrade() { return letterGrade; }
    public double getGradePoint() { return gradePoint; }

    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }
    public void setGradePoint(double gradePoint) { this.gradePoint = gradePoint; }
}
