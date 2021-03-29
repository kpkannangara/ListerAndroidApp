package com.listerapp.lister.grocerystore.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.activity.MessageActivity;
import com.listerapp.lister.grocerystore.activity.ProductActivity;
import com.listerapp.lister.grocerystore.activity.ProductViewActivity;
import com.listerapp.lister.grocerystore.activity.ProductViewAdminActivity;
import com.listerapp.lister.grocerystore.model.Upload;
import com.listerapp.lister.grocerystore.model.User;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    List<User> userList;
    Context context;
    String Tag;

    public UsersAdapter(List<User> categoryList, Context context) {
        this.userList = categoryList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_users_details, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        LocalStorage localStorage = new LocalStorage(context);;
        if(localStorage.getCurrentUserISAdmin().equals("true")){
            holder.btn_delete.setVisibility(View.VISIBLE);
        }else{
            holder.btn_delete.setVisibility(View.GONE);
        }
//        final Category category = categoryList.get(position);
        holder.user_name.setText(userList.get(position).getFname());
        holder.user_role.setText(userList.get(position).getRole());
        holder.user_email.setText(userList.get(position).getEmail());
        holder.btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall(userList.get(position).getMobile());
            }
        });
        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser(userList.get(position).getId());
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatScreen = new Intent(context, MessageActivity.class);
                chatScreen.putExtra("userId",userList.get(position).getId());
                chatScreen.putExtra("userName",userList.get(position).getFname());
                chatScreen.putExtra("userImage",userList.get(position).getImage());
                context.startActivity(chatScreen);
            }
        });

        Log.i("user", "mobile number "+userList.get(position).getPhonenumber());
        Picasso.get()
                .load(userList.get(position).getImage())
                .into(holder.user_image, new Callback() {
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

    private void deleteUser(String id) {
        DatabaseReference mDatabaseUserRef = FirebaseDatabase.getInstance().getReference("users").child(id);
        mDatabaseUserRef.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("1234", "User account deleted.");
                        }
                    }
                });
    }

    private void makeCall(String mobile) {

//        String phone = "352454";
        String d = "tel:" + mobile ;
        Log.i("Make call", "");
//        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse(d));
        try {
            context.startActivity(phoneIntent);
            Log.i("Finished making a call", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Call faild, please try again later.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.i("call", "make call exception "+e);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView user_image;
        TextView user_name, user_email, user_role, btn_call, btn_delete;
        ProgressBar progressBar;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.user_image);
            user_name = itemView.findViewById(R.id.user_name);
            user_email = itemView.findViewById(R.id.user_email);
            user_role = itemView.findViewById(R.id.user_role);
            btn_call = itemView.findViewById(R.id.btn_call);
            btn_delete = itemView.findViewById(R.id.btn_delete);


            progressBar = itemView.findViewById(R.id.progressbar);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }
}
