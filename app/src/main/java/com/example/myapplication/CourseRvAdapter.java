package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import myClasses.Course;

public class CourseRvAdapter extends RecyclerView.Adapter<CourseRvAdapter.ViewHolder> {

    private ArrayList<Course> data;
    private ArrayList<String> groups;
    private ItemClickListener listener;

    public CourseRvAdapter(ArrayList<Course> data, ArrayList<String> groups, ItemClickListener listener) {
        this.data = data;
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseRvAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_rv_single_item_layout,parent,false);
        return new CourseRvAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseRvAdapter.ViewHolder holder, int position) {
        String str = data.get(position).getCourseCode() + " - Gr: " + groups.get(position);
        holder.codeGroupTxt.setText(str);
        holder.nameTxt.setText(data.get(position).getCourseName());
        holder.statusTxt.setText(data.get(position).getStatus());
        holder.semesterTxt.setText(data.get(position).getSemester());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface ItemClickListener{
        void onClick(View v,int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView codeGroupTxt, nameTxt,statusTxt, semesterTxt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            codeGroupTxt = itemView.findViewById(R.id.class_code_id);
            nameTxt = itemView.findViewById(R.id.class_name_id);
            statusTxt = itemView.findViewById(R.id.class_status_id);
            semesterTxt = itemView.findViewById(R.id.class_date_id);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            listener.onClick(itemView,getAdapterPosition());
        }
    }
}
