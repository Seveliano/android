package com.example.md_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.md_app.models.Model_User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.md_app.Constants.emailPattern;

public class Activity_Login extends Alert_Base {

    private LinearLayout lin_primery;
    private TextInputLayout til_email, til_password;
    private Button btn_Login;
    private TextView tv_signUp;
    private RelativeLayout btn_googleLogin;

    private String st_email, st_password;

    //======== firebase =======================//
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ProgressDialog progressDialog;

    //---------- GOOGLE SIGN IN -----------------//
    public static int GOOGLE_SIGNIN_KEY = 101;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_2);

        init_viewItem();

        event_handler();
    }

    private void init_viewItem(){
        til_email = findViewById(R.id.activityLogin_til_email);
        til_password = findViewById(R.id.activityLogin_til_password);
        btn_Login = findViewById(R.id.activityLogin_button);
        tv_signUp = findViewById(R.id.activityLogin_tv_signUp);
        btn_googleLogin = findViewById(R.id.activityLogin_btn_google);

        lin_primery = findViewById(R.id.activityLgoin_lin_prim);
        screenResolution();

        //===============================================================//
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        //==============================================================//
//        logout();
    }

    private void screenResolution(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int heightPixels = metrics.heightPixels;
        int widthPixels = metrics.widthPixels;

        lin_primery.setOrientation(LinearLayout.VERTICAL);
        lin_primery.setLayoutParams(new LinearLayout.LayoutParams(widthPixels, heightPixels));
    }

    private void event_handler(){
        tv_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_Login.this, Activity_SignUp.class);
                startActivity(intent);
            }
        });

        btn_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateEmail() | !validatePassword()) return;
                progressDialog = progressDialog.show(Activity_Login.this, "Alert", "Connecting...");
                loginEvent();
            }
        });

        btn_googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = progressDialog.show(Activity_Login.this, "Alert", "Connecting...");
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_SIGNIN_KEY);
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){
                    //--------- Login ---------------
                    Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
                    startActivity(intent);
                    finish();
                } else {
                    //--------- Log out -------------
                }
            }
        };
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

    public void loginEvent(){
        firebaseAuth.signInWithEmailAndPassword(st_email, st_password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        if (task.isSuccessful()){
//                            String uid = firebaseAuth.getCurrentUser().getUid();
//                            FirebaseDatabase.getInstance().getReference("users/" + uid + "/st_userName")
//                                    .addValueEventListener(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                            String userName = dataSnapshot.getValue(String.class);
//
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                        }
//                                    });

                        } else {
                            Toast.makeText(Activity_Login.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGNIN_KEY){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                if (progressDialog.isShowing()) progressDialog.dismiss();
                Toast.makeText(Activity_Login.this,
                        "Can not connect google acount", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        if (task.isSuccessful()){

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNew){
                                Model_User model_user;
                                String uid = task.getResult().getUser().getUid();
                                String userName = account.getDisplayName();
                                String email = account.getEmail();
                                String imgUrl = account.getPhotoUrl().toString();

                                if (imgUrl == null){
                                    model_user = new Model_User(userName, email, "empty");
                                } else {
                                    model_user = new Model_User(userName, email, imgUrl);
                                }

                                FirebaseDatabase.getInstance().getReference()
                                        .child("users").child(uid).setValue(model_user)
                                        .addOnCompleteListener(Activity_Login.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (!task.isSuccessful()){
                                                    alert_ok("Error!", "Sorry. There must be some error. Try again!");
                                                }
                                            }
                                        });
                            }

                        } else {
                            Toast.makeText(Activity_Login.this,
                                    "Sorry, there must be truble in the internet. Try again!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public void logout(){
        firebaseAuth.signOut();
        googleSignInClient.signOut();
    }
}
