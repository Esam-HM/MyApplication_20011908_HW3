package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InstructorListAdapter extends RecyclerView.Adapter<InstructorListAdapter.ViewHolder> {

    private ArrayList<String> instructors;
    private ItemClickListener itemClickListener;

    public InstructorListAdapter(ArrayList<String> instructors, ItemClickListener itemClickListener) {
        this.instructors = instructors;
        this.itemClickListener = itemClickListener;
    }

    public InstructorListAdapter(ArrayList<String> instructors) {
        this.instructors = instructors;
    }

    @NonNull
    @Override
    public InstructorListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.instructor_rv_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructorListAdapter.ViewHolder holder, int position) {
        holder.txt.setText(instructors.get(position));
    }

    @Override
    public int getItemCount() {
        return instructors.size();
    }

    public interface ItemClickListener{
        void onClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView txt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.instructorTxtId);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(itemView,getAdapterPosition());
        }
    }
}
