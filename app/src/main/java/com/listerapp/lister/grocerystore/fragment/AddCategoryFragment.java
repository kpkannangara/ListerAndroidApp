package com.listerapp.lister.grocerystore.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.model.Upload;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

public class AddCategoryFragment extends Fragment implements View.OnClickListener {

    private final static int Gallery_pick = 1;
    private ImageView imgCategory;
    private Button btnAddImage, btnsaveCategory;
    private EditText etProductName;

    private StorageReference mStorageCategoryRef;
    private DatabaseReference mDatabaseCategoryRef;
    private StorageTask mUploadTask;

    public AddCategoryFragment() {
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

        View view = inflater.inflate(R.layout.fragment_add_category, container, false);
        imgCategory = view.findViewById(R.id.imgCategory);
        btnAddImage = view.findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(this);
        btnsaveCategory = view.findViewById(R.id.btnsaveCategory);
        btnsaveCategory.setOnClickListener(this);
        getActivity().setTitle("Add Category");


        imgCategory.setOnClickListener(this);

        etProductName = view.findViewById(R.id.etProductName);
        // ImageView in your Activity

        mStorageCategoryRef = FirebaseStorage.getInstance().getReference("category");
        mDatabaseCategoryRef = FirebaseDatabase.getInstance().getReference("category");




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
           Picasso.get().load(cropResultUri).into(imgCategory);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnAddImage:
                openFileChooser();
                break;

            case R.id.btnsaveCategory:
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadCategoryFirebase();
                }
                break;
            case R.id.imgCategory:
                break;
        }
    }

    private void uploadCategoryFirebase() {
        if (cropResultUri != null) {
           final ProgressDialog pd = new ProgressDialog(getContext());
            pd.setTitle("Saving data...");
            pd.show();
            StorageReference fileReference = mStorageCategoryRef.child(System.currentTimeMillis()
                    + ".jpg");
            mUploadTask = fileReference.putFile(cropResultUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                   pd.setMessage("Upload successful");
                                }
                            }, 500);
                            pd.dismiss();
                            Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_LONG).show();
                           Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Upload upload = new Upload(etProductName.getText().toString().trim(),
                                            uri.toString());
                                    String uploadId = mDatabaseCategoryRef.push().getKey();
                                    mDatabaseCategoryRef.child(etProductName.getText().toString()).setValue(upload).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            showError2("Successfully uploaded", false);
                                        }
                                    });
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
            Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void showError2(String msg, final boolean b) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("")
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();


                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


}