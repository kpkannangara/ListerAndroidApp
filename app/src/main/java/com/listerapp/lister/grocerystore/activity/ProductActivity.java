package com.listerapp.lister.grocerystore.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.adapter.ProductAdapter;

import com.listerapp.lister.grocerystore.helper.Converter;
import com.listerapp.lister.grocerystore.helper.Data;
import com.listerapp.lister.grocerystore.model.Category;
import com.listerapp.lister.grocerystore.model.Product;
import com.listerapp.lister.grocerystore.model.ProductResult;
import com.listerapp.lister.grocerystore.model.Token;
import com.listerapp.lister.grocerystore.model.Upload;
import com.listerapp.lister.grocerystore.model.User;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductActivity extends BaseActivity {
    private static int cart_count = 0;
    Data data;

    String Tag = "List";
    View progress;
//    LocalStorage localStora/ge;
//    Gson gson = new Gson();
//    User user;
//    Token token;
    String categoryName;
    Category category;
    List<Product> productList = new ArrayList<>();
    ProductAdapter mAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference mDatabaseProductRef;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        changeActionBarTitle(getSupportActionBar());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
        //upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        progress = findViewById(R.id.progress_bar);

//        localStorage = new LocalStorage(getApplicationContext());
//        user = gson.fromJson(localStorage.getUserLogin(), User.class);
//        token = new Token(user.getToken());
        Intent intent = getIntent();
        categoryName = intent.getStringExtra("category");
//        category = new Category(categoryName, user.getToken());

        cart_count = cartCount();
        recyclerView = findViewById(R.id.product_rv);



    //    getCategoryProduct();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseProductRef = FirebaseDatabase.getInstance().getReference("category");
        mDatabaseProductRef.child(categoryName).child("Items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    try{
                        Product upload = postSnapshot.getValue(Product.class);
                        productList.add(upload);
                        Log.i("1234", "product list count: "+productList.size());
                    }catch (Exception e){}
                }
                setUpRecyclerView();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void changeActionBarTitle(ActionBar actionBar) {
        // Create a LayoutParams for TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, // Width of TextView
                RelativeLayout.LayoutParams.WRAP_CONTENT); // Height of TextView
        TextView tv = new TextView(getApplicationContext());
        // Apply the layout parameters to TextView widget
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        // Set text to display in TextView
        tv.setText("Products"); // ActionBar title text
        tv.setTextSize(20);

        // Set the text color of TextView to red
        // This line change the ActionBar title text color
        tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        // Set the ActionBar display option
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Finally, set the newly created TextView as ActionBar custom view
        actionBar.setCustomView(tv);
    }

    private void setUpRecyclerView() {

        mAdapter = new ProductAdapter(productList, ProductActivity.this, Tag);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }



    public void onToggleClicked(View view) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here

                Intent intent = new Intent(ProductActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            case R.id.cart_action:
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.cart_action);
        MenuItem menuItem2 = menu.findItem(R.id.action_search).setVisible(false);
        menuItem.setIcon(Converter.convertLayoutToImage(ProductActivity.this, cart_count, R.drawable.ic_shopping_basket));
        return true;
    }


    @Override
    public void onAddProduct() {
        cart_count++;
        invalidateOptionsMenu();

    }

    @Override
    public void onRemoveProduct() {
        cart_count--;
        invalidateOptionsMenu();

    }

}
