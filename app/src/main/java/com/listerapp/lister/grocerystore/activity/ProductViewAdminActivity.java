package com.listerapp.lister.grocerystore.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.model.Product;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ProductViewAdminActivity extends BaseActivity implements View.OnClickListener {
    String _title, _description, _price, _currency, _discount, _attribute, _category,_amount, _image;
    Spinner spinner;
    Button btnUpdate, btnAddImage, btnDeleteProduct;
    ImageView imgProduct;
    EditText etProductName, etDescription, etPrice, etAmount;

    private final static int Gallery_pick = 1;

    private DatabaseReference mDatabaseCategoryRef;
    private StorageReference mStorageCategoryRef;
    private StorageTask mUploadTask;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view_admin);

        Intent intent = getIntent();

//        _id = intent.getStringExtra("id");
        _title = intent.getStringExtra("title");
        _image = intent.getStringExtra("image");
        _description = intent.getStringExtra("description");
        _price = intent.getStringExtra("price");
        _currency = intent.getStringExtra("currency");
        _discount = intent.getStringExtra("discount");
        _attribute = intent.getStringExtra("attribute");
        _category = intent.getStringExtra("category");
        _amount = intent.getStringExtra("amount");

        spinner = findViewById(R.id.spinner);
        // Spinner click listener
//        spinner.setOnItemSelectedListener(this);
        mStorageCategoryRef = FirebaseStorage.getInstance().getReference("category");
        mDatabaseCategoryRef = FirebaseDatabase.getInstance().getReference("category");

        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);

        btnAddImage = findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(this);

        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
        btnDeleteProduct.setOnClickListener(this);

        etProductName = findViewById(R.id.etProductName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etAmount = findViewById(R.id.etAmount);
        imgProduct = findViewById(R.id.imgProduct);


        etProductName.setText(_title);
        etPrice.setText(_price);
        etAmount.setText(_amount);

        Picasso.get()
                .load(_image)
                .into(imgProduct, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnUpdate:

                Log.i("1234", "description :"+etDescription.getText().toString());
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

                updateProductFirebase();
                break;

            case R.id.btnAddImage:
                openFileChooser();
                break;
            case R.id.btnDeleteProduct:
                deleteProduct();
                break;
        }
    }

    private void showError2(String msg, final boolean b) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
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

    private void deleteProduct() {
        DatabaseReference mDatabaseCategoryRef2 = mDatabaseCategoryRef.child(_category).child("Items").child(etProductName.getText().toString());
        mDatabaseCategoryRef2.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                        }
                    }
                });
    }

    private void updateProductFirebase() {
        if (cropResultUri != null) {
            final ProgressDialog pd = new ProgressDialog(this);
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
                            Toast.makeText(ProductViewAdminActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
//                            Upload upload = new Upload(etProductName.getText().toString().trim(),
//                                    taskSnapshot.getUploadSessionUri().toString());
                            Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Product upload = new Product(_category, etProductName.getText().toString(), etDescription.getText().toString(), etPrice.getText().toString(), uri.toString(), etAmount.getText().toString());
                                    mDatabaseCategoryRef.child(_category).child("Items").child(etProductName.getText().toString()).setValue(upload);
                                    showError2("Successfully updated", true);
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(ProductViewAdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            DatabaseReference mDatabaseCategoryRef2 = mDatabaseCategoryRef.child(_category).child("Items").child(etProductName.getText().toString());
            mDatabaseCategoryRef2.child("name").setValue(etProductName.getText().toString());
            mDatabaseCategoryRef2.child("price").setValue(etPrice.getText().toString());
            mDatabaseCategoryRef2.child("description").setValue(etDescription.getText().toString());
            mDatabaseCategoryRef2.child("category").setValue(_category);
            mDatabaseCategoryRef2.child("amount").setValue(etAmount.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        try{
                            showError2("Successfully updated", true);
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