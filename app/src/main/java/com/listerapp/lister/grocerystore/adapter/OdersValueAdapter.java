package com.listerapp.lister.grocerystore.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.model.Cart;
import com.listerapp.lister.grocerystore.model.Orders;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class OdersValueAdapter extends RecyclerView.Adapter<OdersValueAdapter.MyViewHolder> {

    List<Cart> ordersValueList;
    Context context;


    public OdersValueAdapter(List<Cart> ordersValueList, Context context) {
        this.ordersValueList = ordersValueList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cart, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        Log.i("orders", "values: "+ordersValueList.get(position).getTitle());
        holder.quantity_minus.setVisibility(View.GONE);
        holder.quantity_plus.setVisibility(View.GONE);
        holder.product_attribute.setVisibility(View.GONE);
        holder.cart_delete.setVisibility(View.GONE);
        holder.quantity.setText(ordersValueList.get(position).getQuantity());
        holder.product_price.setText(ordersValueList.get(position).getPrice());
        holder.sub_total.setText(ordersValueList.get(position).getSubTotal());
        Picasso.get()
                .load(ordersValueList.get(position).getImage())
                .into(holder.product_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressbar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("Error : ", e.getMessage());
                    }
                });

        holder.product_title.setText(ordersValueList.get(position).getTitle());



    }

    @Override
    public int getItemCount() {

        return ordersValueList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        Button quantity_minus, quantity_plus, cart_delete;
        TextView quantity, product_price, sub_total, product_title, product_attribute;
        ImageView product_image;
        ProgressBar progressbar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            quantity_minus = itemView.findViewById(R.id.quantity_minus);
            quantity = itemView.findViewById(R.id.quantity);
            product_price = itemView.findViewById(R.id.product_price);
            sub_total = itemView.findViewById(R.id.sub_total);
            quantity_plus = itemView.findViewById(R.id.quantity_plus);
            product_image = itemView.findViewById(R.id.product_image);
            progressbar = itemView.findViewById(R.id.progressbar);
            product_title = itemView.findViewById(R.id.product_title);
            product_attribute = itemView.findViewById(R.id.product_attribute);
            cart_delete = itemView.findViewById(R.id.cart_delete);


        }
    }
}
