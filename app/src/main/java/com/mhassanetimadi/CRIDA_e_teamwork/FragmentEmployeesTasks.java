package com.mhassanetimadi.CRIDA_e_teamwork;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.qa30m.m_teamwork.R;

import java.util.ArrayList;


/**
 * Created by M-Qasim on 1/21/2017.
 */

public class FragmentEmployeesTasks extends Fragment {

    public static final int REQUEST_CODE = 1003;
    public static final String EXTRA_KEY = "key";
    private ArrayList<Task> taskArrayList = new ArrayList<>();
    private EmployeesAdapter tasksAdapter;
    private ChildEventListener eventListener;
    private RecyclerView mRecyclerView;
    private DatabaseReference mDatabaseRef;
    private Context mContext;

    public FragmentEmployeesTasks() {
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

        taskArrayList.clear();
        tasksAdapter.notifyDataSetChanged();

        if (requestCode == REQUEST_CODE && resultCode == ActivityAddEmployee.RESULT_OK) {
            RefreshTask task = new RefreshTask();
            task.execute();
        }else if (requestCode == REQUEST_CODE){
            mDatabaseRef.addChildEventListener(eventListener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, null);

        setHasOptionsMenu(true);

        setUpRecyclerView(view);

        return view;
    }

    private void setUpRecyclerView(View view) {

        // set up recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerNews);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tasksAdapter = new EmployeesAdapter(getContext());
        mRecyclerView.setAdapter(tasksAdapter);

        // set up db
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child(DbContract.DB_TASKS);
        addNewDbEventListener();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_task, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_task) {

            // stop event listener
            stopEventListener();

            Intent intent = new Intent(getContext(), ActivityAddTask.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewDbEventListener() {
        eventListener = mDatabaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String curUser = mContext.getSharedPreferences(DbContract.PREF_USER, Context.MODE_PRIVATE)
                        .getString(DbContract.USER_NAME, "no name");

                String managerUserName = dataSnapshot.child(DbContract.MANAGER_USER_NAME).getValue().toString();

                if (curUser.equals(managerUserName)) {
                    String title = dataSnapshot.child(ActivityAddTask.TITLE).getValue().toString();
                    String description = dataSnapshot.child(ActivityAddTask.DESCRIPTION).getValue().toString();
                    String to = dataSnapshot.child(ActivityAddTask.TO).getValue().toString();
                    String key = dataSnapshot.getKey();

                    // add employee to list
                    Task task = new Task(title, description, to, key, managerUserName);
                    taskArrayList.add(task);
                    tasksAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (int i=0; i<taskArrayList.size(); i++) {
                    String key = dataSnapshot.getKey();
                    if (key.equals(dataSnapshot.getKey())) {
                        taskArrayList.remove(i);
                        tasksAdapter.notifyDataSetChanged();
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


    private void stopEventListener() {
        mDatabaseRef.removeEventListener(eventListener);
    }

    private class EmployeesAdapter extends RecyclerView.Adapter<TaskVH> {

        LayoutInflater inflater;

        public EmployeesAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public TaskVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View root = inflater.inflate(R.layout.single_task_layout, null);
            TaskVH postVH = new TaskVH(root);
            return postVH;
        }

        @Override
        public void onBindViewHolder(TaskVH holder, int position) {
            final Task task = taskArrayList.get(position);
            holder.mTitleTextView.setText(task.getPostTitle());
            holder.mDescTextView.setText(task.getPostDescription());

            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // stop event listener
                    stopEventListener();

                    // go to employee activity
                    Intent intent = new Intent(mContext, ActivityTaskDetial.class);
                    // put the post's key
                    intent.putExtra(EXTRA_KEY, task.getKey());
                    startActivityForResult(intent, REQUEST_CODE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return taskArrayList.size();
        }
    }

    public static class TaskVH extends RecyclerView.ViewHolder {

        View mItemView;
        TextView mTitleTextView;
        TextView mDescTextView;

        public TaskVH(View itemView) {
            super(itemView);
            mItemView = itemView;

            mTitleTextView = (TextView) itemView.findViewById(R.id.post_title_textView);
            mDescTextView = (TextView) itemView.findViewById(R.id.post_desc_textView);
        }
    }

    class Task {

        private String postTitle;
        private String postDescription;
        private String deadline;
        private String employeeName;
        private String manager;
        private String key;

        public Task(String postTitle, String postDescription, String employeeName, String key, String manager) {
            this.postTitle = postTitle;
            this.postDescription = postDescription;
            this.deadline = deadline;
            this.employeeName = employeeName;
            this.manager = manager;
            this.key = key;
        }

        public String getPostTitle() {
            return postTitle;
        }

        public String getPostDescription() {
            return postDescription;
        }

        public String getDeadline() {
            return deadline;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public String getKey() {
            return key;
        }
    }
}
