package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import myClasses.Course;
import myClasses.Group;
import myClasses.User;

public class CreateCourseActivity extends AppCompatActivity {

    private EditText courseNameTxt, courseCodeTxt, groupNoTxt;
    private AutoCompleteTextView actv;
    private ImageButton backBtn;
    private Button createCourseBtn;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private DatabaseReference dbRef;
    private InstructorListAdapter adapter;
    private InstructorListAdapter.ItemClickListener rvItemListener;
    private ArrayAdapter<String> adapterItems;
    private ArrayList<String> dropDownMenuItems = new ArrayList<>();
    private ArrayList<String> selectedInsts = new ArrayList<>();
    private ArrayList<String> selectedInstsIds = new ArrayList<>();
    private ArrayList<String> instructorsIds = new ArrayList<>();
    private String[] inputs = new String[3];
    private ArrayList<Group> groups = new ArrayList<>();
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setIds();
        progressBar.setVisibility(View.VISIBLE);

        dbRef = FirebaseDatabase.getInstance().getReference();
        user = (User) getIntent().getSerializableExtra("user");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedInsts.add(user.getName() + " " + user.getSurname());
        selectedInstsIds.add(user.getId());


        getInstructors();
        setDropDownMenuAdapter();
        setRecyclerViewAdapter();

        progressBar.setVisibility(View.GONE);

        toBack();
        createCourse();
        selectInstructors();

    }
    private void createCourse() {
        createCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                getInputs();
                if (checkNotEmpty()){
                    int n = Integer.parseInt(inputs[2]);
                    int i;
                    Course course = new Course(inputs[0], inputs[1], inputs[2], user.getId());
                    getCourseDate(course);
                    // initialize groups
                    for (i=0;i<n;i++){
                        String num = String.valueOf(i+1);
                        groups.add(new Group(num));
                    }
                    // assign instructors to groups of courses.
                    if (selectedInsts.isEmpty()){
                        // if no selection, course creator is instructor for first group.
                        groups.get(0).setInstructorId(user.getId());
                        if (user.getCourses() == null){
                            user.setCourses(new HashMap<>());
                        }
                        // write info to database.
                        user.getCourses().put(course.getCourseCode(),groups.get(0).getGroupNo());
                        dbRef.child("usersInfo").child("instructors").child(user.getId()).setValue(user);

                        course.setGroups(groups);
                        dbRef.child("courses").child(course.getCourseCode()).setValue(course);
                        Toast.makeText(getApplicationContext(), "Course Created Successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(CreateCourseActivity.this,CourseActivity.class);
                        intent.putExtra("user",user);
                        startActivity(intent);
                    }else {
                       n = selectedInsts.size();
                       if (n>Integer.parseInt(inputs[2])){
                           Toast.makeText(CreateCourseActivity.this,"too much instructors",
                                   Toast.LENGTH_SHORT).show();
                           progressBar.setVisibility(View.GONE);
                       }else{
                           for (i=0;i<n;i++){
                               groups.get(i).setInstructorId(selectedInstsIds.get(i));
                               dbRef.child("usersInfo").child("instructors").child(selectedInstsIds.get(i))
                                       .child("courses").child(course.getCourseCode()).setValue(groups.get(i).getGroupNo());
                               if (user.getId().equals(selectedInstsIds.get(i))){
                                   if (user.getCourses() ==null){
                                       user.setCourses(new HashMap<>());
                                   }
                                   user.getCourses().put(course.getCourseCode(),groups.get(i).getGroupNo());
                               }
                           }
                           course.setGroups(groups);
                           dbRef.child("courses").child(course.getCourseCode()).setValue(course);
                           Toast.makeText(getApplicationContext(), "Course Created Successfully", Toast.LENGTH_SHORT).show();
                           progressBar.setVisibility(View.GONE);
                           Intent intent = new Intent(CreateCourseActivity.this,CourseActivity.class);
                           intent.putExtra("user",user);
                           startActivity(intent);

                           //Toast.makeText(CreateCourseActivity.this, user.getCourses().get(course.getCourseCode()),
                           //        Toast.LENGTH_SHORT).show();
                       }

                    }


                }else{
                    // empty input
                    progressBar.setVisibility(View.GONE);
                }


            }
        });
    }

    // get current date
    private void getCourseDate(Course course){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String str = String.valueOf(year);
        if (3<month && month<7){
            str += "-Spring Semester";
        }else{
            str += "-Fall Semester";
        }
        // set semester.
        course.setSemester(str);

        // set Date
        str = year + "-" + month + "-" + day;
        course.setDate(str);
    }

    private void selectInstructors(){
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedInsts.add(selectedItem);
                selectedInstsIds.add(instructorsIds.get(position));
                adapter.notifyDataSetChanged();
                //Toast.makeText(getApplicationContext(), "Selected: " + selectedItem,
                //        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getInstructors() {
        dbRef.child("usersInfo").child("instructors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                String name = "";
                String surname = "";
                while(iterator.hasNext()){
                    DataSnapshot node = iterator.next();
                    name = node.child("name").getValue(String.class);
                    surname = node.child("surname").getValue(String.class);
                    dropDownMenuItems.add(name + " " + surname);
                    instructorsIds.add(node.child("id").getValue(String.class));
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateCourseActivity.this, "Fail to get instructors data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRecyclerViewAdapter(){
        rvSetOnItemClickListener();
        adapter = new InstructorListAdapter(selectedInsts,rvItemListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    // delete selected instructor
    private void rvSetOnItemClickListener(){
        rvItemListener = new InstructorListAdapter.ItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                recyclerView.removeViewAt(position);
                selectedInsts.remove(position);
                selectedInstsIds.remove(position);
                adapter.notifyItemRemoved(position);
            }
        };
    }

    private void setDropDownMenuAdapter(){
        adapterItems = new ArrayAdapter<String>(this,
                R.layout.drop_down_item_layout, dropDownMenuItems);
        actv.setAdapter(adapterItems);
    }

    private void getInputs(){
        inputs[0] = courseNameTxt.getText().toString();
        inputs[1] = courseCodeTxt.getText().toString();
        inputs[2] = groupNoTxt.getText().toString();
    }

    private boolean checkNotEmpty(){
        int flag = 0;

        if (inputs[0].isEmpty()){
            courseNameTxt.setError("This field must be filled");
            flag = 1;
        }

        if (inputs[1].isEmpty()){
            courseCodeTxt.setError("This field must be filled");
            flag = 1;
        }

        if (inputs[2].isEmpty()){
            groupNoTxt.setError("This field must be filled");
            flag = 1;
        }
        return flag==0;
    }

    private void setIds(){
        courseNameTxt = findViewById(R.id.courseNameId_cc);
        courseCodeTxt = findViewById(R.id.courseCodeId_cc);
        groupNoTxt = findViewById(R.id.grupNumId_cc);
        backBtn = findViewById(R.id.backBtn_cca);
        actv = findViewById(R.id.autoCompleteTextView_cc);
        recyclerView = findViewById(R.id.instructorList_cca);
        createCourseBtn = findViewById(R.id.createCourseBtn_cca);
        progressBar = findViewById(R.id.progressBar_cca);
    }

    private void toBack() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateCourseActivity.this,CourseActivity.class);
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
}