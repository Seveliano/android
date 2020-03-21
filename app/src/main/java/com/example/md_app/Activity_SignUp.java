package com.example.md_app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.md_app.models.Model_User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.md_app.Constants.emailPattern;

public class Activity_SignUp extends Alert_Base {

    private ImageView img_camera, img_photo;
    private TextInputLayout til_userName, til_email, til_password, til_confirm_pass;
    private Button btn_sign_back, btn_signUp;

    private String st_userName, st_email, st_password, st_confirm_pass;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init_viewItem();

        init_modal();

        event_handler();
    }

    private void init_viewItem(){
        img_photo = findViewById(R.id.activitysignUp_img_photo);
        img_camera = findViewById(R.id.activitySignUp_img_camera);
        til_userName = findViewById(R.id.activitySignUp_til_userName);
        til_email = findViewById(R.id.activitySignUp_til_email);
        til_password = findViewById(R.id.activitySignUp_til_password);
        til_confirm_pass = findViewById(R.id.activitySignUp_til_confirmPassword);
        btn_sign_back = findViewById(R.id.btn_sign_back);
        btn_signUp = findViewById(R.id.activitySignUp_button);
    }

    private void event_handler(){
        img_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStorageDirectory();
                modal_img_capture.show();
            }
        });

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validateEmail() | !validateUsername() | !validatePassword() | !validateConfirmPassword()) return;

                progressDialog = progressDialog.show(Activity_SignUp.this, "Alert", "Connecting...");

                createAccount_in_firebase();
            }
        });

        btn_sign_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validateUsername(){
        st_userName = til_userName.getEditText().getText().toString().trim();
        if (st_userName.isEmpty()){
            til_userName.setError("User name can't be empty");
            return false;
        }else  if (st_userName.length() > 15){
            til_userName.setError("Username is too long");
            return false;
        }else {
            til_userName.setError(null);
            return true;
        }
    }

    private boolean validateEmail(){
        st_email = til_email.getEditText().getText().toString().trim();
        if (st_email.isEmpty()){
            til_email.setError("Email can't be empty!");
            return false;
        }else if (!st_email.matches(emailPattern)){
            til_email.setError("Invalid email!");
            return false;
        } else {
            til_email.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        st_password = til_password.getEditText().getText().toString().trim();
        if (st_password.isEmpty()){
            til_password.setError("Password can't be empty!");
            return false;
        }else if (st_password.length() < 6) {
            til_password.setError("Password must be over 6 letters!");
            return false;
        }else {
                til_password.setError(null);
                return true;
        }
    }

    private boolean validateConfirmPassword(){
        st_confirm_pass = til_confirm_pass.getEditText().getText().toString().trim();
        if (st_confirm_pass.isEmpty()){
            til_password.setError("Confirm your password");
            return false;
        }else if (!st_confirm_pass.equals(st_password)) {
            til_password.setError("Invalid Password");
            return false;
        }else {
            til_password.setError(null);
            return true;
        }
    }

    //-------- return the image taken ------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //---------- result in the case of capture and store --------------------
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO)
        {
            img_photo.setImageURI(photoURI);;
            modal_img_capture.dismiss();
        }

        //---------- result in the case of file choosing ----------------
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            photoURI = data.getData();

            img_photo.setImageURI(photoURI);;
            modal_img_capture.dismiss();
        }
    }

    private void createAccount_in_firebase(){
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(st_email, st_password)
                .addOnCompleteListener(Activity_SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            String uid = task.getResult().getUser().getUid();
                            imageUpload_in_firebaseFileStore(uid);
                        } else {
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                            Toast.makeText(Activity_SignUp.this,
                                    "There are already user exists. Choose another email or password",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void imageUpload_in_firebaseFileStore(final String userId){
        if (photoURI == null){
            registerUser_in_firebaseDatabase(userId, "empty");
        } else {
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("img_users").child(userId);

            UploadTask uploadTask = ref.putFile(photoURI);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        Toast.makeText(Activity_SignUp.this,
                                task.getException().toString(), Toast.LENGTH_LONG).show();
                        throw task.getException();
                    }

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){

                        String st_downloadUrl = task.getResult().toString();
                        registerUser_in_firebaseDatabase(userId, st_downloadUrl);

                    } else {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        Toast.makeText(Activity_SignUp.this,
                                task.getException().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    private void registerUser_in_firebaseDatabase(String userId, String imgUrl){

        Model_User model_user = new Model_User(st_userName, st_email, imgUrl);

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(userId).setValue(model_user)
                .addOnCompleteListener(Activity_SignUp.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        if (task.isSuccessful()){
                            alert_ok_withFinish("Welcome!", "You registered successfully");
                        } else {
                            alert_ok("Error!", "Sorry. There must be some error. Try again!");
                        }
                    }
                });
    }

    public void alert_ok_withFinish(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle(title);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
