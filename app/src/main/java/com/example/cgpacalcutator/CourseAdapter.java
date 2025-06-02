package com.example.cgpacalcutator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;

    public CourseAdapter(List<Course> courseList) {
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.tvCourseCode.setText("Course Code: " + course.getCourseCode());
        holder.tvCourseTitle.setText("Course Title: " + course.getCourseTitle());
        holder.tvCourseCredit.setText("Credit: " + course.getCourseCredit());
        holder.tvLetterGrade.setText(course.getLetterGrade());
        holder.tvGradePoint.setText(String.valueOf(course.getGradePoint()));

    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseCode, tvCourseTitle, tvCourseCredit, tvLetterGrade, tvGradePoint;
        ImageButton btnEdit, btnDelete;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvCourseCredit = itemView.findViewById(R.id.tvCourseCredit);
            tvLetterGrade = itemView.findViewById(R.id.tvLetterGrade);
            tvGradePoint = itemView.findViewById(R.id.tvGradePoint);
        }
    }
}
