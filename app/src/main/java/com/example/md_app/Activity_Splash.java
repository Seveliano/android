package com.example.md_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.WindowManager;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Timer;
import java.util.TimerTask;

public class Activity_Splash extends Alert_Base implements OnProgressBarListener {
    private final String TAG = "Activity_Splash_log";

    private NumberProgressBar progressBar;
    private Timer timer;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //--- no title bar ---------------------
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.activitySplash_progressBar);
        progressBar.setProgress(0);
        progressBar.setOnProgressBarListener(this);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.incrementProgressBy(1);
                    }
                });
            }
        }, 1000, 50);

//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(3600)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
//        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.firebase_remote_config);
//
//        mFirebaseRemoteConfig.fetchAndActivate()
//                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Boolean> task) {
//                        if (task.isSuccessful()) {
//
//                            Toast.makeText(Activity_Splash.this, "fetch...", Toast.LENGTH_LONG).show();
//                        } else {
//                        }
//                        displayMessage();
//                    }
//                });
    }

    @Override
    public void onProgressChange(int current, int max) {
        if (current == max){
            timer.cancel();

            Intent intent = new Intent(this, Activity_Login.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

//    private void displayMessage(){
//        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
//        boolean splash_message_caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
//        String splash_message = mFirebaseRemoteConfig.getString("splash_message");
//
////        if (splash_message_caps){
////            alert_ok("Alert", splash_message);
////        }
//    }


}
