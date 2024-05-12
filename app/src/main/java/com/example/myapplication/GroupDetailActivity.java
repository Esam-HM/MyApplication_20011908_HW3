package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import myClasses.Course;
import myClasses.Group;
import myClasses.User;

public class GroupDetailActivity extends AppCompatActivity {
    private TextView groupNoTxt, instNameTxt, stdNumTxt;
    private CardView addStdBtn, deleteStdBtn, uploadBtn;
    private EditText addStdIdTxt, deleteStdIdTxt;
    private ImageButton backBtn;
    private RecyclerView stdRv;
    private User user;
    private Course course;
    private Group group;
    private String groupNo, instName;
    private int grNo;
    private DatabaseReference dbRef;
    private InstructorListAdapter adapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbRef = FirebaseDatabase.getInstance().getReference();
        user = (User) getIntent().getSerializableExtra("user");
        course = (Course) getIntent().getSerializableExtra("course");
        groupNo = getIntent().getStringExtra("groupNo");
        setIDs();
        initializeScreen();
        defineLaunch();

        addStd();
        deleteStd();
        uploadCSV();

        toBack();
    }

    private void defineLaunch(){
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri uri = data.getData();
                                String line = "";
                                String splitBy = ",";
                                String index = String.valueOf(grNo-1);
                                int i;
                                boolean flag = false;
                                try
                                {
                                    InputStream inputStream = getContentResolver().openInputStream(uri);
                                    //parsing a CSV file into BufferedReader class constructor
                                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                                    if (group.getStudents() ==null){
                                        group.setStudents(new ArrayList<>());
                                        flag=true;
                                    }
                                    i= group.getStudents().size();
                                    while ((line = br.readLine()) != null)   //returns a Boolean value
                                    {
                                        String[] arr = line.split(splitBy);    // use comma as separator
                                        //Toast.makeText(GroupDetailActivity.this,arr[0] + " " + arr[1],
                                        //        Toast.LENGTH_SHORT).show();

                                        dbRef.child("usersInfo").child("students").child(arr[0])
                                                .child("courses").child(course.getCourseCode()).setValue(groupNo);

                                        dbRef.child("courses").child(course.getCourseCode()).child("groups")
                                                .child(index).child("students").child(String.valueOf(i))
                                                .setValue(arr[0]);
                                        group.getStudents().add(arr[0]);
                                        i++;

                                    }
                                    line = "Student Number: " + course.getGroups().get(grNo-1).getStudents().size();
                                    stdNumTxt.setText(line);
                                    if (flag){
                                        setRecycleViewAdapter();
                                    }else{
                                        adapter.notifyDataSetChanged();
                                    }

                                    Toast.makeText(GroupDetailActivity.this,"Students assigned successfully",
                                            Toast.LENGTH_SHORT).show();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                    Toast.makeText(GroupDetailActivity.this,"Error Read CSV file",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
    }

    private void uploadCSV(){
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermission();
            }
        });
    }

    public void checkStoragePermission(){
        final int REQUEST_STORAGE_PERMISSION = 101; // Change request code for storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Permission is already granted
            openFilePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){ // Change the request code to match the storage permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(this, "Storage permission required to access CSV files", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFilePicker() {
        // Launch file picker
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv"); // Restrict to CSV files
        filePickerLauncher.launch(intent);
    }


/*
    private void uploadCSV(){
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch file picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/csv"); // Restrict to CSV files
                filePickerLauncher.launch(intent);
            }
        });
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        Uri uri = data.getData();
                        try {
                            InputStream stream = getContentResolver().openInputStream(uri);
                            // Read the CSV file here and handle accordingly
                            // For simplicity, let's just log the file URI
                            if (stream != null) {
                                // Here you can handle the CSV file
                                // For example, you can parse the CSV and upload it to Firebase
                                // For demonstration purposes, let's just log the file URI
                                // You can replace this with your CSV parsing and uploading logic
                                String csvFilePath = uri.toString();
                                Toast.makeText(GroupDetailActivity.this,"uploaded successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
*/


    private void addStd(){
        addStdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = addStdIdTxt.getText().toString();
                if (checkNotEmpty(addStdIdTxt,str)){
                    if (!isFound(str)){
                        String node1, node2;
                        int n=0;
                        node1 = String.valueOf(grNo-1);
                        if (group.getStudents() != null){
                            n = group.getStudents().size();
                        }
                        node2 = String.valueOf(n);
                        // store in database
                        dbRef.child("usersInfo").child("students").child(str)
                                .child("courses").child(course.getCourseCode()).setValue(groupNo);
                        dbRef.child("courses").child(course.getCourseCode()).child("groups")
                                .child(node1).child("students").child(node2).setValue(str);

                        // update objects.
                        if (course.getGroups().get(grNo-1).getStudents() != null){
                            course.getGroups().get(grNo-1).getStudents().add(str);
                        }else{
                            course.getGroups().get(grNo-1).setStudents(new ArrayList<>());
                            course.getGroups().get(grNo-1).getStudents().add(str);
                            setRecycleViewAdapter();
                        }
                        // update UI
                        String str1 = "Students Number: " + group.getStudents().size();
                        stdNumTxt.setText(str1);

                        adapter.notifyDataSetChanged();
                        Toast.makeText(GroupDetailActivity.this,"Student added successfully",
                                Toast.LENGTH_SHORT).show();
                    }else{

                        Toast.makeText(GroupDetailActivity.this,"Student already found",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void deleteStd(){
        deleteStdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = deleteStdIdTxt.getText().toString();
                if (checkNotEmpty(deleteStdIdTxt,input)){
                    // check if student found.
                    if (isFound(input)){
                        String node1, node2;
                        node1 = String.valueOf(grNo-1);
                        node2 = String.valueOf(group.getStudents().indexOf(input));
                        dbRef.child("usersInfo").child("students").child(input)
                                .child("courses").child(course.getCourseCode()).removeValue();

                        dbRef.child("courses").child(course.getCourseCode())
                                .child("groups").child(node1)
                                .child("students").child(node2).removeValue();

                        course.getGroups().get(grNo-1).getStudents().remove(input);

                        //update UI
                        String str = "Students Number: " + group.getStudents().size();
                        stdNumTxt.setText(str);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(GroupDetailActivity.this,"Student deleted successfully",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(GroupDetailActivity.this,"Student Not Found",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean isFound(String id){
        if (group.getStudents() !=null){
            for (String std: group.getStudents()){
                if (id.equals(std)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkNotEmpty(EditText editText, String txt){
        if (txt.isEmpty()){
            editText.setError("This field must be filled");
            return false;
        }else {
            return true;
        }
    }

    private void toBack() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this,CourseDetailActivity.class);
                intent.putExtra("user",user);
                intent.putExtra("course",course);
                startActivity(intent);
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(),CourseDetailActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initializeScreen(){
        String str;
        grNo = Integer.parseInt(groupNo);
        group = course.getGroups().get(grNo-1);
        if (!user.getId().equals(group.getInstructorId())){
            findViewById(R.id.card2_gda).setVisibility(View.GONE);
            findViewById(R.id.card3_gda).setVisibility(View.GONE);
        }
        str = "Group " + groupNo;
        groupNoTxt.setText(str);
        if (group.getStudents() != null){
            str = "Student Number: " + group.getStudents().size();
            stdNumTxt.setText(str);
        }else {
            str = "Student Number: 0";
            stdNumTxt.setText(str);
        }

        // get instructor name
        if (!group.getInstructorId().isEmpty()){
            dbRef.child("usersInfo").child("instructors").child(group.getInstructorId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            instName = snapshot.child("name").getValue(String.class);
                            String str = "instructor Name-ID: " + instName + " - " + group.getInstructorId();
                            instNameTxt.setText(str);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(GroupDetailActivity.this,"Failed to get Data gda",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            str = "instructor Name-ID: Not Assigned yet";
            instNameTxt.setText(str);
        }
        if (group.getStudents() != null){
            setRecycleViewAdapter();
        }
    }

    private void setRecycleViewAdapter(){
        adapter = new InstructorListAdapter(group.getStudents());
        stdRv.setLayoutManager(new LinearLayoutManager(this));
        stdRv.setItemAnimator(new DefaultItemAnimator());
        stdRv.setAdapter(adapter);
    }

    private  void setIDs(){
        backBtn = findViewById(R.id.backBtn_gda);

        groupNoTxt = findViewById(R.id.groupNoTxtId_gda);
        instNameTxt = findViewById(R.id.instNameId_gda);
        stdNumTxt = findViewById(R.id.stdNumId_gda);

        stdRv = findViewById(R.id.studentListRv_gda);

        addStdBtn = findViewById(R.id.addStdBtn_gda);
        addStdIdTxt = findViewById(R.id.stdIdAdd_gda);
        uploadBtn = findViewById(R.id.uploadBtn_gda);

        deleteStdIdTxt = findViewById(R.id.stdIdDelete_gda);
        deleteStdBtn = findViewById(R.id.deleteStdBtn_gda);
    }
}