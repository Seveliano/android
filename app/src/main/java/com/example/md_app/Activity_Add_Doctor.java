package com.example.md_app;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.md_app.models.Model_Doctor;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Activity_Add_Doctor extends Alert_Base {

    private String uid = null;

    private ImageView img_doctor;
    private EditText et_name, et_hospital, et_qualification;
    private Button btn_back, btn_sign;

    private String st_imgUrl, st_name, st_hospital, st_qualification;

//    //    final String storageDir;
//    File storageDir;
//    private String currentPhotoPath;
//    private Uri photoURI = null;
//
//    //------- modal dialog for capturing image --------------
//    private Dialog modal_img_capture;
//    private FrameLayout frame_camera, frame_library;
//
//    //------- pick the image in library -----------
//    public static final int REQUEST_TAKE_PHOTO = 102;
//    private static final int PICK_IMAGE_REQUEST = 103;
//
//    private ProgressDialog progressDialog;
//
//    public void createStorageDirectory(){
//        if (Build.VERSION.SDK_INT >= 23) {
//            //do your check here
//            if( !isPermissionGranted() ){
//                return;
//            }
//        }
//
//        storageDir = new File(Environment.getExternalStorageDirectory() + "/" + "MD_App");
//        if (!storageDir.exists()) {
//            boolean bResult = storageDir.mkdirs();
////            if( bResult ){
////                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
////            }else{
////                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
////            }
//        }
//    }
//
//    public  boolean isPermissionGranted() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Build.VERSION_CODES.M = 23
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//                    checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                return true;
//            } else {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                Manifest.permission.CAMERA}, 1);
//                return false;
//            }
//        }
//        else { //permission is automatically granted on sdk<23 upon installation
//
//            return true;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED // PERMISSION_GRANTED = 0;
//                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
//            createStorageDirectory();
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor);

        init_viewItem();

        init_modal();

        event_handling();
    }

    private void init_viewItem(){
        img_doctor = findViewById(R.id.addDoctor_image);
        et_name = findViewById(R.id.addDoctor_et_userName);
//        et_name.setInputType(InputType.TYPE_NULL);
        find_userName();

        et_hospital = findViewById(R.id.addDoctor_et_hospital);
        et_qualification = findViewById(R.id.addDoctor_et_qualification);
        btn_back = findViewById(R.id.addDcotor_btn_back);
        btn_sign = findViewById(R.id.addDoctor_btn_sign);
    }

//    public void init_modal()
//    {
//        modal_img_capture = new Dialog(this);
//        modal_img_capture.setContentView(R.layout.modal_img_capture);
//        modal_img_capture.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//        frame_camera = modal_img_capture.findViewById(R.id.frame_camera);
//        frame_library = modal_img_capture.findViewById(R.id.frame_library);
//    }

    private void find_userName(){
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("users/" + uid + "/st_userName")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        st_name = dataSnapshot.getValue(String.class);
                        et_name.setText(st_name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void event_handling(){
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        img_doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStorageDirectory();
                modal_img_capture.show();
            }
        });

//        frame_camera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                // check the camera
//                if (!hasCamera())
//                {
//                    alert_ok("Alert", "There is no user camera!");
//                }
//                else launchCamera();
//            }
//        });
//
//        frame_library.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openFileChooser();
//            }
//        });

        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()) return;

                signAsDoctor();
            }
        });
    }

//    //--- Check camera exists or not ---------------
//    public boolean hasCamera()
//    {
//        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
//    }
//
//    //-------- take a photo with a camera app --------------
//    public void launchCamera()
//    {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null)
//        {
//            File photoFile = null;
//            try{
//                photoFile = createImageFile();
//            }
//            catch (IOException e)
//            { }
//
//            //--- Continue only if the File was successfully created
//            if (photoFile != null)
//            {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                {
//                    photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
//                }
//                else {
//                    String s = Environment.getExternalStorageState();
//                    if (Environment.MEDIA_MOUNTED.equals(s))
//                    {
//                        photoURI = Uri.fromFile(photoFile);
//                    }
//                    else {
//                        photoURI = Uri.parse("content://com.example.android.fileprovider/");
//                    }
//                }
//
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
//
//            }
//        }
//    }
//
//    //-------- Create file for image ----------------------
//    private File createImageFile() throws IOException{
//        //---- Create an image file name -----------
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//
//        String imageFileName = "JPEG_" + timeStamp;
//
//        String state = Environment.getExternalStorageState();
//        File image = null;
//        if (Environment.MEDIA_MOUNTED.equals(state))
//        {
//            image = new File(storageDir.getPath() + "/" + imageFileName + ".jpg");
//        }
//        //--- Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
//        return image;
//    }
//
//    private void openFileChooser()
//    {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//
//        startActivityForResult(intent, PICK_IMAGE_REQUEST);
//    }

    //-------- return the image taken ------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //---------- result in the case of capture and store --------------------
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO)
        {
            img_doctor.setImageURI(photoURI);;
            modal_img_capture.dismiss();
        }

        //---------- result in the case of file choosing ----------------
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            photoURI = data.getData();

            img_doctor.setImageURI(photoURI);;
            modal_img_capture.dismiss();
        }
    }

    //----- validation ---------------//
    private boolean validate() {
        st_name = et_name.getText().toString().trim();
        st_hospital = et_hospital.getText().toString().trim();
        st_qualification = et_qualification.getText().toString().trim();

        if (st_name.isEmpty() || st_name.equals("")){
            alert_ok("Alert", "Enter your name!");
            return false;
        }else if (st_hospital.isEmpty() || st_hospital.equals("")){
            alert_ok("Alert", "Enter your hospital!");
            return false;
        } else if (st_qualification.isEmpty() || st_qualification.equals("")){
            alert_ok("Alert", "Enter your qualification!");
            return false;
        } else if (photoURI == null) {
            alert_ok("Alert", "Image require!");
            return false;
        }

        return true;
    }

    private void signAsDoctor() {

        progressDialog = progressDialog.show(Activity_Add_Doctor.this, "Alert", "Connecting...");

        //======== get firebase StorageReference =======================//
        final StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("img_doctors").child(uid);

//        UploadTask uploadTask = ref.putFile(photoURI);
//
//        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                if (!task.isSuccessful()){
//                    throw task.getException();
//                }
//
//                return ref.getDownloadUrl();
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//                if (task.isSuccessful()){
//                    Uri downloadUri = task.getResult();
//
//                    Picasso.with(Activity_Add_Doctor.this)
//                            .load(downloadUri.toString())
//                            .into(imgback);
//                    Log.d("DownloadUri", "uri: " + downloadUri.toString());
//                } else {
//                    Log.d("DownloadUri", "failed");
//                }
//            }
//        });

        //======== image upload ==================//
        ref.putFile(photoURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){

                                //=============== Register As Doctor ====================//
                                st_imgUrl = task.getResult().toString();
                                Model_Doctor model_doctor = new Model_Doctor(uid, st_name, st_imgUrl, st_hospital, st_qualification);

                                FirebaseDatabase.getInstance().getReference()
                                        .child("doctors").child(uid).setValue(model_doctor)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                                if (task.isSuccessful()){
                                                    alert_ok("Alert", "Register Successfully!");
                                                } else {
                                                    Toast.makeText(Activity_Add_Doctor.this,
                                                            task.getException().toString(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                            else {
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                Toast.makeText(Activity_Add_Doctor.this,
                                        "Can not upload your image. Try agin", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(Activity_Add_Doctor.this,
                            "Can not upload your image. Try agin", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
