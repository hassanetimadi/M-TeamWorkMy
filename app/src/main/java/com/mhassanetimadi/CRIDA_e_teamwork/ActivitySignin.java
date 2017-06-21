package com.mhassanetimadi.CRIDA_e_teamwork;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qa30m.m_teamwork.R;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class ActivitySignin extends AppCompatActivity {

    public static final String LOGIN_TAG = "LoginTag";
    FirebaseAuth mAuth;

    // UI references.
    private AutoCompleteTextView mUsernameTextView;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;

    DatabaseReference employeesReference;
    ChildEventListener eventListener;
    private ArrayList<Employee> employeeArrayList = new ArrayList<>();

    public static final String ADMIN_EMAIL = "admin@gmail.com";
    public static final String ADMIN_PASS = "asdf1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Set up the login form.
        mUsernameTextView = (AutoCompleteTextView) findViewById(R.id.textView_userCategory);

        mPasswordView = (EditText) findViewById(R.id.editText_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSignin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignin();
            }
        });

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Signing in");
//        mProgressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        employeesReference = FirebaseDatabase.getInstance().getReference().child(DbContract.DB_EMPLOYEES);

        Log.d(LOGIN_TAG, "***************************** Loading employees *****************");

        eventListener = employeesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String username = dataSnapshot.child(DbContract.USER_NAME).getValue().toString();
                String password = dataSnapshot.child(DbContract.PASSWORD).getValue().toString();
                String category = dataSnapshot.child(DbContract.USER_CATEGORY).getValue().toString();
                String profilePic = dataSnapshot.child(DbContract.PROFILE_PIC).getValue().toString();
                String key = dataSnapshot.getKey();

                Log.d(LOGIN_TAG, username + ":" + password + ":" + category + ":" + profilePic + ":" + key);
                Employee employee = new Employee(username, password, category, profilePic, key);
                employeeArrayList.add(employee);
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
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignin() {

        mProgressDialog.show();

        // Reset errors.
        mUsernameTextView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String username = mUsernameTextView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameTextView.setError(getString(R.string.error_field_required));
            focusView = mUsernameTextView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        // firebase sing in
        mAuth.signInWithEmailAndPassword(ADMIN_EMAIL, ADMIN_PASS)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            mProgressDialog.dismiss();
                            showSigninErrorDialog();
                        }
                        else if (task.isSuccessful()) {
                            customSingin(username, password);
                        }
                    }
                });
    }

    private void customSingin(final String username, final String password) {

        boolean isCredentialsValid = false;
        for (Employee employee: employeeArrayList) {

            if (username.equals(employee.getUsername()) && password.equals(employee.password)) {
                isCredentialsValid = true;

                SharedPreferences sharedPreferences = getSharedPreferences(DbContract.PREF_USER, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(DbContract.USER_DB_KEY, employee.getKey());
                editor.putString(DbContract.USER_NAME, employee.getUsername());
                editor.putString(DbContract.USER_CATEGORY, employee.getCategory());
                editor.putString(DbContract.PROFILE_PIC, employee.getProfilePic());
                editor.commit();
            }
        }

        mProgressDialog.dismiss();

        if (isCredentialsValid) {
            Log.d(LOGIN_TAG, "*********************************************** Successful");

            Intent intent = new Intent(ActivitySignin.this, ActivityMain.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Log.d(LOGIN_TAG, "*********************************************** Unsuccessful");

            mAuth.signOut();
            showSigninErrorDialog();
        }
    }

    private void showSigninErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySignin.this);
        builder.setTitle("Error");
        if(employeeArrayList.size() == 0) {
            builder.setMessage("Couldn't sign in\nCheck your internet connection.");
        } else {
            builder.setMessage("Couldn't sign in\nCheck your username and password.");
        }

        builder.setCancelable(true);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        employeesReference.removeEventListener(eventListener);
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private class Employee {
        private String username;
        private String password;
        private String category;
        private String profilePic;
        private String key;

        public Employee(String username, String password, String category, String profilePic, String key) {
            this.username = username;
            this.password = password;
            this.category = category;
            this.profilePic = profilePic;
            this.key = key;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getCategory() {
            return category;
        }

        public String getProfilePic() {
            return profilePic;
        }

        public String getKey() {
            return key;
        }
    }
}

