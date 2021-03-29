package com.listerapp.lister.grocerystore.activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bugfender.sdk.Bugfender;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.listerapp.lister.grocerystore.R;
import com.listerapp.lister.grocerystore.adapter.SearchAdapter;
import com.listerapp.lister.grocerystore.fragment.AddCategoryFragment;
import com.listerapp.lister.grocerystore.fragment.AddProductFragment;
import com.listerapp.lister.grocerystore.fragment.CategoryFragment;
import com.listerapp.lister.grocerystore.fragment.FavouriteFragment;
import com.listerapp.lister.grocerystore.fragment.OrderFragment;
import com.listerapp.lister.grocerystore.fragment.ProfileFragment;
import com.listerapp.lister.grocerystore.fragment.AddUserFragment;
import com.listerapp.lister.grocerystore.fragment.ResetPwFragment;
import com.listerapp.lister.grocerystore.fragment.UsersFragment;
import com.listerapp.lister.grocerystore.helper.Converter;
import com.listerapp.lister.grocerystore.model.Product;
import com.listerapp.lister.grocerystore.model.ProductResult;
import com.listerapp.lister.grocerystore.model.Upload;
import com.listerapp.lister.grocerystore.model.User;
import com.listerapp.lister.grocerystore.util.localstorage.LocalStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.listerapp.lister.grocerystore.App.CHANNEL_1_ID;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static int cart_count = 0;
    User user;
    List<Product> productList = new ArrayList<>();
    SearchAdapter mAdapter;
    private RecyclerView recyclerView;
    private  Intent intent;
    private FirebaseAuth mAuth;
    String currentUserEmail;
    boolean doubleBackToExitPressedOnce = false;
    DatabaseReference mDatabaseProfileRef;
    @SuppressLint("ResourceAsColor")
    static void centerToolbarTitle(@NonNull final Toolbar toolbar) {
        final CharSequence title = toolbar.getTitle();
        final ArrayList<View> outViews = new ArrayList<>(1);
        toolbar.findViewsWithText(outViews, title, View.FIND_VIEWS_WITH_TEXT);
        if (!outViews.isEmpty()) {
            final TextView titleView = (TextView) outViews.get(0);
            titleView.setGravity(Gravity.CENTER);
            titleView.setTextColor(Color.parseColor("#FAD23C"));
            final Toolbar.LayoutParams layoutParams = (Toolbar.LayoutParams) titleView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            toolbar.requestLayout();
            //also you can use titleView for changing font: titleView.setTypeface(Typeface);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
//            super.onBackPressed();

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);


        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.cart_action);
        menuItem.setIcon(Converter.convertLayoutToImage(MainActivity.this, cart_count, R.drawable.ic_shopping_basket));
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        searchView.setQueryHint("Search Here...");
        EditText searchBox = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchBox.setTextColor(Color.BLACK);
        searchBox.setHintTextColor(Color.GRAY);
        searchView.setIconifiedByDefault(true);
        ImageView searchClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.ic_close_black_24dp);

        if (searchView != null) {
            final SearchView finalSearchView = searchView;
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Toast like print
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    // UserFeedback.show( "SearchOnQueryTextChanged: " + s);
                    if (s.length() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        productList = new ArrayList<>();
                    } else {

                        getSearchProduct(s);
                    }

                    return true;
                }
            });
        }

        return true;
    }


    private void getSearchProduct(final String query2){

        Log.i("1234", "string value 1: "+query2);

        productList.clear();


        ///
        mDatabaseCategoryRef = FirebaseDatabase.getInstance().getReference("category");
        mDatabaseCategoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    getProductString(query2, upload.getName());
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void getProductString(String query2, String name) {
        DatabaseReference mDatabaseRef =FirebaseDatabase.getInstance().getReference("category").child(name).child("Items");
        Query query=mDatabaseRef.orderByChild("name").startAt(query2)
                .endAt(query2 + "\uf8ff");
//        Query query=mDatabaseRef.orderByChild("name").startAt("[a-zA-Z0-9]*")
//                .endAt(query2);
        Log.i("1234", "string value 2: "+query.toString());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    try{
                        Log.i("1234", "string value 4: "+postSnapshot.toString());
                        Product upload = postSnapshot.getValue(Product.class);
                        productList.add(upload);
                        Log.i("1234", "product name: "+upload.getName());
                    }catch (Exception e){}
                }
                setUpRecyclerView();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void setUpRecyclerView() {
        if (productList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }

        mAdapter = new SearchAdapter(productList, MainActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cart_action:
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Toolbar toolbar;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        centerToolbarTitle(toolbar);
        cart_count = cartCount();

        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.search_recycler_view);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        localStorage = new LocalStorage(getApplicationContext());
        String userString = localStorage.getUserLogin();
        Gson gson = new Gson();
        userString = localStorage.getUserLogin();
        user = gson.fromJson(userString, User.class);

        displaySelectedScreen(R.id.nav_home);
        getAllUsersForCheckAdmin();


    }



    private List<User> mUploads = new ArrayList<>();;
    private void getAllUsersForCheckAdmin() {
        DatabaseReference mDatabaseUserRef = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    User user = postSnapshot.getValue(User.class);
                    mUploads.add(user);
                }
                checkisAdmin(mUploads);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void checkisAdmin(List<User> mUploads) {
        String currentUser = mAuth.getCurrentUser().getUid();
        for(int i=0; i<mUploads.size(); i++){
            Log.i("1234", "upload size "+currentUser +" ?????? "+mUploads.get(i).getId());
            if(currentUser.equals(mUploads.get(i).getId())){
//                Log.i("1234", "equals user");
                if("Admin".equals(mUploads.get(i).getRole())){
//                    Log.i("1234", "admin true- "+mUploads.get(i).getRole());
                    localStorage.setCurrentUserISAdmin("true");
                }else{
//                    Log.i("1234", "admin false- "+mUploads.get(i).getRole());
                    localStorage.setCurrentUserISAdmin("false");
                }
            }
        }
        otherUIRun();
    }

    private void otherUIRun() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);
        Menu nav_Menu = navigationView.getMenu();

        if(localStorage.getCurrentUserISAdmin().equals("true")){
            nav_Menu.findItem(R.id.nav_favourite).setVisible(false);
            try{
                checkCategoryList();

            }catch (Exception e){

            }
        }else{
            nav_Menu.findItem(R.id.nav_create_users).setVisible(false);
            nav_Menu.findItem(R.id.nav_add_product_categoty).setVisible(false);
            nav_Menu.findItem(R.id.nav_add_products).setVisible(false);
            nav_Menu.findItem(R.id.nav_my_order).setVisible(false);
        }



        LinearLayout nav_headr_all = hView.findViewById(R.id.nav_headr_all);
        nav_headr_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("mainActivity", "clicked nav Header");
                Fragment fragment = new ProfileFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
                ft.replace(R.id.content_frame, fragment);
                ft.commit();

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        final TextView nav_user = hView.findViewById(R.id.nav_header_name);
        final ImageView nav_image = hView.findViewById(R.id.imageView);
        LinearLayout nav_footer = findViewById(R.id.footer_text);


        mDatabaseProfileRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid());
        mDatabaseProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                Picasso.get()
                        .load(currentUser.getImage())
                        .into(nav_image, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }
                            @Override
                            public void onError(Exception e) {
                            }
                        });

                nav_user.setText(currentUser.getFname());


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
        nav_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new
                FirebaseAuth.getInstance().signOut();

                //old
//                localStorage.logoutUser();
                startActivity(new Intent(getApplicationContext(), LoginRegisterActivity.class));
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                // Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_LONG).show();
            }
        });

    }

    private DatabaseReference mDatabaseCategoryRef;
    private void checkCategoryList(){

        mUploads = new ArrayList<>();
        mDatabaseCategoryRef = FirebaseDatabase.getInstance().getReference("category");
        mDatabaseCategoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    if(upload.getName() != null){
                        checkProductList(upload.getName());
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private DatabaseReference mDatabaseProductRef;
    private void checkProductList(String name) {

        mDatabaseProductRef = FirebaseDatabase.getInstance().getReference("category");
        mDatabaseProductRef.child(name).child("Items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Product upload = postSnapshot.getValue(Product.class);
                    Log.i("1234", "product name "+upload.getName()+ " quantity "+upload.getAmount());


                    try{
                        if(Float.parseFloat(upload.getAmount()) <=10){
                            notificationForLowCapacity(upload.getName());
                        }
                    }catch (Exception e){
                        Bugfender.i("Exceptions", "mainActivity: "+e);
                    }

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private NotificationManagerCompat notificationManager;
    private void notificationForLowCapacity(String name) {
        Log.i("1234", "Notification manager clicked");
        notificationManager = NotificationManagerCompat.from(this);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Product quantity low")
                .setContentText(name+ "quantity is low please inform sellers")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);

    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_home:
                fragment = new CategoryFragment();
                break;

            case R.id.nav_reset:
                fragment = new ResetPwFragment();
                break;

            case R.id.nav_profile:
                fragment = new ProfileFragment();
                break;
            case R.id.nav_create_users:
                fragment = new AddUserFragment();
                break;

            case R.id.nav_add_product_categoty:
                fragment = new AddCategoryFragment();
                break;

            case R.id.nav_add_products:
                fragment = new AddProductFragment();
                break;
            case R.id.nav_users_details:
                fragment = new UsersFragment();
                break;

            case R.id.nav_my_order:
                fragment = new OrderFragment();
                break;
            case R.id.nav_favourite:
                fragment = new FavouriteFragment();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
            ft.replace(R.id.content_frame, fragment);
            try{
                ft.commit();
            }catch (Exception e){
                Log.i("1234", "error shows "+e);
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        //make this method blank
        return true;
    }

    @Override
    public void onAddProduct() {
        super.onAddProduct();
        cart_count++;
        invalidateOptionsMenu();

    }

    @Override
    public void onRemoveProduct() {
        super.onRemoveProduct();
    }
}
