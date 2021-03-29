package com.listerapp.lister.grocerystore.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.adapter.MessageAdapter;
import com.listerapp.lister.grocerystore.model.Message;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    String userId = "";
    private Intent intent;
    TextView tvfriendsName;
    ImageView imgfriendsimage;
    private String currentUserId;
    ImageButton imgbtsendMessage;
    EditText etMessageContent;

    RecyclerView recyclerViewMessage;

    List<Message> mMessage;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    DatabaseReference messageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        tvfriendsName = findViewById(R.id.tvfriendsName);
        imgfriendsimage = findViewById(R.id.imgfriendsimage);
        imgbtsendMessage = findViewById(R.id.imgbtsendMessage);
        etMessageContent = findViewById(R.id.etMessageContent);
        intent = getIntent();
        getIntentData();

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();


        recyclerViewMessage = findViewById(R.id.recyclerViewMessage);
        recyclerViewMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewMessage.setLayoutManager(linearLayoutManager);
        readMessage(currentUserId,userId);

        imgbtsendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textfieldString = etMessageContent.getText().toString();

                if(!textfieldString.equals("")) {
                    sendMessage(currentUserId , userId , textfieldString);
                    etMessageContent.setText("");
                }else{
                    Toast.makeText(MessageActivity.this,"Enter your Message",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getIntentData() {
        if(intent.getStringExtra("userId") != null) {
            userId =  intent.getStringExtra("userId");
            Log.i("1234", "propertyID 1 "+userId);
        }
        if(intent.getStringExtra("userName") != null) {
            String userName =  intent.getStringExtra("userName");
            tvfriendsName.setText(userName);
        }
        if(intent.getStringExtra("userImage") != null) {
            String userImage =  intent.getStringExtra("userImage");

            Picasso.get()
                    .load(userImage)
                    .into(imgfriendsimage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d("Error : ", e.getMessage());
                        }
                    });
        }
    }

    public void sendMessage(String sender, String receiver, final String message){
        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference().child("chat").child(currentUserId).child(userId);
        DatabaseReference chatReference1 = FirebaseDatabase.getInstance().getReference().child("chat").child(userId).child(currentUserId);
        chatReference.child("friends_id").setValue(userId);
        chatReference.child("message").setValue(message);

        chatReference1.child("friends_id").setValue(currentUserId);
        chatReference1.child("message").setValue(message);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("reciever", receiver);
        hashMap.put("message", message);

        reference.child("message").push().setValue(hashMap);
        etMessageContent.setText("");
    }

    private void readMessage(final String myid, final String userid){
        mMessage = new ArrayList<>();

        messageReference = FirebaseDatabase.getInstance().getReference().child("message");
        messageReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMessage.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);
                    if(message.getReciever().equals(myid) && message.getSender().equals(userid) ||
                            message.getReciever().equals(userid) && message.getSender().equals(myid)  ){
                        mMessage.add(message);
//                        Log.i("intent","message is "+message.getReciever());
                    }
                    MessageAdapter messageAdapter = new MessageAdapter(MessageActivity.this, mMessage);
                    recyclerViewMessage.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}