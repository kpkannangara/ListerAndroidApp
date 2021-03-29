package com.listerapp.lister.grocerystore.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.adapter.CategoryAdapter;
import com.listerapp.lister.grocerystore.adapter.FavouriteAdapter;
import com.listerapp.lister.grocerystore.model.Product;
import com.listerapp.lister.grocerystore.model.Upload;

import java.util.ArrayList;
import java.util.List;


public class FavouriteFragment extends Fragment {

    RecyclerView favourite_rv;

    public FavouriteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    List<Product> productList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_favourite, container, false);

        getActivity().setTitle("Favourite");

        favourite_rv = v.findViewById(R.id.favourite_rv);
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabaseFavourite = FirebaseDatabase.getInstance().getReference("favourite").child(id);
        mDatabaseFavourite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Product upload = postSnapshot.getValue(Product.class);
                    productList.add(upload);
                }
                setupCategoryRecycleView();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return v;
    }

    private void setupCategoryRecycleView() {
        FavouriteAdapter mAdapter = new FavouriteAdapter(productList, getContext(), "Category");
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        favourite_rv.setLayoutManager(mLayoutManager);
        favourite_rv.setItemAnimator(new DefaultItemAnimator());
        favourite_rv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
}