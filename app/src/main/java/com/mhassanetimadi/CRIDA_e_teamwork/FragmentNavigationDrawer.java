package com.mhassanetimadi.CRIDA_e_teamwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.qa30m.m_teamwork.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M-Qasim on 1/19/2017.
 */

public class FragmentNavigationDrawer extends Fragment {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Context mContext;

    private OnFragmentListInteractionListener mListener;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public FragmentNavigationDrawer() {
        // required public empty constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

        if (context instanceof OnFragmentListInteractionListener) {
            mListener = (OnFragmentListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFragmentListInteractionListener {
        void onFragmentListInteraction(int index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_navigation_drawer, null);

        setUpNavigationHeader(root);

        setupRecyclerView(root);

        return root;
    }

    private void setUpNavigationHeader(View root) {

        final View navHeader = root.findViewById(R.id.nav_header_container);
        navHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserAccount();
            }
        });

        final ImageView userPicImageView = (ImageView) navHeader.findViewById(R.id.user_pic_imageView);
        final TextView userNameTextView = (TextView) navHeader.findViewById(R.id.textView_userName);
        final TextView userLevelTextView = (TextView) navHeader.findViewById(R.id.textView_userCategory);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {

                    SharedPreferences sharedPreferences = FragmentNavigationDrawer.this.getActivity()
                            .getSharedPreferences(DbContract.PREF_USER, Context.MODE_PRIVATE);
                    String user_key = sharedPreferences.getString(DbContract.USER_DB_KEY, "");
                    String userLevelString = sharedPreferences.getString(DbContract.USER_CATEGORY, "no level");
                    int userLevel = 0;

                    for (int i=0; i<DbContract.USER_LEVELS.length; i++) {
                        if (DbContract.USER_LEVELS[i].equals(userLevelString)) {
                            userLevel = i;
                        }
                    }

                    DatabaseReference curUserDb = FirebaseDatabase.getInstance().getReference()
                            .child(DbContract.DB_EMPLOYEES).child(user_key);
                    curUserDb.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String prifilePicUrl = DbContract.PROFILE_PIC_DEFAULT;

                            // handle profile pic null pointer exception
                            if (dataSnapshot.child(DbContract.PROFILE_PIC).getValue() != null) {
                                prifilePicUrl = dataSnapshot.child(DbContract.PROFILE_PIC).getValue().toString();
                            }
                            if (!prifilePicUrl.equals(DbContract.PROFILE_PIC_DEFAULT)) {
                                PicHelper.setImage(mContext, prifilePicUrl, userPicImageView);
                            }

                            // handle null pointer exception
                            String userName = "User";
                            if (dataSnapshot.child(DbContract.USER_NAME).getValue() != null) {
                                userName = dataSnapshot.child(DbContract.USER_NAME).getValue().toString();
                            }
                            userNameTextView.setText(userName);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    userLevelTextView.setText(DbContract.USER_LEVELS[userLevel]);
                }
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    private void openUserAccount() {
        Intent intent = null;

        if(mAuth.getCurrentUser() == null) {
            intent = new Intent(mContext, ActivitySignin.class);
        } else {
            intent = new Intent(mContext, ActivityEmployee.class);
            String key = mContext.getSharedPreferences(DbContract.PREF_USER, Context.MODE_PRIVATE)
                    .getString(DbContract.USER_DB_KEY, "nokey");
            Log.d("******* Key FND ******", key);
            intent.putExtra(DbContract.USER_DB_KEY, key);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }

        startActivity(intent);
    }

    private void setupRecyclerView(View root) {

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.drawerList);
        recyclerView.setAdapter(new NavigationDrawerAdapter(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setUpDrawer(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {

        mDrawerLayout = drawerLayout;

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.app_name, R.string.app_name){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    private class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyHolder>{

        private List<NavigationDrawerListItem> mDataList = new ArrayList<>();
        private LayoutInflater mInflater;

        public NavigationDrawerAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);

            String[] sections = getResources().getStringArray(R.array.sections);
            int[] imageIds = {R.drawable.ic_task2, R.drawable.ic_employees, R.drawable.ic_task_report};

            for (int i=0; i<sections.length; i++) {
                NavigationDrawerListItem item = new NavigationDrawerListItem();
                item.setTitleText(sections[i]);
                item.setImageResourceId(imageIds[i]);
                mDataList.add(item);
            }
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.navigation_drawer_list_item, null);
            MyHolder holder = new MyHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyHolder holder, final int position) {
            NavigationDrawerListItem dataItem = mDataList.get(position);

            holder.mImageView.setImageResource(dataItem.getImageResourceId());
            holder.mTextView.setText(dataItem.getTitleText());
            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onFragmentListInteraction(position);
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        public class MyHolder extends RecyclerView.ViewHolder{
            ImageView mImageView;
            TextView mTextView;
            View mItemView;

            public MyHolder(View itemView) {
                super(itemView);
                mItemView = itemView;
                mImageView = (ImageView) itemView.findViewById(R.id.navigation_drawer_imageView);
                mTextView = (TextView) itemView.findViewById(R.id.navigation_drawer_textView);
            }
        }
    }

    public class NavigationDrawerListItem {

        private int imageResourceId;
        private String titleText;

        public NavigationDrawerListItem(){}

        public int getImageResourceId() {
            return imageResourceId;
        }

        public void setImageResourceId(int imageResourceId) {
            this.imageResourceId = imageResourceId;
        }

        public String getTitleText() {
            return titleText;
        }

        public void setTitleText(String titleText) {
            this.titleText = titleText;
        }
    }
}
