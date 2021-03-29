package com.listerapp.lister.grocerystore.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.activity.CartActivity;
import com.listerapp.lister.grocerystore.adapter.CartAdapter;
import com.listerapp.lister.grocerystore.adapter.OdersAdapter;
import com.listerapp.lister.grocerystore.model.Cart;
import com.listerapp.lister.grocerystore.model.Orders;
import com.listerapp.lister.grocerystore.model.Product;
import com.listerapp.lister.grocerystore.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class OrderFragment extends Fragment {

    RecyclerView order_rv;
    List<Cart> orderList = new ArrayList<>();
    private DatabaseReference mDatabaseOrderRef;

    public OrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    List<Orders> OorderList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_order, container, false);

        getActivity().setTitle("Orders");

        mDatabaseOrderRef = FirebaseDatabase.getInstance().getReference("order");
        mDatabaseOrderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OorderList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    final Orders orders = new Orders();
                    orders.setDate(postSnapshot.getKey());
                    final List<Cart> productList = new ArrayList<>();

                    Log.i("1234", "date:::"+orders.getDate());

                    for (DataSnapshot postSnapshotItem : postSnapshot.getChildren()) {
                        Log.i("1234", "postsnapshot "+postSnapshotItem.toString());
                        Cart upload = postSnapshotItem.getValue(Cart.class);
                        productList.add(upload);
                        orders.setValueList(productList);
                    }

                    OorderList.add(orders);

                }

                setupOrdersRecycleView(v);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    private void setupOrdersRecycleView(View v) {
        order_rv = v.findViewById(R.id.order_rv);
        order_rv.setHasFixedSize(true);
        LinearLayoutManager recyclerViewlayoutManager = new LinearLayoutManager(getContext());
        order_rv.setLayoutManager(recyclerViewlayoutManager);
        OdersAdapter adapter = new OdersAdapter(OorderList, getContext());
        order_rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}