package com.mhassanetimadi.CRIDA_e_teamwork;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qa30m.m_teamwork.R;

public class ActivityTaskDetial extends AppCompatActivity implements View.OnClickListener {

//    public static final String EXTRA_KEY = "extra_key";
//    public static final String EXTRA_DESCRIPTION = "extra_description";
//    public static final String EXTRA_TITLE = "extra_title";
//    public static final String EXTRA_IMAGE = "extra_image";
    public static final String TASK_REPLIES = "replies";
    public static final String REPLY_TEXT = "replyText";

    private EditText commentEditText;
    private Button addCommentButton;
    private RecyclerView commentsRecyclerView;
    private TextView postTitleTextView;
    private TextView numberOfCommentsTextView;
    private TextView postDescTextView;
    ProgressDialog dialog;

    private DatabaseReference curTaskDbRef;
    private DatabaseReference curTaskRepliesDbRef;
//    private DatabaseReference curUserDbRef;
    private String key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detial);

        // initialize views
        initViews();

        addCommentButton.setOnClickListener(this);

        key = getIntent().getStringExtra(FragmentMyTasks.EXTRA_KEY);
        curTaskDbRef = FirebaseDatabase.getInstance().getReference().child(DbContract.DB_TASKS).child(key);
        curTaskRepliesDbRef = curTaskDbRef.child(TASK_REPLIES);

        setTaskDetails();

        setTaskReplies();

        // set up toolbar
        setUpToolbar();
    }

    private void setTaskDetails() {
        curTaskDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // set profile pic
                String title = dataSnapshot.child(ActivityAddTask.TITLE).getValue().toString();
                postTitleTextView.setText(title);

                String description = dataSnapshot.child(ActivityAddTask.DESCRIPTION).getValue().toString();
                postDescTextView.setText(description);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTaskReplies() {
        // set number of comments text
        curTaskRepliesDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                numberOfCommentsTextView.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // set comments recyclerView
        FirebaseRecyclerAdapter<Reply, ReplyVH> commentsAdapter = new FirebaseRecyclerAdapter<Reply, ReplyVH>(
            Reply.class, R.layout.single_reply_layout, ReplyVH.class, curTaskRepliesDbRef
        ) {
            @Override
            protected void populateViewHolder(final ReplyVH viewHolder, final Reply model, int position) {

                viewHolder.mUsernameTextView.setText(model.getUserName());
                viewHolder.mCommentTextView.setText(model.getReplyText());
                if (!model.getUserProfilePic().equals(DbContract.PROFILE_PIC_DEFAULT)) {
                    PicHelper.setImage(ActivityTaskDetial.this, model.getUserProfilePic(), viewHolder.mProfilePicImageView);
                }
            }
        };

        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Task Detail");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Task")
                    .setMessage("Delete this task permanently?")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            curTaskDbRef.removeValue();
                            ActivityTaskDetial.this.finish();
                        }
                    }).setNegativeButton("Cancel", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        postTitleTextView = (TextView) findViewById(R.id.post_title_textView);
        postDescTextView = (TextView) findViewById(R.id.post_desc_textView);
        numberOfCommentsTextView = (TextView) findViewById(R.id.number_of_comment_textView);
        commentEditText = (EditText) findViewById(R.id.et_comment);
        addCommentButton = (Button) findViewById(R.id.btn_add_comment);
        commentsRecyclerView = (RecyclerView) findViewById(R.id.comments_recyclerView);
        dialog = new ProgressDialog(ActivityTaskDetial.this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_add_comment) {
            addComment();
        }

    }

    private void addComment() {
        String replyText = commentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(replyText)) {
            Toast.makeText(ActivityTaskDetial.this, "Enter some text", Toast.LENGTH_LONG).show();
            return;
        }

        // get the username and profilePicUrl for current user
        dialog.setMessage("Posting comment");
        dialog.show();

        DatabaseReference newReplyDb = curTaskRepliesDbRef.push();

        // get the commenter user name and profile pic from shared preferences
        String userName = getPreferences(MODE_PRIVATE).getString(DbContract.USER_NAME, null);
        String profilePicUrl = getPreferences(MODE_PRIVATE).getString(DbContract.PROFILE_PIC, DbContract.PROFILE_PIC_DEFAULT);

        newReplyDb.child(DbContract.USER_NAME).setValue(userName);
        newReplyDb.child(DbContract.PROFILE_PIC).setValue(profilePicUrl);
        newReplyDb.child(REPLY_TEXT).setValue(replyText).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (task.isSuccessful()) {
                    commentEditText.setText("");
                }else {
                    Toast.makeText(ActivityTaskDetial.this, "Couldn't add reply, check your internet",
                            Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });

    }

    static class ReplyVH extends RecyclerView.ViewHolder {

        View mItemView;
        TextView mUsernameTextView;
        TextView mCommentTextView;
        ImageView mProfilePicImageView;
        View viewCommentBorder;

        public ReplyVH(View itemView) {
            super(itemView);
            mItemView = itemView;
            mUsernameTextView = (TextView) itemView.findViewById(R.id.userNameTextView);
            mCommentTextView = (TextView) itemView.findViewById(R.id.commentTextView);
            mProfilePicImageView = (ImageView) itemView.findViewById(R.id.profilePicImageView);
            viewCommentBorder = itemView.findViewById(R.id.comment_border);
        }
    }

    public static class Reply {
        public String name;
        public String replyText;
        public String profile_pic;

        public Reply() {
            //Required empty no-argument constructor
        }

        public Reply(String userName, String replyText, String userProfilePic) {
            this.name = userName;
            this.replyText = replyText;
            this.profile_pic = userProfilePic;
        }

        public String getUserName() {
            return name;
        }

        public String getReplyText() {
            return replyText;
        }

        public String getUserProfilePic() {
            return profile_pic;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setReplyText(String replyText) {
            this.replyText = replyText;
        }

        public void setProfile_pic(String profile_pic) {
            this.profile_pic = profile_pic;
        }
    }
}
