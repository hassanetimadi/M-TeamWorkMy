package com.mhassanetimadi.CRIDA_e_teamwork;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qa30m.m_teamwork.R;

public class ActivityEmployee extends AppCompatActivity {

    ImageView iv_profile;
    TextView tv_userName;
    TextView tv_email;
    TextView tv_category;
    Toolbar toolbar;
    private DatabaseReference curUserDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);
        initViews();

        // user name and profile pic
        String key = getIntent().getStringExtra(DbContract.USER_DB_KEY);
        Log.d("******* Key EMP ******", key);
        curUserDb = FirebaseDatabase.getInstance().getReference()
                .child(DbContract.DB_EMPLOYEES).child(key);

        curUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // set profile pic
                String profilePicUrl = dataSnapshot.child(DbContract.PROFILE_PIC).getValue().toString();
                if (!profilePicUrl.equals(DbContract.PROFILE_PIC_DEFAULT)) {
                    PicHelper.setImage(ActivityEmployee.this, profilePicUrl, iv_profile);
                }

                // set name
                String firstName = dataSnapshot.child(DbContract.FIRST_NAME).getValue().toString();
                tv_userName.setText(firstName);

                String category = dataSnapshot.child(DbContract.USER_CATEGORY).getValue().toString();
                tv_category.setText(category);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        setupToolbarMenu();

    }

    private void setupToolbarMenu() {
        toolbar.inflateMenu(R.menu.menu_employee_activity);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_delete_employee) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEmployee.this);
                    builder.setTitle("Delete Employee")
                            .setMessage("Delete this employee?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    curUserDb.removeValue();
                                    ActivityEmployee.this.setResult(RESULT_OK);
                                    ActivityEmployee.this.finish();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show();
                }
                return false;
            }
        });
    }

    private void initViews() {
        iv_profile = (ImageView) findViewById(R.id.imageView_profile);
        tv_userName = (TextView) findViewById(R.id.editText_username);
        tv_email = (TextView) findViewById(R.id.textView_Email);
        tv_category = (TextView) findViewById(R.id.textView_userCategory);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

    }
}
