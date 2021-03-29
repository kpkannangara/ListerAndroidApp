package com.listerapp.lister.grocerystore.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.activity.ProductActivity;
import com.listerapp.lister.grocerystore.activity.ProductViewActivity;
import com.listerapp.lister.grocerystore.model.Product;
import com.listerapp.lister.grocerystore.model.Upload;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.MyViewHolder> {

    List<Product> categoryList;
    Context context;
    String Tag;

    public FavouriteAdapter(List<Product> categoryList, Context context, String tag) {
        this.categoryList = categoryList;
        this.context = context;
        Tag = tag;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_category, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        final Product product = categoryList.get(position);

        LocalStorage localStorage = new LocalStorage(context);;
        if(localStorage.getCurrentUserISAdmin().equals("true")){
            holder.btn_delete.setVisibility(View.VISIBLE);
        }else{
            holder.btn_delete.setVisibility(View.GONE);
        }

        holder.title.setText(product.getName());
        if (Tag.equalsIgnoreCase("Category")) {
//
            Picasso.get()
                    .load(product.getImage())
                    .into(holder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d("Error : ", e.getMessage());
                        }
                    });
        }

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCategory(product.getName(), position);
            }
        });


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductViewActivity.class);
                intent.putExtra("title", product.getName());
                intent.putExtra("isSelect", "true");
                intent.putExtra("image", product.getImage());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("currency", product.getCurrency());
                intent.putExtra("attribute", product.getAttribute());
                intent.putExtra("discount", product.getDiscount());
                intent.putExtra("description", product.getDescription());
                intent.putExtra("category", product.getCategory());
                intent.putExtra("amount", product.getAmount());


                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(String name, final int position) {
        DatabaseReference mDatabaseCategoryRef = FirebaseDatabase.getInstance().getReference("category").child(name);
        mDatabaseCategoryRef.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showError2("Removed category successfully", true);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        if (Tag.equalsIgnoreCase("Home") && categoryList.size() < 6 && categoryList.size() > 3) {
            return 3;
        } else if (Tag.equalsIgnoreCase("Home") && categoryList.size() >= 6) {
            return 6;
        } else {
            return categoryList.size();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, btn_delete;
        ProgressBar progressBar;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.category_image);
            title = itemView.findViewById(R.id.category_title);
            progressBar = itemView.findViewById(R.id.progressbar);
            cardView = itemView.findViewById(R.id.card_view);
            btn_delete = itemView.findViewById(R.id.btn_delete);
        }
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
}
