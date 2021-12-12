package com.example.fer_medindex.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fer_medindex.R;
import com.example.fer_medindex.fragment.DoctorFragment;
import com.example.fer_medindex.fragment.ListPatientFragment;
import com.example.fer_medindex.fragment.OptionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BackgroundDoctor extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_doctor);
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new DoctorFragment()).commit();
        }
    }




    @SuppressLint("NonConstantResourceId")
    @SuppressWarnings("deprecation")
    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.bottom_doctor:
                        selectedFragment = new DoctorFragment();
                        break;
                    case R.id.bottom_list_patient:
                        selectedFragment = new ListPatientFragment();
                        break;
                    case R.id.bottom_options: {
                        selectedFragment = new OptionFragment();
                        break;
                    }

                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            };

}