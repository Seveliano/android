package com.example.md_app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.md_app.history.Fragment_History;
import com.example.md_app.Doctors.Fragment_Doctors;
import com.example.md_app.account.Fragment_Account;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class Activity_Main extends Alert_Base implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView navBar;
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        Toast.makeText(this, user.getUid(), Toast.LENGTH_LONG).show();

        init_viewItem();
    }

    private void init_viewItem(){
        navBar = findViewById(R.id.activityMain_bottomNavBar);
        navBar.setOnNavigationItemSelectedListener(this);

        fragments.add(new Fragment_History());
        fragments.add(new Fragment_Doctors());
        fragments.add(new Fragment_Account());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainActivity_fragment, new Fragment_History())
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment selectedFragment = null;

        switch (menuItem.getItemId()){
            case R.id.action_history:
                selectedFragment = fragments.get(0);
                break;
            case R.id.action_doctors:
                selectedFragment = fragments.get(1);
                break;
            case R.id.action_account:
                selectedFragment = fragments.get(2);
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainActivity_fragment, selectedFragment)
                .commit();

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = fragments.get(2);
        fragment.onActivityResult(requestCode, resultCode, data);
    }
}
