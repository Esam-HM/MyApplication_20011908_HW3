package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import myClasses.Course;
import myClasses.User;

public class CourseActivity extends AppCompatActivity {

    private User user;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private ImageButton backBtn;
    private TextView emptyTxt;
    private EditText yearTxt;
    private RadioGroup radioGroup;
    private RadioButton yearBtn;
    private CardView filterBtn, resetBtn;
    private int[] checked = {0,0,0};
    private CourseRvAdapter.ItemClickListener itemListener;
    private CourseRvAdapter rvAdapter;
    private ArrayList<Course> courses = new ArrayList<>();
    private ArrayList<String> groups = new ArrayList<>();
    private ArrayList<Course> allCourses = new ArrayList<>();
    private ArrayList<String> allGroups = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbRef = FirebaseDatabase.getInstance().getReference();
        user = (User) getIntent().getSerializableExtra("user");
        setIds();

        if (user.getCourses()==null){
            emptyTxt.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            getData();
            recyclerView.setVisibility(View.VISIBLE);
            emptyTxt.setVisibility(View.GONE);
        }
        if(user.getAccount_type().equals("Instructor")){
            findViewById(R.id.cc_fab_id_ca).setVisibility(View.VISIBLE);
            toCreateCourse();
        }

        filter();
        selectedFilters();
        reset();
        toBack();
    }

    private void reset(){
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checked[0]==1 || checked[1]==1) {
                    int radioButtonCount = radioGroup.getChildCount();
                    for (int i = 0; i < radioButtonCount; i++) {
                        View view = radioGroup.getChildAt(i);
                        if (view instanceof RadioButton) {
                            RadioButton radioButton = (RadioButton) view;
                            radioButton.setChecked(false);
                        }
                    }
                }
                yearBtn.setChecked(false);
                yearTxt.setText("");
                checked[0] = 0;
                checked[1] = 0;
                checked[2] = 0;
                groups.clear();
                courses.clear();
                for (Course crs: allCourses){
                    courses.add(crs);
                    groups.add(user.getCourses().get(crs.getCourseCode()));
                }
                rvAdapter.notifyDataSetChanged();
            }
        });
    }

    private void filter(){
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checked[0]==1){
                    // attending
                    if (checked[2]==1){
                        courses.clear();
                        groups.clear();

                        // with year
                        String year = yearTxt.getText().toString();
                        String sene;
                        if (!year.isEmpty()){
                            for (Course crs: allCourses){
                                sene = crs.getDate().substring(0,4);
                                if (crs.getStatus().equals("Attending") && year.equals(sene)){
                                    courses.add(crs);
                                    groups.add(user.getCourses().get(crs.getCourseCode()));
                                }
                            }
                            rvAdapter.notifyDataSetChanged();
                        }else{
                            yearTxt.setError("Enter year");
                        }
                    }else{
                        courses.clear();
                        groups.clear();
                        // without year
                        Toast.makeText(CourseActivity.this,"girdi2",
                                Toast.LENGTH_SHORT).show();
                        for (Course crs: allCourses){
                            if (crs.getStatus().equals("Attending")){
                                courses.add(crs);
                                groups.add(user.getCourses().get(crs.getCourseCode()));
                            }
                        }
                        rvAdapter.notifyDataSetChanged();
                    }
                } else if (checked[1]==1) {
                    // completed
                    if (checked[2]==1){
                        // with year
                        courses.clear();
                        groups.clear();

                        String year = yearTxt.getText().toString();
                        String sene;
                        if (!year.isEmpty()){
                            for (Course crs: allCourses){
                                sene = crs.getDate().substring(0,4);
                                if (crs.getStatus().equals("Completed") && year.equals(sene)){
                                    courses.add(crs);
                                    groups.add(user.getCourses().get(crs.getCourseCode()));
                                }
                            }
                            rvAdapter.notifyDataSetChanged();
                        }else{
                            yearTxt.setError("Enter year");
                        }
                    }else{
                        courses.clear();
                        groups.clear();
                        // without year

                        for (Course crs: allCourses){
                            if (crs.getStatus().equals("Completed")){
                                courses.add(crs);
                                groups.add(user.getCourses().get(crs.getCourseCode()));
                            }
                        }
                        rvAdapter.notifyDataSetChanged();
                    }
                }else {
                    String year = yearTxt.getText().toString();
                    String sene;
                    if (!year.isEmpty()){
                        courses.clear();
                        groups.clear();

                        for (Course crs: allCourses){
                            sene = crs.getDate().substring(0,4);
                            if (year.equals(sene)){
                                courses.add(crs);
                                groups.add(user.getCourses().get(crs.getCourseCode()));
                            }
                        }
                        rvAdapter.notifyDataSetChanged();
                    }else{
                        yearTxt.setError("Enter year");
                    }
                }
            }
        });
    }

    private void selectedFilters(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedBtn = findViewById(checkedId);

                if (checkedBtn.getText().toString().equals("Completed")){
                    checked[0] = 0;
                    checked[1] = 1;
                }else {
                    checked[0] = 1;
                    checked[1] = 0;
                }
            }
        });

        yearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked[2] = 1;
            }
        });
    }
/*
    private void sortCourses() {
        ArrayList<Course> list = new ArrayList<>(allCourses);
        for (int i = 0; i < allCourses.size(); i++) {
            int max = i;
            for (int j = i + 1; j < allCourses.size(); j++) {
                if (list.get(i).getDate().compareTo(list.get(j).getDate()) < 0) {
                    max = j;
                }
            }
            // Swap elements
            courses.add(list.get(max));
            groups.add(user.getCourses().get(list.get(max).getCourseName()));
            list.get(max).setDate("1000-1-1");
        }
    }

*/


    private void getData(){
        dbRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String ii: user.getCourses().keySet()){
                    allCourses.add(snapshot.child(ii).getValue(Course.class));
                    allGroups.add(user.getCourses().get(ii));
                }
                //Toast.makeText(CourseActivity.this,"size1" + courses.size(),
                //        Toast.LENGTH_SHORT).show();
                Collections.sort(allCourses);
                Collections.reverse(allCourses);
                for (Course crs: allCourses){
                    courses.add(crs);
                    groups.add(user.getCourses().get(crs.getCourseCode()));
                }
                //sortCourses();
                setRecyclerViewAdapter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CourseActivity.this,"Failed in getting data",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRecyclerViewAdapter(){
        rvSetOnItemClickListener();
        rvAdapter = new CourseRvAdapter(courses,groups,itemListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rvAdapter);
    }

    // go to course detail page.
    private void rvSetOnItemClickListener(){
        itemListener = new CourseRvAdapter.ItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                // to course details activity.
                Intent intent = new Intent(CourseActivity.this,CourseDetailActivity.class);
                intent.putExtra("course",courses.get(position));
                intent.putExtra("user",user);
                startActivity(intent);
                //if (user.getAccount_type().equals("Student")){
                    //Toast.makeText(CourseActivity.this,"item clicked" + position,
                    //        Toast.LENGTH_SHORT).show();
                //}else{

                    //Toast.makeText(CourseActivity.this,"item clicked" + position,
                    //                Toast.LENGTH_SHORT).show();
                //}
            }
        };
    }

    private void toCreateCourse() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseActivity.this,CreateCourseActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });
    }

    private void toBack() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourseActivity.this,MenuActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(),MenuActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void setIds() {
        recyclerView = findViewById(R.id.recycleViewId_ca);
        backBtn = findViewById(R.id.backBtn_courses);
        fab = findViewById(R.id.cc_fab_id_ca);
        emptyTxt = findViewById(R.id.emptyListTxtId_ca);
        filterBtn = findViewById(R.id.filterBtnId_ca);
        resetBtn = findViewById(R.id.resetBtnId_ca);
        radioGroup = findViewById(R.id.radioGroupId_ca);
        yearBtn = findViewById(R.id.yearRadioBtnId_ca);
        yearTxt = findViewById(R.id.yearTxtId_ca);
    }

}