package com.mhassanetimadi.CRIDA_e_teamwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.qa30m.m_teamwork.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static com.mhassanetimadi.CRIDA_e_teamwork.ActivityAddTask.GALLERY_REQUEST_CODE;

/**
 * A login screen that offers login via email/password.
 */
public class ActivitySignup extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDB_Users;
    StorageReference mProfilePicsRef;

    // UI references.
    private ImageButton mImageButton;
    private AutoCompleteTextView mNameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private Uri mProfileImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI Variables
        mImageButton = (ImageButton) findViewById(R.id.imageButton_postPicture);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });

        mNameView = (AutoCompleteTextView) findViewById(R.id.editText_name);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.textView_userCategory);

        mPasswordView = (EditText) findViewById(R.id.editText_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDB_Users = FirebaseDatabase.getInstance().getReference().child("Users");
        mProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile_Pics");
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {
        final ProgressDialog progressDialog = new ProgressDialog(this);

        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String name = mNameView.getText().toString();
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(name)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            progressDialog.setMessage("Singing up ...");
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {

                                Toast.makeText(ActivitySignup.this, "Signing up failed", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();

                            }else if (task.isSuccessful()) {

                                // create new Firebase Databse Reference child for current user
                                final String user_id = mAuth.getCurrentUser().getUid();

                                // set username
                                mDB_Users.child(user_id).child(DbContract.USER_NAME).setValue(name);

                                mDB_Users.child(user_id).child(DbContract.PROFILE_PIC).setValue(DbContract.PROFILE_PIC_DEFAULT);

                                // store profile picture
                                if(mProfileImageUri != null) {
                                    mProfilePicsRef.child(user_id).putFile(mProfileImageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            @SuppressWarnings("VisibleForTests")
                                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                            mDB_Users.child(user_id).child(DbContract.PROFILE_PIC).setValue(downloadUrl);
                                            Log.e("kmd", downloadUrl+" -- ddddd");
                                        }
                                    });
                                }

                                // dismiss dialog and start MainActivity
                                progressDialog.dismiss();
                                Intent intent = new Intent(ActivitySignup.this, ActivityMain.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }

                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // get image from gallery and pass to CropImag
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri choosenImageUri = data.getData();
            CropImage.activity(choosenImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        // get cropped image uri and display
        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfileImageUri = cropResult.getUri();

                mImageButton.setImageURI(mProfileImageUri);
            }
        }
    }
}

