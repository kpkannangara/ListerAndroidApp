package com.listerapp.lister.grocerystore.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.adapter.CategoryAdapter;
import com.listerapp.lister.grocerystore.adapter.UsersAdapter;
import com.listerapp.lister.grocerystore.model.Upload;
import com.listerapp.lister.grocerystore.model.User;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

    private DatabaseReference mDatabaseUserRef;
    private List<User> mUploads;
    private UsersAdapter usersAdapter;
    private RecyclerView recyclerView;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = v.findViewById(R.id.users_rv);

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        getActivity().setTitle("Users");

        mUploads = new ArrayList<>();
        mDatabaseUserRef = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    if(!currentUser.getUid().toString().equals(user.getId())){
                        mUploads.add(user);
                    }
                }
                setupCategoryRecycleView();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
        return v;
    }

    private void setupCategoryRecycleView() {
        usersAdapter = new UsersAdapter(mUploads, getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(usersAdapter);
        usersAdapter.notifyDataSetChanged();

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CALL_PHONE},
                1);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted, yay! Do the
            // contacts-related task you need to do.
        } else {

            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Toast.makeText(getActivity(), "Permission denied to call", Toast.LENGTH_SHORT).show();
        }
        return;
    }
}