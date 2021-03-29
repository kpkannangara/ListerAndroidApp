package com.listerapp.lister.grocerystore.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.model.User;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;

import java.util.ArrayList;
import java.util.List;

public class AddUserFragment extends Fragment implements OnClickListener, AdapterView.OnItemSelectedListener {
    private static View view;
    private static EditText etEmail, etPassword;
    private static TextView login;
    private static Button signUpButton;
    private static CheckBox terms_conditions;
    User user;
    LocalStorage localStorage;
    Gson gson = new Gson();
    View progress;
    private Spinner spinner;
    String roleSpinner  = "Admin";

    private FirebaseAuth firebaseAuth;

    public AddUserFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_user_layout, container, false);
        localStorage = new LocalStorage(getContext());
        getActivity().setTitle("Add user");
        initViews();
        setListeners();
        return view;
    }

    // Initialize all views
    private void initViews() {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);

        signUpButton = view.findViewById(R.id.signUpBtn);
        login = view.findViewById(R.id.already_user);
        terms_conditions = view.findViewById(R.id.terms_conditions);

        firebaseAuth = FirebaseAuth.getInstance();

        spinner = view.findViewById(R.id.spinner);
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Admin");
        categories.add("User");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // Setting text selector over textviews
        @SuppressLint("ResourceType") XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(),
                    xrp);

            login.setTextColor(csl);
            terms_conditions.setTextColor(csl);
        } catch (Exception e) {
        }
    }

    // Set Listeners
    private void setListeners() {
        signUpButton.setOnClickListener(this);
//        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpBtn:
                addUser();
                break;
       }

    }

    private void addUser() {
        if(etEmail.getText().toString().equals("")){
            etEmail.setError("E-mail is empty");
            return;

        }
        if(etPassword.getText().toString().equals("")){
            etPassword.setError(" password is empty");
            return;

        }

        firebaseAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString()).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    userDetailsAdd(task);
                }else{
                    Log.i("1234", "User Already added, please try again"+task);
                }
            }
        });
    }

    private void userDetailsAdd(Task<AuthResult> task) {
        DatabaseReference userReference;
        userReference = FirebaseDatabase.getInstance().getReference().child("users").child(task.getResult().getUser().getUid());

        userReference.child("user_id").setValue(task.getResult().getUser().getUid());
        userReference.child("email").setValue(task.getResult().getUser().getEmail());
        userReference.child("role").setValue(spinner.getSelectedItem()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(getContext(), "Successfully added", Toast.LENGTH_LONG).show();
                }else{
                    Log.i("1234", "user added failed" );
                }
            }
        });
    }


    private void hideProgressDialog() {
        progress.setVisibility(View.GONE);
    }

    private void showProgressDialog() {
        progress.setVisibility(View.VISIBLE);
    }


    // spinner view functions
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        spinner.setSelection(i);
        roleSpinner = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        String item = adapterView.getItemAtPosition(0).toString();
        spinner.setSelection(0);
    }
}
