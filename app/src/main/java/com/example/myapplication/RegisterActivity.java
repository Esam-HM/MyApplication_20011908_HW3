package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import myClasses.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText[] edtTxtArr = new EditText[5];
    private Button createAccBtn;
    private ProgressBar progressBar;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private User user;
    private String[] informations = new String[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toBackBtn();

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        setIds();

        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                getInfromations();
                if (checkNotEmpty()){

                    if(EmailAuth()){
                        setUser();
                        RegisterUser();
                    }else{
                        edtTxtArr[3].setError("invalid email address");
                        disappearProgressBar();
                    }
                }else{
                    disappearProgressBar();
                }
            }
        });

    }
    private void RegisterUser(){
        // create new user or register new user
        mAuth.createUserWithEmailAndPassword(informations[3], informations[4])
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Registration successful!",
                                    Toast.LENGTH_LONG).show();
                            String userID = mAuth.getCurrentUser().getUid().toString();
                            dbRef.child("users").child(userID).setValue(user.getId());
                            if(user.getAccount_type().equals("Student")){
                                dbRef.child("usersInfo").child("students").child(user.getId()).setValue(user);
                            }else {
                                dbRef.child("usersInfo").child("instructors").child(user.getId()).setValue(user);
                            }

                            // hide the progress bar
                            disappearProgressBar();

                            Intent intent = new Intent(RegisterActivity.this, MenuActivity.class);
                            intent.putExtra("user",user);
                            startActivity(intent);
                        }
                        else {
                            // Registration failed
                            Toast.makeText(getApplicationContext(), "Registration failed!!"
                                    + " Please try again later", Toast.LENGTH_LONG).show();
                            // hide the progress bar
                            disappearProgressBar();
                        }
                    }
                });
    }

    private void toBackBtn(){
        ImageButton backBtn = findViewById(R.id.backBtn_ra);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backBtn.setAlpha(0.2f);
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });
    }

    private boolean EmailAuth(){
        String str1 = "std.yildiz.edu.tr";
        String str2 = "yildiz.edu.tr";
        String[] segments = informations[3].split("@");
        if(segments.length == 2){
            if(segments[1].equals(str1) || segments[1].equals(str2)){
                return true;
            }
        }
        return false;
    }

    private void getInfromations(){
        int i;
        for (i=0;i<5;i++){
            informations[i] = edtTxtArr[i].getText().toString();
        }
    }

    private boolean checkNotEmpty(){
        int[] emptyFields = {-1,-1,-1,-1,-1};
        int i;
        int j=0;
        for (i=0;i<5;i++){
            if(informations[i].isEmpty()){
                emptyFields[j] = i;
                j++;
            }
        }
        if (emptyFields[0]==-1){
            return true;
        }else{
            sendError(emptyFields);
            return false;
        }
    }

    private void sendError(int[] arr){
        int i=0;
        for(i=0;i<5;i++){
            if(arr[i]!=-1){
                edtTxtArr[arr[i]].setError("This field must be filled");
            }
        }
    }

    private void setIds(){
        edtTxtArr[0] = findViewById(R.id.name_reg);
        edtTxtArr[1] = findViewById(R.id.surname_reg);
        edtTxtArr[2] = findViewById(R.id.stdID_reg);
        edtTxtArr[3] = findViewById(R.id.email_reg);
        edtTxtArr[4] = findViewById(R.id.password_reg);
        createAccBtn = findViewById(R.id.createAcc_Btn);
        progressBar = findViewById(R.id.progressbar_reg);
    }

    private void setUser(){
        user = new User(informations[0],informations[1],informations[2],informations[3],informations[4]);
    }

    private void showProgressBar(){
        LayoutParams layoutParams = (LayoutParams) createAccBtn.getLayoutParams();
        layoutParams.bottomMargin = 20;
        progressBar.setVisibility(View.VISIBLE);
    }

    private void disappearProgressBar(){
        progressBar.setVisibility(View.GONE);
        LayoutParams layoutParams = (LayoutParams) createAccBtn.getLayoutParams();
        layoutParams.bottomMargin = 0;
    }

}