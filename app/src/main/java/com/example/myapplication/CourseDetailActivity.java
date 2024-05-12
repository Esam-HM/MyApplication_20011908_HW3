package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import myClasses.Course;
import myClasses.Group;
import myClasses.User;

public class CourseDetailActivity extends AppCompatActivity {
    private EditText crsNameTxt, instIdTxt, groupNoTxt, groupNoForwardTxt;
    private CardView crsDeleteBtn, addGroupBtn, deleteGroupBtn, addInstBtn, forwardBtn;
    private TextView crsCodeTxt, crsGrNumTxt, crsStdNumTxt, sDateTxt, semesterTxt, stdCrsNameTxt;
    private Button updateBtn;
    private ImageButton backBtn;
    private DatabaseReference dbRef;
    private Course course;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbRef = FirebaseDatabase.getInstance().getReference();
        course = (Course) getIntent().getSerializableExtra("course");
        user = (User) getIntent().getSerializableExtra("user");
        setIds();
        initializeScreen();

        updateCrsName();
        deleteCourse();
        assignInstructor();
        addNewGroup();
        deleteGroup();
        groupDetail();

        toBack();

    }

    private void groupDetail(){
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = groupNoForwardTxt.getText().toString();
                int input = Integer.parseInt(txt);
                if (checkNotEmpty(groupNoForwardTxt,txt)){
                    if (input <= course.getGroups().size()){
                        Intent intent = new Intent(CourseDetailActivity.this,GroupDetailActivity.class);
                        intent.putExtra("course",course);
                        intent.putExtra("user",user);
                        intent.putExtra("groupNo",txt);
                        startActivity(intent);
                    }else{
                        Toast.makeText(CourseDetailActivity.this,"Group not found",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void deleteGroup(){
        deleteGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = groupNoTxt.getText().toString();
                int input = Integer.parseInt(str);
                int i;
                if (checkNotEmpty(groupNoTxt,str)){
                    if (input <= course.getGroups().size()){
                        Group group = course.getGroups().get(input-1);
                        // check if this group for current instructor.
                        if (str.equals(user.getCourses().get(course.getCourseCode()))){

                            // delete from students.
                            if (group.getStudents() !=null){
                                for (String std: group.getStudents()){
                                    dbRef.child("usersInfo").child("students")
                                            .child(std).child("courses")
                                            .child(course.getCourseCode()).removeValue();
                                }
                            }
                            // delete from instructor.
                            user.getCourses().remove(course.getCourseCode());
                            dbRef.child("usersInfo").child("instructors")
                                    .child(user.getId()).child("courses")
                                            .child(course.getCourseCode()).removeValue();
                            // delete from course.
                            dbRef.child("courses").child(course.getCourseCode())
                                            .child("groups").child(String.valueOf(input-1))
                                            .removeValue();
                            Toast.makeText(CourseDetailActivity.this,"Group deleted successfully",
                                    Toast.LENGTH_SHORT).show();

                            course.getGroups().remove(input-1);

                            // update Groups No.
                            i=1;
                            String indexStr;
                            for (Group grp: course.getGroups()){
                                indexStr = String.valueOf(i);
                                // update for instructor
                                if (!grp.getInstructorId().isEmpty()){
                                    dbRef.child("usersInfo").child("instructors")
                                            .child(grp.getInstructorId()).child("courses")
                                            .child(course.getCourseCode()).setValue(indexStr);

                                    if (grp.getStudents() != null){
                                        for (String std: grp.getStudents()){
                                            dbRef.child("usersInfo").child("students")
                                                    .child(std).child("courses")
                                                    .child(course.getCourseCode()).setValue(indexStr);
                                        }
                                    }
                                }
                                i++;
                                grp.setGroupNo(String.valueOf(i-1));
                            }
                            course.setGroupNumbers(String.valueOf(course.getGroups().size()));

                            if (course.getGroupNumbers().equals("0")){
                                dbRef.child("courses").child(course.getCourseCode())
                                        .removeValue();
                            }else {
                                dbRef.child("courses").child(course.getCourseCode())
                                        .setValue(course);
                            }

                            //Toast.makeText(CourseDetailActivity.this,"Update Successfully",
                            //        Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(CourseDetailActivity.this,CourseActivity.class);
                            intent.putExtra("user",user);
                            startActivity(intent);

                        }else{
                            // delete from instructor.
                            if (!group.getInstructorId().isEmpty()){
                                dbRef.child("usersInfo").child("instructors")
                                        .child(group.getInstructorId()).child("courses")
                                        .child(course.getCourseCode()).removeValue();

                                // delete from students
                                if (group.getStudents() !=null){
                                    for (String std: group.getStudents()){
                                        dbRef.child("usersInfo").child("students")
                                                .child(std).child("courses")
                                                .child(course.getCourseCode()).removeValue();
                                    }
                                }

                            }
                            course.getGroups().remove(input-1);
                            // update groups No.
                            i=1;
                            String indexStr;
                            for (Group grp: course.getGroups()){
                                indexStr = String.valueOf(i);
                                // update for instructor
                                if (!grp.getInstructorId().isEmpty()){
                                    dbRef.child("usersInfo").child("instructors")
                                            .child(grp.getInstructorId()).child("courses")
                                            .child(course.getCourseCode()).setValue(indexStr);

                                    if (grp.getStudents() != null){
                                        for (String std: grp.getStudents()){
                                            dbRef.child("usersInfo").child("students")
                                                    .child(std).child("courses")
                                                    .child(course.getCourseCode()).setValue(indexStr);
                                        }
                                    }
                                }
                                i++;
                                grp.setGroupNo(String.valueOf(i-1));
                            }
                            course.setGroupNumbers(String.valueOf(course.getGroups().size()));
                            dbRef.child("courses").child(course.getCourseCode())
                                    .setValue(course);
                            int n=0;
                            String msg = "Number of Groups:" + course.getGroupNumbers();
                            crsGrNumTxt.setText(msg);
                            for (Group grp: course.getGroups()){
                                if (grp.getStudents() != null){
                                    n += grp.getStudents().size();
                                }
                            }
                            msg = "Total Students: " + n;
                            crsStdNumTxt.setText(msg);
                            Toast.makeText(CourseDetailActivity.this,"Group Deleted Successfully",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(CourseDetailActivity.this,"Group Not Found",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    private void addNewGroup(){
        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(course.getGroupNumbers()) +1;
                course.setGroupNumbers(String.valueOf(n));
                Group group = new Group(course.getGroupNumbers());
                group.setInstructorId("");
                course.getGroups().add(group);
                dbRef.child("courses").child(course.getCourseCode())
                        .child("groupNumbers").setValue(group.getGroupNo());
                dbRef.child("courses").child(course.getCourseCode())
                        .child("groups").child(String.valueOf(n-1)).setValue(group);

                String msg = "Number of Groups:" + course.getGroupNumbers();
                crsGrNumTxt.setText(msg);

                Toast.makeText(CourseDetailActivity.this,"Group Created Successfully",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void updateCrsName(){
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = crsNameTxt.getText().toString();
                if (checkNotEmpty(crsNameTxt,str)){
                    course.setCourseName(str);
                    dbRef.child("courses").child(course.getCourseCode())
                            .child("courseName").setValue(str);
                }
            }
        });
    }

    private void assignInstructor(){
        addInstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = instIdTxt.getText().toString();
                if (checkNotEmpty(instIdTxt, str)){
                    int i=0;
                    boolean found=false;
                    while (i<course.getGroups().size() && !found){
                        if (course.getGroups().get(i).getInstructorId().isEmpty()){
                            found=true;
                            dbRef.child("courses").child(course.getCourseCode())
                                    .child("groups").child(String.valueOf(i)).child("instructorId")
                                    .setValue(str);

                            dbRef.child("usersInfo").child("instructors")
                                    .child(str).child("courses").child(course.getCourseCode())
                                    .setValue(String.valueOf(i+1));
                            course.getGroups().get(i).setInstructorId(str);
                            Toast.makeText(CourseDetailActivity.this,"assigned successfully",
                                            Toast.LENGTH_SHORT).show();
                        }else {
                            i++;
                        }
                    }
                }
            }
        });
    }

    private void deleteCourse(){
        crsDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbRef.child("courses").child(course.getCourseCode()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        int i;
                        String str;
                        for (i=0;i<course.getGroups().size();i++){
                            for(Group grp: course.getGroups()){
                                str = grp.getInstructorId();
                                if (!str.isEmpty()){
                                    dbRef.child("usersInfo").child("instructors")
                                            .child(str).child("courses")
                                            .child(course.getCourseCode()).removeValue();
                                    if (grp.getStudents() !=null){
                                        for (String id:grp.getStudents()){
                                            dbRef.child("usersInfo").child("students")
                                                    .child(id).child("courses")
                                                    .child(course.getCourseCode()).removeValue();
                                        }
                                    }

                                }
                            }
                        }
                        Toast.makeText(CourseDetailActivity.this,"deleted Successfully",
                                Toast.LENGTH_SHORT).show();
                        user.getCourses().remove(course.getCourseCode());
                        Intent intent = new Intent(CourseDetailActivity.this,CourseActivity.class);
                        intent.putExtra("user",user);
                        startActivity(intent);
                    }
                });

            }
        });
    }

    private void initializeScreen(){
        String str;
        str = course.getCourseCode() + "- Gr: " + user.getCourses().get(course.getCourseCode());
        crsCodeTxt.setText(str);

        crsNameTxt.setText(course.getCourseName());

        str = "Number of Groups:" + course.getGroupNumbers();
        crsGrNumTxt.setText(str);

        int n=0;

        for (Group grp: course.getGroups()){
            if (grp.getStudents() != null){
                n += grp.getStudents().size();
            }
        }
        str = "Total Students: " + n;
        crsStdNumTxt.setText(str);
        str = "Start Date: " + course.getDate();
        sDateTxt.setText(str);
        str = course.getSemester();
        semesterTxt.setText(str);

        if (user.getAccount_type().equals("Student")){
            stdCrsNameTxt = findViewById(R.id.stdCrsNameTxtId_cd);
            stdCrsNameTxt.setVisibility(View.VISIBLE);
            stdCrsNameTxt.setText(course.getCourseName());
            crsDeleteBtn.setVisibility(View.GONE);
            findViewById(R.id.lineId_cda).setVisibility(View.VISIBLE);
            findViewById(R.id.editCoursNameId_cd).setVisibility(View.GONE);
            findViewById(R.id.card2_cd).setVisibility(View.GONE);
            findViewById(R.id.card3_cd).setVisibility(View.GONE);
            findViewById(R.id.card4_cd).setVisibility(View.GONE);
        }else{
            crsNameTxt.setText(course.getCourseName());
        }
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
                Intent intent = new Intent(CourseDetailActivity.this,CourseActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(),CourseActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setIds(){
        backBtn = findViewById(R.id.backBtn_cd);

        crsNameTxt = findViewById(R.id.courseNameId_cd);
        crsCodeTxt = findViewById(R.id.courseCodeTxt_cd);
        crsGrNumTxt = findViewById(R.id.groupNumTxtId_cd);
        crsStdNumTxt = findViewById(R.id.stdNumTxtId_cd);
        sDateTxt = findViewById(R.id.startDateTxtId_cd);
        semesterTxt = findViewById(R.id.semesterTxtId_cd);
        crsDeleteBtn = findViewById(R.id.deleteCrsBtnId_cd);
        updateBtn = findViewById(R.id.updateBtnId_cd);

        instIdTxt = findViewById(R.id.instAssID_cd);
        addInstBtn = findViewById(R.id.addInstBtn_cd);

        groupNoTxt = findViewById(R.id.groupNoId_cd);
        addGroupBtn = findViewById(R.id.addGrBtn_cd);
        deleteGroupBtn = findViewById(R.id.deleteGrBtn_cd);

        groupNoForwardTxt = findViewById(R.id.groupNoForwardId_cd);
        forwardBtn = findViewById(R.id.forwardBtn_cd);
    }

}