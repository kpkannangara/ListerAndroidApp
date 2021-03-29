package com.listerapp.lister.grocerystore.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.model.Product;
import com.listerapp.lister.grocerystore.model.Upload;
import com.listerapp.lister.grocerystore.model.User;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    EditText etUserName, etEmail, etMobile, etRole;
    ImageView profileImage;
    Button etSave;

    private FirebaseAuth mAuth;
    String userFirebaseID;
    DatabaseReference mDatabaseProfileRef;
    private StorageReference mStorageProfileRef;
    private StorageTask mUploadTask;
    String previousImageURL = "";

    private final static int Gallery_pick = 1;

    LocalStorage localStorage;
    Gson gson = new Gson();

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etUserName = view.findViewById(R.id.etUserName);
        etEmail = view.findViewById(R.id.etEmail);
        etMobile = view.findViewById(R.id.etMobile);
        etRole = view.findViewById(R.id.etRole);
        profileImage = view.findViewById(R.id.profileImage);
        etSave = view.findViewById(R.id.etSave);
        etSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserDetails();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        userFirebaseID = mAuth.getCurrentUser().getUid();

//        Log.i("profile"," user details: "+mAuth.getCurrentUser().getUid());
//        Log.i("profile"," user details: "+mAuth.getCurrentUser().getEmail());

        mStorageProfileRef = FirebaseStorage.getInstance().getReference("user_Image");
        mDatabaseProfileRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        mDatabaseProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                Log.i("profile", "username "+currentUser.getFname());

                previousImageURL = currentUser.getImage();
                Picasso.get()
                        .load(currentUser.getImage())
                        .into(profileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                            }
                        });

                etUserName.setText(currentUser.getFname());
                etEmail.setText(currentUser.getEmail());
                etMobile.setText(""+currentUser.getMobile());
                etRole.setText(currentUser.getRole());


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });



        return view;
    }

    private void openFileChooser() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, Gallery_pick);
    }

    Uri cropResultUri;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null && data.getData() !=null){
            cropResultUri = data.getData();
            Picasso.get().load(cropResultUri).into(profileImage);
        }
    }

    private void saveUserDetails() {
        if (cropResultUri != null) {
            final ProgressDialog pd = new ProgressDialog(getContext());
            pd.setTitle("Saving data...");
            pd.show();
            StorageReference fileReference = mStorageProfileRef.child(System.currentTimeMillis()
                    + ".jpg");
            mUploadTask = fileReference.putFile(cropResultUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                    mProgressBar.setProgress(0);
                                    pd.setMessage("Upload successful");
                                }
                            }, 500);
                            pd.dismiss();
                            Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_LONG).show();
//                            Upload upload = new Upload(etProductName.getText().toString().trim(),
//                                    taskSnapshot.getUploadSessionUri().toString());
                            Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    User user = new User(mAuth.getCurrentUser().getUid(), etUserName.getText().toString(), etMobile.getText().toString(), etEmail.getText().toString(), uri.toString(), etRole.getText().toString() , 0);
                                    mDatabaseProfileRef.setValue(user);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            pd.setMessage("uploading: "+(int) progress+" %");
                        }
                    });
        } else {
            mDatabaseProfileRef.child("id").setValue(mAuth.getCurrentUser().getUid());
            mDatabaseProfileRef.child("fname").setValue(etUserName.getText().toString());
            mDatabaseProfileRef.child("mobile").setValue(etMobile.getText().toString());
            mDatabaseProfileRef.child("email").setValue(etEmail.getText().toString());
            mDatabaseProfileRef.child("image").setValue(previousImageURL);
            mDatabaseProfileRef.child("phonenumber").setValue(0);
            mDatabaseProfileRef.child("role").setValue(etRole.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        try{
                            Toast.makeText(getContext(), "Successfully added", Toast.LENGTH_LONG).show();

                        }catch (Exception e){
                            Log.i("1234", "profile fragment exception "+e);
                        }

                    }else{
                        Log.i("1234", "user added failed" );
                    }
                }
            });



        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Profile");
    }
}
