package com.mhassanetimadi.CRIDA_e_teamwork;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.android.gms.tasks.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qa30m.m_teamwork.R;

public class Employee_Register extends AppCompatActivity implements View.OnClickListener {

    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String PASSWORD = "password";
    public static final String USERNAME = "username";
    public static final String GENDER = "gender";
    private Button btnadd,btncancel;
   EditText etFirstName,ettLastname ,etaddress,etemail,etusername, etphone, etpassword;
    RadioButton radioMale,radioFemale;
    RadioGroup radioGroupGender;
    private Spinner spinnerUserType;
    private Spinner spinnerManger;

    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee__register);

        btnadd =(Button)findViewById(R.id.btn_register);
        btncancel =(Button)findViewById(R.id.btn_cancel);
        etFirstName = (EditText)findViewById(R.id.editTextFirstName);
        ettLastname = (EditText)findViewById(R.id.editTextLastName);
        etaddress =(EditText)findViewById(R.id.editTextAddress);
        etusername =(EditText)findViewById(R.id.editTextUsername);
        etemail =(EditText)findViewById(R.id.editTextEmail);
        etphone =(EditText)findViewById(R.id.editTextPhone);
        etpassword =(EditText)findViewById(R.id.editTextPassword);

        radioGroupGender = (RadioGroup) findViewById(R.id.radioGender);

        spinnerUserType = (Spinner) findViewById(R.id.spinner_user_type);
        spinnerManger = (Spinner) findViewById(R.id.spinner_manger);
        ArrayAdapter userTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Senior Manger", "Manger", "Employee"});
        spinnerUserType.setAdapter(userTypeAdapter);

        reference = FirebaseDatabase.getInstance().getReference().child("employees");

        btnadd.setOnClickListener(this);
        btncancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_register) {
            String firstName = etFirstName.getText().toString();
            String lastname = ettLastname.getText().toString();
            String email = etemail.getText().toString();
            String phone = etphone.getText().toString();
            String address = etaddress.getText().toString();
            String password = etpassword.getText().toString();
            String username = etusername.getText().toString();
            String gender;

            int checkedId = radioGroupGender.getCheckedRadioButtonId();
            if (checkedId == R.id.radioMale){
                gender = "male";
            } else {
                gender = "female";
            }

            DatabaseReference newEmpRef = reference.child(username);
            newEmpRef.child(FIRSTNAME).setValue(firstName);
            newEmpRef.child(LASTNAME).setValue(lastname);
            newEmpRef.child(EMAIL).setValue(email);
            newEmpRef.child(PHONE).setValue(phone);
            newEmpRef.child(ADDRESS).setValue(address);
            newEmpRef.child(PASSWORD).setValue(password);
            newEmpRef.child(GENDER).setValue(gender);
            newEmpRef.child(USERNAME).setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Employee_Register.this.finish();
                }
            });
        } else if(view.getId() == R.id.btn_cancel) {
            this.finish();
        }
    }
}
