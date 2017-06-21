package com.mhassanetimadi.CRIDA_e_teamwork;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qa30m.m_teamwork.R;

import java.util.ArrayList;


public class ActivityAddEmployee extends AppCompatActivity {

    // UI Variables
    private Button buttonAddUser, buttonCancel;
    private EditText editTextFirstname, editTextLastname, editTextUsername, editTextPassword;
    private Spinner spinnerUserCategory;
    private ProgressDialog mProgressDialog;

    private ArrayList<String> availableUserCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

        initializeViews();

        // set up user category adapter
        setUpUserCategorySpinner();

        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAdding();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityAddEmployee.this.finish();
            }
        });

        mProgressDialog = new ProgressDialog(this);
    }

    private void setUpUserCategorySpinner() {

        // get all user categories
        ArrayList<String> allUserCategories = new ArrayList();
        for (String category : DbContract.USER_LEVELS) {
            allUserCategories.add(category);
        }

        // get current user's category
        String curUserCategory = getSharedPreferences(DbContract.PREF_USER, MODE_PRIVATE)
                .getString(DbContract.USER_CATEGORY, null);
        int index = allUserCategories.indexOf(curUserCategory);
        // add all user categories that this user category can create
        for (int i = allUserCategories.size() - 1; i > index; i--) {
            availableUserCategories.add(allUserCategories.get(i));
        }

        // set up user category spinner
        ArrayAdapter userCategoryAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                availableUserCategories);
        spinnerUserCategory.setAdapter(userCategoryAdapter);
    }

    private void startAdding() {

        // Show ProgressBar while posting
        mProgressDialog.setMessage("Adding employee ...");
        mProgressDialog.show();

        final String firstName = editTextFirstname.getText().toString().trim();
        final String lastName = editTextLastname.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String userCategory = availableUserCategories.get(spinnerUserCategory.getSelectedItemPosition());
        final String profilePic = DbContract.PROFILE_PIC_DEFAULT;

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(userCategory)
                || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            mProgressDialog.dismiss();
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isUsernameAvailable(username)) {
            Toast.makeText(this, "Username is not available\nPlease choose another username.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        final DatabaseReference newUserDbRef = FirebaseDatabase.getInstance().getReference()
                .child(DbContract.DB_EMPLOYEES).push();

        // add username
        newUserDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                newUserDbRef.child(DbContract.USER_NAME).setValue(username);
                newUserDbRef.child(DbContract.FIRST_NAME).setValue(firstName);
                newUserDbRef.child(DbContract.LAST_NAME).setValue(lastName);
                newUserDbRef.child(DbContract.PASSWORD).setValue(password);
                newUserDbRef.child(DbContract.PHONE).setValue(DbContract.PHONE_NOT_SET);
                newUserDbRef.child(DbContract.EMAIL).setValue(DbContract.EMAIL_NOT_SET);
                newUserDbRef.child(DbContract.PROFILE_PIC).setValue(profilePic);
                String managerUserName = getSharedPreferences(DbContract.PREF_USER, MODE_PRIVATE)
                        .getString(DbContract.USER_NAME, "no_user_name");
                newUserDbRef.child(DbContract.MANAGER_USER_NAME).setValue(managerUserName);
                newUserDbRef.child(DbContract.USER_CATEGORY).setValue(userCategory)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    mProgressDialog.dismiss();

                                    ActivityAddEmployee.this.setResult(RESULT_OK);
                                    ActivityAddEmployee.this.finish();
                                } else {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(ActivityAddEmployee.this, "Couldn't add employee\n" +
                                            "Check your internet connection", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @NonNull
    private boolean isUsernameAvailable(final String username) {

        final boolean[] usernameAvailable = {true};

        DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference()
                .child(DbContract.DB_EMPLOYEES);
        usersDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String existentUsername = "";
                if (dataSnapshot.child(DbContract.USER_NAME).getValue() != null) {
                    existentUsername = dataSnapshot.child(DbContract.USER_NAME).getValue().toString();
                    if (existentUsername == username) {
                        usernameAvailable[0] = false;
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return usernameAvailable[0];
    }

    private void initializeViews() {
        buttonAddUser = (Button) findViewById(R.id.btn_register);
        buttonCancel = (Button) findViewById(R.id.btn_cancel);
        editTextFirstname = (EditText) findViewById(R.id.editTextFirstName);
        editTextLastname = (EditText) findViewById(R.id.editTextLastName);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        spinnerUserCategory = (Spinner) findViewById(R.id.spinner_user_type);
    }
}