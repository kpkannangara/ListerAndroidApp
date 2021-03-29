package com.listerapp.lister.grocerystore.activity;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.helper.Converter;
import com.listerapp.lister.grocerystore.model.Cart;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductViewActivity extends BaseActivity {
    private static int cart_count = 0;
    public TextView quantity, inc, dec;
    String _id, _title, _image, _description, _price, _currency, _discount, _attribute, _category, _amount;
    TextView id, title, price, discount, disc1;
    ImageView imageView;
    ProgressBar progressBar;
    LinearLayout addToCart, share;
    RelativeLayout quantityLL;
    List<Cart> cartList = new ArrayList<>();
    int cartId;
    Cart cart;
    RelativeLayout imgSelect;
    boolean isSelect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);

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



        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        changeActionBarTitle(getSupportActionBar());
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
        //upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        cart_count = cartCount();

        title = findViewById(R.id.apv_title);
        price = findViewById(R.id.apv_price);
        discount = findViewById(R.id.apv_discount);
        imageView = findViewById(R.id.apv_image);
        progressBar = findViewById(R.id.progressbar);
        addToCart = findViewById(R.id.add_to_cart_ll);
        share = findViewById(R.id.apv_share);
        quantityLL = findViewById(R.id.quantity_rl);
        quantity = findViewById(R.id.quantity);
        inc = findViewById(R.id.quantity_plus);
        dec = findViewById(R.id.quantity_minus);

        imgSelect = findViewById(R.id.imgSelect);
        disc1 = findViewById(R.id.disc1);

        Log.i("1234", "discription "+_description);
        cartList = getCartList();
        title.setText(_title);
        price.setText(_price);
        disc1.setText(_description);

        if(intent.hasExtra("isSelect")){
            String isselect = intent.getStringExtra("isSelect");
            if(isselect.equals("true")){
                isSelect = true;
                imgSelect.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.ic_star_48));
            }
        }

        if (_image != null) {
            Picasso.get().load(_image).error(R.drawable.no_image).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        if (!cartList.isEmpty()) {
            for (int i = 0; i < cartList.size(); i++) {
                if (cartList.get(i).getTitle().equalsIgnoreCase(_title)) {
                    addToCart.setVisibility(View.GONE);
                    quantityLL.setVisibility(View.VISIBLE);
                    quantity.setText(cartList.get(i).getQuantity());
                    cartId = i;

                }
            }
        }

        imgSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSelect){
                    productSetFavouriteSelect();
                }else{
                    productSetFavouriteDeselect();
                }

            }
        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEntry = _image + "\n" + _title + "\n" + _description + "\n" + _attribute + "-" + _currency + _price + "(" + _discount + ")";

                Intent textShareIntent = new Intent(Intent.ACTION_SEND);
                textShareIntent.putExtra(Intent.EXTRA_TEXT, userEntry);
                textShareIntent.setType("text/plain");
                startActivity(textShareIntent);
            }
        });


        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _price = price.getText().toString();

                cart = new Cart(_id, _title, _image, _price, "1", _price, _category, _amount);
                cartList.add(cart);
                String cartStr = gson.toJson(cartList);
                //Log.d("CART", cartStr);
                localStorage.setCart(cartStr);
                onAddProduct();
                addToCart.setVisibility(View.GONE);
                quantityLL.setVisibility(View.VISIBLE);
            }
        });


        inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _price = price.getText().toString();

                // int total_item = Integer.parseInt(cartList.get(cartId).getQuantity());
                int total_item = Integer.parseInt(quantity.getText().toString());
                total_item++;
                Log.d("totalItem", total_item + "");
                quantity.setText(total_item + "");
                String subTotal = String.valueOf(Double.parseDouble(_price) * total_item);
                cartList.get(cartId).setQuantity(quantity.getText().toString());
                cartList.get(cartId).setSubTotal(subTotal);
                cartList.get(cartId).setPrice(_price);
                String cartStr = gson.toJson(cartList);
                //Log.d("CART", cartStr);
                localStorage.setCart(cartStr);
            }
        });

        dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _price = price.getText().toString();

                //int total_item = Integer.parseInt(quantity.getText().toString());
                int total_item = Integer.parseInt(quantity.getText().toString());
                if (total_item != 1) {
                    total_item--;
                    quantity.setText(total_item + "");
                    Log.d("totalItem", total_item + "");
                    String subTotal = String.valueOf(Double.parseDouble(_price) * total_item);


                    cartList.get(cartId).setQuantity(quantity.getText().toString());
                    cartList.get(cartId).setSubTotal(subTotal);
                    cartList.get(cartId).setPrice(_price);
                    String cartStr = gson.toJson(cartList);
                    //Log.d("CART", cartStr);
                    localStorage.setCart(cartStr);
                }
            }
        });
    }

    private void productSetFavouriteDeselect() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabaseFavourite = FirebaseDatabase.getInstance().getReference("favourite").child(id).child(_title);
        mDatabaseFavourite.removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            imgSelect.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.ic_star_border_48));
                            isSelect = false;
                        }else{
                            imgSelect.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.ic_star_48));
                            isSelect = true;
                        }
                    }
                });
    }

    private void productSetFavouriteSelect() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabaseFavourite = FirebaseDatabase.getInstance().getReference("favourite").child(id).child(_title);
        mDatabaseFavourite.child("name").setValue(_title);
        mDatabaseFavourite.child("price").setValue(_price);
        mDatabaseFavourite.child("description").setValue(_description);
        mDatabaseFavourite.child("category").setValue(_category);
        mDatabaseFavourite.child("image").setValue(_image);
        mDatabaseFavourite.child("amount").setValue(_amount).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    imgSelect.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.ic_star_48));
                    isSelect = true;
                }else{
                    imgSelect.setBackground(ContextCompat.getDrawable(getApplication(), R.drawable.ic_star_border_48));
                    isSelect = false;
                }
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
        tv.setText(_title); // ActionBar title text
        tv.setTextSize(20);

        // Set the text color of TextView to red
        // This line change the ActionBar title text color
        tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        // Set the ActionBar display option
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        // Finally, set the newly created TextView as ActionBar custom view
        actionBar.setCustomView(tv);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here
                onBackPressed();
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
        menuItem.setIcon(Converter.convertLayoutToImage(ProductViewActivity.this, cart_count, R.drawable.ic_shopping_basket));
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
