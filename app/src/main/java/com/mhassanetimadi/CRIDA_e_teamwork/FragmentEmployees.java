package com.mhassanetimadi.CRIDA_e_teamwork;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qa30m.m_teamwork.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by M-Qasim on 1/21/2017.
 */

public class FragmentEmployees extends Fragment {

    public static final int REQUEST_CODE = 1001;
    private RecyclerView mRecyclerView;
    private DatabaseReference mDbEmployeesRef;
    private Context mContext;
    private ArrayList<Employee> employeeArrayList = new ArrayList<>();
    private EmployeesAdapter employeesAdapter;
    private ChildEventListener eventListener;

    public FragmentEmployees() {
        // Required public empty constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        public RefreshTask() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Loading ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();
            addNewDbEventListener();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        employeeArrayList.clear();
        employeesAdapter.notifyDataSetChanged();

        if (requestCode == REQUEST_CODE && resultCode == ActivityAddEmployee.RESULT_OK) {
            RefreshTask task = new RefreshTask();
            task.execute();
        }else if (requestCode == REQUEST_CODE){
            mDbEmployeesRef.addChildEventListener(eventListener);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, null);

        setHasOptionsMenu(true);

        setUpRecyclerView(view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_employees_activity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_add_employee) {

            // stop event listener
            stopEventListener();

            // go to add employee activity
            Intent intent = new Intent(getActivity(), ActivityAddEmployee.class);
            startActivityForResult(intent, REQUEST_CODE);
        }

        return false;
    }

    private void stopEventListener() {
        mDbEmployeesRef.removeEventListener(eventListener);
    }

    private void setUpRecyclerView(View view) {

        // set up recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerNews);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        employeesAdapter = new EmployeesAdapter(getContext());
        mRecyclerView.setAdapter(employeesAdapter);

        // set up db
        mDbEmployeesRef = FirebaseDatabase.getInstance().getReference().child(DbContract.DB_EMPLOYEES);
        addNewDbEventListener();
    }

    private void addNewDbEventListener() {
        eventListener = mDbEmployeesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String curUser = getActivity().getSharedPreferences(DbContract.PREF_USER, Context.MODE_PRIVATE)
                        .getString(DbContract.USER_NAME, "no name");
                String managerUserName = dataSnapshot.child(DbContract.MANAGER_USER_NAME).getValue().toString();

                if (curUser.equals(managerUserName)) {
                    String firstName = dataSnapshot.child(DbContract.FIRST_NAME).getValue().toString();
                    String lastName = dataSnapshot.child(DbContract.LAST_NAME).getValue().toString();
                    String username = dataSnapshot.child(DbContract.USER_NAME).getValue().toString();
                    String userCategory = dataSnapshot.child(DbContract.USER_CATEGORY).getValue().toString();
                    String profilePic = dataSnapshot.child(DbContract.PROFILE_PIC).getValue().toString();
                    String key = dataSnapshot.getKey();

                    // add employee to list
                    Employee employee = new Employee(firstName, lastName, username, userCategory, profilePic, key);
                    employeeArrayList.add(employee);
                    employeesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i=0; i<employeeArrayList.size(); i++) {
                    String key = dataSnapshot.getKey();
                    if (key.equals(dataSnapshot.getKey())) {
                        employeeArrayList.remove(i);
                        employeesAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class EmployeesAdapter extends RecyclerView.Adapter<EmployeeVH> {

        LayoutInflater inflater;
        public EmployeesAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }
        @Override
        public EmployeeVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = inflater.inflate(R.layout.single_employee_layout, null);
            EmployeeVH employeeVh = new EmployeeVH(root);
            return employeeVh;
        }

        @Override
        public void onBindViewHolder(EmployeeVH holder, int position) {
            final Employee employee = employeeArrayList.get(position);
            holder.fullNameTextView.setText(employee.getFirstname() + ", " + employee.getLastname());
            holder.usernameTextView.setText(employee.getUsername());
            holder.userCategoryTextView.setText(employee.getCategory());
            if (!employee.getProfilePicture().equals(DbContract.PROFILE_PIC_DEFAULT)) {
                PicHelper.setImage(getContext(), employee.getProfilePicture(), holder.profilePicImageView);
            }

            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // stop event listener
                    stopEventListener();

                    // go to employee activity
                    Intent intent = new Intent(mContext, ActivityEmployee.class);
                    // put the post's key
                    intent.putExtra(DbContract.USER_DB_KEY, employee.getKey());
                    startActivityForResult(intent, REQUEST_CODE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return employeeArrayList.size();
        }
    }

    public static class EmployeeVH extends RecyclerView.ViewHolder {

        View mItemView;
        TextView fullNameTextView;
        TextView usernameTextView;
        TextView userCategoryTextView;
        ImageView profilePicImageView;

        public EmployeeVH(View itemView) {
            super(itemView);
            mItemView = itemView;

            fullNameTextView = (TextView) itemView.findViewById(R.id.fullNameTextView);
            usernameTextView = (TextView) itemView.findViewById(R.id.userNameTextView);
            userCategoryTextView = (TextView) itemView.findViewById(R.id.userCategoryTextView);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.profilePicImageView);
        }
    }
    class Employee {

        private String firstname;
        private String lastname;
        private String username;
        private String category;
        private String profilePicture;
        private String key;

        public Employee(String fn, String ln, String un, String ucat, String ppic, String key) {
            this.firstname = fn;
            this.lastname = ln;
            this.username = un;
            this.category = ucat;
            this.profilePicture = ppic;
            this.key = key;
        }
        public String getLastname() {
            return lastname;
        }

        public String getFirstname() {
            return firstname;
        }

        public String getCategory() {
            return category;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public String getUsername() {
            return username;
        }

        public String getKey() {
            return key;
        }
    }

}
