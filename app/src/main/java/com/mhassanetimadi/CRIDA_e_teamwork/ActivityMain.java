package com.mhassanetimadi.CRIDA_e_teamwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.qa30m.m_teamwork.R;

public class ActivityMain extends AppCompatActivity
        implements FragmentNavigationDrawer.OnFragmentListInteractionListener{

    // Constants for which section should be selected
    // When the activity starts
    public static final int TASKS_SECTION = 0;
    public static final int EMPLOYEES_SECTION = 1;
    public static final String KEY_SECTION = "key_section";

    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    Intent intent = new Intent(ActivityMain.this, ActivitySignin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };

        // set up toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setUpDrawer();

        // set section
        int section = getIntent().getIntExtra(KEY_SECTION, TASKS_SECTION);
        setSection(section);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setSection(int section) {

        if (section == 2) {
            mAuth.signOut();
        }
        else {
            Fragment fragment = null;

            if (section == TASKS_SECTION) {
                toolbar.setTitle("Tasks");

                SharedPreferences sharedPreferences = getSharedPreferences(DbContract.PREF_USER, MODE_PRIVATE);
                String userCategory = sharedPreferences.getString(DbContract.USER_CATEGORY, "nocat");
                Log.i("qasimtag", userCategory);

                if (userCategory.equals(DbContract.USER_LEVELS[0])) {
                    fragment = new FragmentEmployeesTasks();
                    Log.i("qasimtag", "0");
                }else if(userCategory.equals(DbContract.USER_LEVELS[3])) {
                    fragment = new FragmentMyTasks();
                    Log.i("qasimtag", "3");
                }else {
                    fragment = new FragmentTask();
                    Log.i("qasimtag", "-1");
                }
            } else if (section == EMPLOYEES_SECTION) {
                toolbar.setTitle("Employees");
                fragment = new FragmentEmployees();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mainContainer, fragment)
                    .commit();
        }
    }

    private void setUpDrawer() {
        FragmentNavigationDrawer drawer =
                (FragmentNavigationDrawer) getSupportFragmentManager().findFragmentById(R.id.drawerFragment);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.setUpDrawer(R.id.drawerFragment, drawerLayout, toolbar);
    }

    @Override
    public void onFragmentListInteraction(int index) {
        setSection(index);
    }
}
