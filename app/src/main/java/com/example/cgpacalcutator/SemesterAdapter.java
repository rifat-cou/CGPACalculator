package com.example.cgpacalcutator;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder> {

    private List<String> semesterList;
    private Context context;

    public SemesterAdapter(List<String> semesterList, Context context) {
        this.semesterList = semesterList;
        this.context = context;
    }

    @NonNull
    @Override
    public SemesterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_semester, parent, false);
        return new SemesterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SemesterViewHolder holder, int position) {
        String semesterName = semesterList.get(position);
        holder.btnSemester.setText(semesterName);

        holder.btnSemester.setOnClickListener(v -> {
            Intent intent = new Intent(context, SemesterActivity.class);
            intent.putExtra("semesterName", semesterName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return semesterList.size();
    }

    public static class SemesterViewHolder extends RecyclerView.ViewHolder {
        Button btnSemester;

        public SemesterViewHolder(@NonNull View itemView) {
            super(itemView);
            btnSemester = itemView.findViewById(R.id.btnSemester);
        }
    }
}
