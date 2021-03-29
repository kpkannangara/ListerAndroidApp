package com.listerapp.lister.grocerystore.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.model.Product;
import com.listerapp.lister.grocerystore.model.Upload;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AddProductFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    Spinner spinner;
    private DatabaseReference mDatabaseCategoryRef;
    private StorageReference mStorageCategoryRef;
    private StorageTask mUploadTask;
    String categorySpinner  = "";

    EditText etProductName, etDescription, etPrice, etAmount;
    ImageView imgProduct;

    final List<String> categoryName = new ArrayList<String>();

    Button addProduct, btnAddImage;
    private final static int Gallery_pick = 1;

    public AddProductFragment() {
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
        View v = inflater.inflate(R.layout.fragment_add_product, container, false);
        getActivity().setTitle("Add Product");

        spinner = v.findViewById(R.id.spinner);
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        mStorageCategoryRef = FirebaseStorage.getInstance().getReference("category");
        mDatabaseCategoryRef = FirebaseDatabase.getInstance().getReference("category");

        setUpSpinner();

        addProduct = v.findViewById(R.id.addProduct);
        addProduct.setOnClickListener(this);

        btnAddImage = v.findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(this);

        etProductName = v.findViewById(R.id.etProductName);
        etDescription = v.findViewById(R.id.etDescription);
        etPrice = v.findViewById(R.id.etPrice);
        etAmount = v.findViewById(R.id.etAmount);
        imgProduct = v.findViewById(R.id.imgProduct);




        return v;
    }
    private void setUpSpinner() {
        // Spinner Drop down elements
        categoryName.clear();
        mDatabaseCategoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    categoryName.add(upload.getName());
                }
                try{
                    // Creating adapter for spinner
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categoryName);
                    // Drop down layout style - list view with radio button
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // attaching data adapter to spinner
                    spinner.setAdapter(dataAdapter);
                }catch (Exception e){
                    Log.i("1234", "add product exxception "+e);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    // spinner view functions
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        spinner.setSelection(i);
        categorySpinner = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        String item = adapterView.getItemAtPosition(0).toString();
        spinner.setSelection(0);
        categorySpinner = item;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addProduct:
                if(etProductName.getText().toString().equals("")){
                    showError2("Please enter product name",true);
                    return;
                }
                if(etDescription.getText().toString().equals("")){
                    showError2("Please enter description",true);
                    return;
                }
                if(etPrice.getText().toString().equals("")){
                    showError2("Please enter price",true);
                    return;
                }
                if(etAmount.getText().toString().equals("")){
                    showError2("Please enter amount",true);
                    return;
                }

                uploadProductFirebase();
                break;

            case R.id.btnAddImage:
                openFileChooser();
                break;
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
    private void uploadProductFirebase() {
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
//                                    mProgressBar.setProgress(0);
                                    pd.setMessage("Upload successful");
                                }
                            }, 500);
                            pd.dismiss();
//                            Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_LONG).show();
//                            Upload upload = new Upload(etProductName.getText().toString().trim(),
//                                    taskSnapshot.getUploadSessionUri().toString());
                            Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Product upload = new Product(categorySpinner, etProductName.getText().toString(), etDescription.getText().toString(), etPrice.getText().toString(), uri.toString(), etAmount.getText().toString());
                                    mDatabaseCategoryRef.child(categorySpinner).child("Items").child(etProductName.getText().toString()).setValue(upload);
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
            Picasso.get().load(cropResultUri).into(imgProduct);
        }
    }
}