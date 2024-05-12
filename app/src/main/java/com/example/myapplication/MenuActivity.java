package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import myClasses.User;

public class MenuActivity extends AppCompatActivity {
    private User user;
    private TextView accountTxtBtn, coursesTxtBtn, reportsTxtBtn, logoutTxtBtn;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        user = (User) getIntent().getSerializableExtra("user");

        setIds();

        toAccountPage();
        toCoursesPage();
        toReportsPage();
        this.logout();
    }
    private void toAccountPage() {
        accountTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this,AccountActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });
    }

    private void toCoursesPage() {

        coursesTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this,CourseActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });
    }

    private void toReportsPage() {
        reportsTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MenuActivity.this, Report.class);
                //intent.putExtra("user",user);
                //startActivity(intent);
                Toast.makeText(MenuActivity.this,"Report Button clicked"
                        ,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout(){
        logoutTxtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });
    }


    private void setIds() {
        accountTxtBtn = findViewById(R.id.menuActAccountBtn);
        coursesTxtBtn = findViewById(R.id.menuActCoursesBtn);
        reportsTxtBtn = findViewById(R.id.menuActReportsBtn);
        logoutTxtBtn = findViewById(R.id.menuActLogoutBtn);
    }
}