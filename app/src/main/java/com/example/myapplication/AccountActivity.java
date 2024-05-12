package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import myClasses.User;

public class AccountActivity extends AppCompatActivity {
    private EditText[] editTexts = new EditText[7];
    private TextView[] txts = new TextView[2];
    private Button updateBtn;
    private ImageButton backBtn;
    private ImageView[] imgBtns = new ImageView[3];
    private CheckBox[] chkBxs = new CheckBox[4];
    private ImageView profile_pic;
    private ProgressBar progressBar,updateProgBar;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private User user;
    private String userId, dir;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setIds();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();
        dbRef = FirebaseDatabase.getInstance().getReference();
        initializeScreen();

        selectImageFromGalleryActivity();
        takePhotoActivity();

        updateOps();

        updatePrivates();

        forwarding();

        setProfileDialog();

        toBack();

        toBackDefault();

    }
    public void setProfileDialog(){
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                String[] items = {"Gallery","Camera","Delete image"};
                builder.setTitle(R.string.select);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            imagePickerLauncher.launch(intent);
                        }else if(which==1){
                            checkCameraPermission();
                        }else {
                            deleteProfileImage();
                        }
                    }
                });
                builder.create().show();
            }
        });
    }


    public void checkCameraPermission(){
        final int REQUEST_CAMERA_PERMISSION = 100;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            // Permission is already granted
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoPickerLauncher.launch(takePictureIntent);
        }else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    public void takePhotoActivity(){
        photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.hasExtra("data")) {
                                progressBar.setVisibility(View.VISIBLE);
                                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                                dbRef.child("usersInfo").child(dir).child(user.getId()).child("profile").setValue("yes");
                                storeProfileImage(bitmap);
                                profile_pic.setImageBitmap(bitmap);
                            } else {
                                Toast.makeText(AccountActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(AccountActivity.this, "Failed getting image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void selectImageFromGalleryActivity(){
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        Uri uri = data.getData();
                        try{
                            InputStream stream = getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(stream);
                            profile_pic.setImageBitmap(bitmap);
                            dbRef.child("usersInfo").child(dir).child(user.getId()).child("profile").setValue("yes");
                            storeProfileImage(bitmap);
                        }catch (FileNotFoundException e){
                            progressBar.setVisibility(View.GONE);
                            e.getStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void storeProfileImage(Bitmap imageBitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images").child(user.getId());
        imageRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "Image upload successfully: ", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Image upload failed: ", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void forwarding(){

        imgBtns[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_DIAL);
                if (!user.getPhoneNumber().isEmpty()){
                    intent.setData(Uri.parse("tel:"+user.getPhoneNumber()));
                    startActivity(intent);
                }else{
                    startActivity(intent);
                }
            }
        });
        imgBtns[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = user.contactInfo();
                openApp("com.whatsapp",content);

            }
        });
        imgBtns[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = user.contactInfo();
                openApp("com.google.android.gm",content);
            }
        });
    }

    private void openApp(String url,String content){
        if (isInstalled(url)==1){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, content);
            intent.setType("text/plain");
            intent.setPackage(url);
            startActivity(intent);
        }else {
            Toast.makeText(AccountActivity.this,"App not found",Toast.LENGTH_SHORT).show();
        }
    }

    private int isInstalled(String url){
        PackageManager packageManager = getPackageManager();
        int flag = 0;
        try {
            packageManager.getPackageInfo(url,0);
            flag = 1;
        }catch (PackageManager.NameNotFoundException e){
            //Do nothing
        };
        return flag;
    }

    private void updatePrivates(){
        chkBxs[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String val = user.getTargetIndex("email");
                if(val.equals("1")){
                    user.setTargetIndex("email","0");
                }else {
                    user.setTargetIndex("email","1");
                }
            }
        });

        chkBxs[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String val = user.getTargetIndex("phone_num");
                if(val.equals("1")){
                    user.setTargetIndex("phone_num","0");
                }else {
                    user.setTargetIndex("phone_num","1");
                }
            }
        });

        chkBxs[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String val = user.getTargetIndex("insta");
                if(val.equals("1")){
                    user.setTargetIndex("insta","0");
                }else {
                    user.setTargetIndex("insta","1");
                }
            }
        });

        chkBxs[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String val = user.getTargetIndex("twit");
                if(val.equals("1") ){
                    user.setTargetIndex("twit","0");
                }else {
                    user.setTargetIndex("twit","1");
                }
            }
        });
    }

    private void updateOps(){
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProgBar.setVisibility(View.VISIBLE);
                updateUser();
            }
        });
    }

    private void updateUser(){
        String str;
        boolean changed = false;
        str = getContent(editTexts[0]);
        if(!str.isEmpty()){
            if (!user.getName().equals(str)){
                user.setName(str);
                changed = true;
            }
        }else{
            editTexts[0].setError("This field must be filled");
        }

        str = getContent(editTexts[1]);
        if(!str.isEmpty()){
            if (!user.getSurname().equals(str)){
                user.setSurname(str);
                changed = true;
            }
        }else{
            editTexts[1].setError("This field must be filled");
        }

        str = getContent(editTexts[2]);
        if (!str.isEmpty()){
            if (!user.getPassword().equals(str)){
                user.setPassword(str);
                updatePassword(str);
                changed = true;
            }
        }else {
            editTexts[2].setError("This field must be filled");
        }

        str = getContent(editTexts[3]);
        if (!user.getPhoneNumber().equals(str)){
            user.setPhoneNumber(str);
            changed = true;
        }
        str = getContent(editTexts[4]);
        if (!user.getEduc_info().equals(str)){
            user.setEduc_info(str);
            changed = true;
        }
        str = getContent(editTexts[5]);
        if (!user.getInstagram().equals(str)){
            user.setInstagram(str);
            changed = true;
        }
        str = getContent(editTexts[6]);
        if (!user.getTwit().equals(str)){
            user.setTwit(str);
            changed = true;
        }
        if(changed){
            dbRef.child("usersInfo").child(dir).child(user.getId()).setValue(user);
        }
        updateProgBar.setVisibility(View.GONE);
        Toast.makeText(this, "Update completed", Toast.LENGTH_SHORT).show();
    }

    public void updatePassword(String newPassword){
        FirebaseUser fbUser = mAuth.getCurrentUser();
        fbUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(AccountActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(Account.this,"Could not update password",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getContent(EditText edtTxt){
        return edtTxt.getText().toString();
    }
    private void initializeScreen(){
        user = (User) getIntent().getSerializableExtra("user");
        editTexts[0].setText(user.getName());
        editTexts[1].setText(user.getSurname());
        txts[1].setText(user.getId());
        txts[0].setText(user.getEmail());
        if (user.getHiddens().get("email").equals("1")){
            chkBxs[0].setChecked(true);
        }
        editTexts[2].setText(user.getPassword());
        editTexts[3].setText(user.getPhoneNumber());
        editTexts[4].setText(user.getEduc_info());
        editTexts[5].setText(user.getInstagram());
        editTexts[6].setText(user.getTwit());
        if (user.getHiddens().get("phone_num").equals("1")){
            chkBxs[1].setChecked(true);
        }
        if (user.getHiddens().get("insta").equals("1")){
            chkBxs[2].setChecked(true);
        }
        if (user.getHiddens().get("twit").equals("1")){
            chkBxs[3].setChecked(true);
        }
        if(!user.getProfile().isEmpty()){
            getUserProfile();
        }
        getDir();
    }

    private void getUserProfile() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images").child(user.getId());
        final long TWO_MEGABYTE = 4096 * 4096;
        imageRef.getBytes(TWO_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                profile_pic.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(AccountActivity.this,"Profile upload failed",Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void deleteProfileImage(){
        progressBar.setVisibility(View.VISIBLE);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("images").child(user.getId());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressBar.setVisibility(View.VISIBLE);
                dbRef.child("usersInfo").child(dir).child(user.getId()).child("profile").setValue("");
                profile_pic.setImageDrawable(ContextCompat.getDrawable(AccountActivity.this,R.drawable.default_profile_image));
                Toast.makeText(AccountActivity.this,"Deleted successfully",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AccountActivity.this,"Delete unsucessfull",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void toBackDefault(){
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

    private void toBack(){
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MenuActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
            }
        });
    }

    private void getDir(){
        if (user.getAccount_type().equals("Student")){
            dir = "students";
        }else {
            dir = "instructors";
        }
    }

    private void setIds(){
        txts[0] = findViewById(R.id.emailFieldId_aa);
        txts[1] = findViewById(R.id.stdIdFieldId_aa);
        editTexts[0] = findViewById(R.id.nameFieldId_aa);
        editTexts[1] = findViewById(R.id.srnameFieldId_aa);
        editTexts[2] = findViewById(R.id.passwFieldId_aa);
        editTexts[3] = findViewById(R.id.phoneNumFieldId_aa);
        editTexts[4] = findViewById(R.id.educFieldId_aa);
        editTexts[5] = findViewById(R.id.instaFieldId_aa);
        editTexts[6] = findViewById(R.id.twitFieldId_aa);
        imgBtns[0] = findViewById(R.id.phoneBtnId_aa);
        imgBtns[1] = findViewById(R.id.wtBtnId_aa);
        imgBtns[2] = findViewById(R.id.gmailBtnId_aa);
        chkBxs[0] = findViewById(R.id.emailChkId_aa);
        chkBxs[1] = findViewById(R.id.numChkId_aa);
        chkBxs[2] = findViewById(R.id.instaChkId_aa);
        chkBxs[3] = findViewById(R.id.twtChkId_aa);
        updateBtn = findViewById(R.id.updateBtnId_aa);
        profile_pic = findViewById(R.id.profileId_aa);
        progressBar = findViewById(R.id.progressBar_aa);
        updateProgBar = findViewById(R.id.updateProgBarId_aa);
        backBtn = findViewById(R.id.backBtn_aa);
    }
}