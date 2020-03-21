package com.example.md_app.account;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.md_app.Activity_Login;
import com.example.md_app.Activity_Main;
import com.example.md_app.Activity_SignUp;
import com.example.md_app.R;
import com.example.md_app.models.Model_User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;
import static com.example.md_app.Alert_Base.PICK_IMAGE_REQUEST;
import static com.example.md_app.Alert_Base.REQUEST_TAKE_PHOTO;
import static com.example.md_app.Constants.emailPattern;

public class Fragment_Account extends Fragment {

    private View layout;
    private Context mContext;
    private GoogleSignInClient googleSignInClient;

    private ImageView img_camera, img_photo;
    private TextInputLayout til_userName, til_email, til_password, til_confirm_pass;
    private Button btn_upDate, btn_signOut;

    private FirebaseUser current_user;
    private String uid;
    private Model_User model_user_currentProfile;
    private String st_userName, st_email, st_password, st_confirm_pass;
    private Uri uri_photoUri = null;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_account, container, false);
        mContext = getContext();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        init_viewItem();

        ((Activity_Main) getActivity()).init_modal();

        event_handling();

        return layout;
    }

    private void init_viewItem() {
        img_photo = layout.findViewById(R.id.fragAccount_img_photo);
        img_camera = layout.findViewById(R.id.fragAccount_img_camera);
        til_userName = layout.findViewById(R.id.fragAccount_til_userName);
        til_email = layout.findViewById(R.id.fragAccount_til_email);
        til_password = layout.findViewById(R.id.fragAccount_til_password);
        til_confirm_pass = layout.findViewById(R.id.fragAccount_til_confirmPassword);
        btn_upDate = layout.findViewById(R.id.fragAccount_btn_upDate);
        btn_signOut = layout.findViewById(R.id.fragAccount_btn_signOut);

        get_userInfo_from_firebase();
    }

    private void get_userInfo_from_firebase() {

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        uid = current_user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        model_user_currentProfile = dataSnapshot.getValue(Model_User.class);
                        update_UI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(mContext, "Can't load user profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void update_UI() {
        Picasso.with(mContext)
                .load(model_user_currentProfile.st_imgUrl)
                .error(R.drawable.img_logo)
                .into(img_photo);
        til_userName.getEditText().setText(model_user_currentProfile.st_userName);
        til_email.getEditText().setText(model_user_currentProfile.st_email);
    }

    private void event_handling() {

        img_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity_Main) getActivity()).createStorageDirectory();
                ((Activity_Main) getActivity()).modal_img_capture.show();
            }
        });

        btn_signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                googleSignInClient.signOut();
                Intent intent = new Intent(mContext, Activity_Login.class);
                startActivity(intent);
            }
        });

        btn_upDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateEmail() | !validateUsername() | !validatePassword() | !validateConfirmPassword()) return;

                progressDialog = progressDialog.show(mContext, "Alert", "Connecting...");

                updateEmail();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //---------- result in the case of capture and store --------------------
        if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO)
        {
            uri_photoUri = ((Activity_Main) getActivity()).photoURI;
            img_photo.setImageURI(uri_photoUri);;
            ((Activity_Main) getActivity()).modal_img_capture.dismiss();
        }

        //---------- result in the case of file choosing ----------------
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            uri_photoUri = data.getData();

            img_photo.setImageURI(uri_photoUri);;
            ((Activity_Main) getActivity()).modal_img_capture.dismiss();
        }
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

    private void updateEmail(){
        if (!st_email.equals(model_user_currentProfile.st_email)){
            current_user.updateEmail(st_email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                updatePassword();
                            } else {
                                toast_message(task.getException().toString());
                            }
                        }
                    });
        } else {
            updatePassword();
        }
    }

    private void updatePassword(){
        current_user.updatePassword(st_password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            if (uri_photoUri != null){
                                updatePhotoFile();
                            } else {
                                update_database();
                            }
                        } else {
                            toast_message(task.getException().toString());
                        }
                    }
                });
    }

    private void updatePhotoFile(){
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child("img_users").child(uid);

        UploadTask uploadTask = ref.putFile(uri_photoUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    toast_message(task.getException().toString());
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){

                    String st_downloadUrl = task.getResult().toString();
                    model_user_currentProfile.st_imgUrl = st_downloadUrl;

                    update_database();

                } else {
                    toast_message(task.getException().toString());
                }
            }
        });
    }

    private void update_database(){
        model_user_currentProfile.st_userName = st_userName;
        model_user_currentProfile.st_email = st_email;

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(uid).setValue(model_user_currentProfile)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        if (task.isSuccessful()){
                            ((Activity_Main) getActivity()).alert_ok("Alert!", "Update your profile successfully");
                        } else {
                            toast_message(task.getException().toString());
                        }
                    }
                });
    }

    private void toast_message(String message){
        if (progressDialog.isShowing()) progressDialog.dismiss();
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }
}
