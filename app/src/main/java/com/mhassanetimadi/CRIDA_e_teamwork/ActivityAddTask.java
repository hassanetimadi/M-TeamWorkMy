package com.mhassanetimadi.CRIDA_e_teamwork;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

public class ActivityAddTask extends AppCompatActivity {

    public static final int GALLERY_REQUEST_CODE = 1001;
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String MANAGER_NAME = "manager";
    public static final String TO = "to";

    // UI Variables
    EditText editTextTitle;
    EditText editTextDescription;
    Button buttonAddTask;
    Spinner spinnerTo;
    private ProgressDialog mProgressDialog;
    private ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        editTextTitle = (EditText) findViewById(R.id.edit_postTitle);
        editTextDescription = (EditText) findViewById(R.id.editPostDescription);
        buttonAddTask = (Button) findViewById(R.id.buttonAddTask);
        spinnerTo = (Spinner) findViewById(R.id.spinner_to);

        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });

        // set an adapter for testing
        setSpinnerToAdapter();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Posting ...");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Assign New Task");
    }

    private void setSpinnerToAdapter() {
        items = new ArrayList<>();
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,items);

        items.add("Select a person");

        DatabaseReference employeesDbRef = FirebaseDatabase.getInstance().getReference(DbContract.DB_EMPLOYEES);
        employeesDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String curUser = getSharedPreferences(DbContract.PREF_USER, Context.MODE_PRIVATE)
                        .getString(DbContract.USER_NAME, "no name");
                String managerUserName = dataSnapshot.child(DbContract.MANAGER_USER_NAME).getValue().toString();

                if (curUser.equals(managerUserName)) {
                    String firstName = dataSnapshot.child(DbContract.FIRST_NAME).getValue().toString();
                    String lastName = dataSnapshot.child(DbContract.LAST_NAME).getValue().toString();
                    String userName = dataSnapshot.child(DbContract.USER_NAME).getValue().toString();
                    String userCategory = dataSnapshot.child(DbContract.USER_CATEGORY).getValue().toString();

                    items.add(firstName + lastName + "(" + userName + ", " + userCategory + ")");
                    adapter.notifyDataSetChanged();
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
        spinnerTo.setAdapter(adapter);
        spinnerTo.setSelection(0);
    }

    private void startPosting() {

        // Show ProgressBar while posting
        mProgressDialog.show();

        final String title = editTextTitle.getText().toString().trim();
        final String description = editTextDescription.getText().toString().trim();

        boolean cancel = false;

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || spinnerTo.getSelectedItemPosition()==0) {
            cancel = true;
            mProgressDialog.dismiss();
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_LONG).show();
        }

        if (cancel == false) {
            final DatabaseReference newPost = FirebaseDatabase.getInstance().getReference()
                    .child(DbContract.DB_TASKS).push();

            // add username to the post
            newPost.child(TITLE).setValue(title);
            newPost.child(DESCRIPTION).setValue(description);
            String userName = getSharedPreferences(DbContract.PREF_USER, MODE_PRIVATE)
                    .getString(DbContract.USER_NAME, "noname");
            newPost.child(MANAGER_NAME).setValue(userName);

            // Todo: get employee name from a spinner list and add the to db
            String to = items.get(spinnerTo.getSelectedItemPosition());
            to = to.substring(to.indexOf("(")+1, to.indexOf(","));
            newPost.child(TO).setValue(to).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mProgressDialog.dismiss();

                        ActivityAddTask.this.setResult(RESULT_OK);
                        ActivityAddTask.this.finish();
                    } else {
                        mProgressDialog.dismiss();
                        Toast.makeText(ActivityAddTask.this, "Couldn't add task\n" +
                                        "Check your internet connection", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
