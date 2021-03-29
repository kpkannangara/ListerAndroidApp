package com.listerapp.lister.grocerystore.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.activity.CartActivity;
import com.listerapp.lister.grocerystore.model.Cart;
import com.listerapp.lister.grocerystore.model.Orders;
import com.listerapp.lister.grocerystore.model.User;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OdersAdapter extends RecyclerView.Adapter<OdersAdapter.MyViewHolder> {

    List<Orders> ordersList;
    Context context;


    public OdersAdapter(List<Orders> ordersList, Context context) {
        this.ordersList = ordersList;
        this.context = context;
        getUserName();
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView;

        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_orders, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        Log.i("orders", "values, date: "+ordersList.get(position).getDate());

        holder.orderDate.setText(ordersList.get(position).getDate());
        holder.recyclerOder.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        OdersValueAdapter postSubAdapter = new OdersValueAdapter(ordersList.get(position).getValueList(), context);
        holder.recyclerOder.setAdapter(postSubAdapter);

        holder.orderClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteOrder(ordersList.get(position).getDate(), position);
            }
        });

        postSubAdapter.notifyDataSetChanged();

    }

    private void deleteOrder(String date, final int position) {
        Log.i("12345", "valuees:"+date+": "+userName);
        DatabaseReference mDatabaseCategoryRef = FirebaseDatabase.getInstance().getReference("order").child(date);
        mDatabaseCategoryRef.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showError2("Removed order successfully", true);
                        }
                    }
                });
    }

    private void showError2(String msg, final boolean b) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
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


    String userName = "";
    private void getUserName() {
        DatabaseReference mDatabaseProfileRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                userName = currentUser.getFname();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public int getItemCount() {

        return ordersList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView orderDate, orderClose;
        RecyclerView recyclerOder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            orderDate = itemView.findViewById(R.id.orderDate);
            recyclerOder = itemView.findViewById(R.id.recyclerOder);
            orderClose = itemView.findViewById(R.id.orderClose);

        }
    }
}
