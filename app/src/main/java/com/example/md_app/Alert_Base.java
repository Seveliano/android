package com.example.md_app;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Alert_Base extends AppCompatActivity {

    public void alert_ok(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle(title);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //======================= test =====================================//

    //    final String storageDir;
    public File storageDir;
    public String currentPhotoPath;
    public Uri photoURI = null;

    //------- modal dialog for capturing image --------------
    public Dialog modal_img_capture;
    public FrameLayout frame_camera, frame_library;

    //------- pick the image in library -----------
    public static final int REQUEST_TAKE_PHOTO = 102;
    public static final int PICK_IMAGE_REQUEST = 103;

    public ProgressDialog progressDialog;

    public void createStorageDirectory(){
        if (Build.VERSION.SDK_INT >= 23) {
            //do your check here
            if( !isPermissionGranted() ){
                return;
            }
        }

        storageDir = new File(Environment.getExternalStorageDirectory() + "/" + "MD_App");
        if (!storageDir.exists()) {
            boolean bResult = storageDir.mkdirs();
//            if( bResult ){
//                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    public  boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Build.VERSION_CODES.M = 23
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED // PERMISSION_GRANTED = 0;
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            createStorageDirectory();
        }
    }

    public void init_modal()
    {
        modal_img_capture = new Dialog(this);
        modal_img_capture.setContentView(R.layout.modal_img_capture);
        modal_img_capture.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        frame_camera = modal_img_capture.findViewById(R.id.frame_camera);
        frame_library = modal_img_capture.findViewById(R.id.frame_library);

        frame_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // check the camera
                if (!hasCamera())
                {
                    alert_ok("Alert", "There is no user camera!");
                }
                else launchCamera();
            }
        });

        frame_library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
    }

    //--- Check camera exists or not ---------------
    public boolean hasCamera()
    {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    //-------- take a photo with a camera app --------------
    public void launchCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }
            catch (IOException e)
            { }

            //--- Continue only if the File was successfully created
            if (photoFile != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                }
                else {
                    String s = Environment.getExternalStorageState();
                    if (Environment.MEDIA_MOUNTED.equals(s))
                    {
                        photoURI = Uri.fromFile(photoFile);
                    }
                    else {
                        photoURI = Uri.parse("content://com.example.android.fileprovider/");
                    }
                }

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    //-------- Create file for image ----------------------
    private File createImageFile() throws IOException{
        //---- Create an image file name -----------
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp;

        String state = Environment.getExternalStorageState();
        File image = null;
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            image = new File(storageDir.getPath() + "/" + imageFileName + ".jpg");
        }
        //--- Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //============================================================//


/*===========================================================
---------------------- Api Connecting -----------------------
==============================================================*/

    //--------- Setting Interface ----------------
    public interface Interface_AsyncTask{
        void get_result(String output);
    }

    //--------- Api Connec class ----------------
    public static class Api_Register extends AsyncTask<Integer, Integer, String>
    {
        //----- interface variable -------
        public Interface_AsyncTask detect_register = null;

        private ProgressDialog progressDialog;
        private OkHttpClient httpClient = new OkHttpClient();

//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .build();

        private Context context;
        private RequestBody requestBody;
        private String url;

        public Api_Register(Context context, RequestBody requestBody, String url)
        {
            this.context = context;
            this.requestBody = requestBody;
            this.url = url;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            String result = "";
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try {
                Response response = httpClient.newCall(request).execute();
                ResponseBody responseBody = response.body();
                result = responseBody.string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(progressDialog.isShowing()){

                progressDialog.dismiss();
            }

            detect_register.get_result(result);
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog = progressDialog.show(context, "Alert", "Api connecting");
        }
    }

    //===========================================================================================//

    //--------- Setting Interface ----------------
    public interface Interface_getDB{
        void getDB_result(String output);
    }

    //----getdb------------------------------
    public static class Api_getDB extends AsyncTask<Integer, Integer,String>
    {
        //----- interface variable -------
        public Interface_getDB detect_getDB = null;

        private ProgressDialog progressDialog;
        private OkHttpClient httpClient = new OkHttpClient();

        private Context context;
        private String url;

        public Api_getDB(Context context, String url)
        {
            this.context = context;
            this.url = url;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            String result = "";
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                Response response = httpClient.newCall(request).execute();
                ResponseBody responseBody = response.body();
                result = responseBody.string();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }

            detect_getDB.getDB_result(result);
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog = progressDialog.show(context, "Alert", "Loading...");
        }
    }
}
