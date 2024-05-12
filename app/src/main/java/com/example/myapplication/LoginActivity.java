package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import myClasses.User;

public class LoginActivity extends AppCompatActivity {
    private EditText emailAddresEdtTxt, passwordEdtTxt;
    private TextView changePasswordTxt;
    private Button loginBtn;
    private Button registerBtn;
    private CheckBox checkBox;
    private ProgressBar progressBar;
    private String[] informations = new String[2];
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setIds();

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        passwordVisiblty();

        registerUser();

        loginUser();

        resetPassword();

    }

    private void resetPassword(){
        changePasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailAddresEdtTxt.getText().toString();
                final EditText emailET = new EditText(v.getContext());
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(emailET);
                builder.setMessage(R.string.reset_password);
                emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                emailET.setHint("Email Address*");
                if (!email.isEmpty()){
                    emailET.setText(email);
                }
                builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String emailAdd = emailET.getText().toString();
                        if(!emailAdd.isEmpty()){
                            sendMail(emailAdd);
                        }else{
                            Toast.makeText(LoginActivity.this,"Email not entered",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do no thing
                    }
                });
                builder.create().show();
            }
        });
    }

    private void sendMail(String mail){
        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(LoginActivity.this,"Mail sent successfully",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,"Failed sending mail",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(){
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                getInformations();

                if (checkNotEmpty()){
                    if (EmailAuth()){
                        signIn();
                    }else{
                        emailAddresEdtTxt.setError("invalid email address");
                        disappearProgressBar();
                    }
                }else{
                    disappearProgressBar();
                }
            }
        });
    }

    private void registerUser(){
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });
    }

    private void signIn(){
        mAuth.signInWithEmailAndPassword(informations[0], informations[1]).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getUserInformations();
                    //Toast.makeText(LoginActivity.this, " Login successful!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "login failed. Please check your information", Toast.LENGTH_LONG).show();
                    disappearProgressBar();
                }
            }
        });
    }

    private void getUserInformations(){
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userId = mAuth.getCurrentUser().getUid().toString();
                String dir = getDir();
                String node = snapshot.child("users").child(userId).getValue(String.class);
                user = snapshot.child("usersInfo").child(dir).child(node).getValue(User.class);
                dbRef.removeEventListener(this);
                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                disappearProgressBar();
                intent.putExtra("user",user);
                startActivity(intent);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText(LoginActivity.this, "Failed in getting data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean EmailAuth(){
        String str1 = "std.yildiz.edu.tr";
        String str2 = "yildiz.edu.tr";
        String[] segments = informations[0].split("@");
        if(segments.length == 2){
            if(segments[1].equals(str1) || segments[1].equals(str2)){
                return true;
            }
        }
        return false;
    }

    private String getDir(){
        String[] segments = informations[0].split("@");
        if(segments[1].equals("yildiz.edu.tr")){
            return "instructors";
        }else{
            return "students";
        }
    }

    private boolean checkNotEmpty(){
        int flag = 0;

        if (informations[0].isEmpty()){
            emailAddresEdtTxt.setError("This field must be filled");
            flag = 1;
        }

        if (informations[1].isEmpty()){
            passwordEdtTxt.setError("This field must be filled");
            flag = 1;
        }
        return flag==0;
    }

    private void getInformations(){
        informations[0] = emailAddresEdtTxt.getText().toString();
        informations[1] = passwordEdtTxt.getText().toString();
    }

    private void setIds(){
        emailAddresEdtTxt = findViewById(R.id.emailAddress_login);
        passwordEdtTxt = findViewById(R.id.password_login);
        loginBtn = findViewById(R.id.login_button);
        registerBtn = findViewById(R.id.createAcc_login);
        changePasswordTxt = findViewById(R.id.change_password_btn);
        progressBar = findViewById(R.id.progressBar_login);
        checkBox = findViewById(R.id.onOff_password_login);
    }

    private void passwordVisiblty(){
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    passwordEdtTxt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else{
                    passwordEdtTxt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    private void showProgressBar(){
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) changePasswordTxt.getLayoutParams();
        layoutParams.bottomMargin = 20;
        progressBar.setVisibility(View.VISIBLE);
    }

    private void disappearProgressBar(){
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) changePasswordTxt.getLayoutParams();
        layoutParams.bottomMargin = 0;
    }

}