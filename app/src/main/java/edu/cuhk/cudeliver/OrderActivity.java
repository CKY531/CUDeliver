package edu.cuhk.cudeliver;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import edu.cuhk.cudeliver.databinding.ActivityOrderBinding;
import model.Order;
import model.User;

public class OrderActivity extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener  {

    ActivityOrderBinding orderBinding;
    FirebaseAuth auth;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference orderRef;

    static User currentUser;



    ProgressDialog progressDialog;

    SwipeRefreshLayout mSwipeRefreshLayout;

    View contentView;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderBinding = ActivityOrderBinding.inflate(getLayoutInflater());
        setContentView(orderBinding.getRoot());
        contentView = findViewById(android.R.id.content);
        auth = FirebaseAuth.getInstance();
        replaceFrag(new OrderDisplayFragment(),"orderList");
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Updating...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // setup swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_order_activity);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        database = FirebaseDatabase.getInstance("https://cudeliver-523c3-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("Users");
        orderRef = database.getReference("Orders");

        usersRef.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Ask for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 2. Otherwise, request location permissions from the user.
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 101);
        }

        //Ask for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 2. Otherwise, request location permissions from the user.
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                    }, 101);
        }


        //Add Listener to listen to bottom nav bar
        orderBinding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.create:
                    replaceFrag(new OrderCreateFragment(),"create");
                    break;
                case R.id.orderList:
                    replaceFrag(new OrderDisplayFragment(),"orderList");
                    break;
                case R.id.yourOrder:
                    replaceFrag(new YourOrderFragment(),"yourOrder");
                    break;
                case R.id.orderToDeliver:
                    replaceFrag(new OrderToDeliverFragment(),"orderToDeliver");
                    break;
            }

            return true;
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_option_menu, menu);
        return true;
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        //Check if permissions are granted

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.option_change_password:
                Log.d("USER", String.valueOf(auth.getCurrentUser()));
                showChangePasswordDialog();
                return true;

            case R.id.option_logout:
                auth.signOut();
                Log.d("USER", String.valueOf(auth.getCurrentUser()));
                Intent intent = new Intent(OrderActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finishAffinity();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showChangePasswordDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this).create();

        LayoutInflater inflater = LayoutInflater.from(this);
        View changePasswordLayout = inflater.inflate(R.layout.dialog_change_password,null);
        dialog.setView(changePasswordLayout);
        EditText oldPassword = changePasswordLayout.findViewById(R.id.old_password);
        EditText newPassword1 = changePasswordLayout.findViewById(R.id.new_password1);
        EditText newPassword2 = changePasswordLayout.findViewById(R.id.new_password2);
        Button update = changePasswordLayout.findViewById(R.id.button_change_password);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(view,OrderActivity.this);
                progressDialog.show();
                String op =  oldPassword.getText().toString().trim();
                String np1 = newPassword1.getText().toString().trim();
                String np2 = newPassword2.getText().toString().trim();
                FirebaseUser user = auth.getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(),op);

                user.reauthenticate(credential).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        if (e.getMessage().equals("The password is invalid or the user does not have a password.")) {
                            Utils.showMessage(view,"Password incorrect",Utils.WARNING);
                        }else
                        {
                            Log.d("AUTH",e.getMessage());
                            Utils.showMessage(view,"Unknown error.",Utils.WARNING);
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.hide();
                        if (np1.length() < 8){
                            Utils.showMessage((View) view,"Password too short",Utils.WARNING);
                        }
                        else if (!Utils.passwordConfirm(np1,np2)){
                            Utils.showMessage((View) view,"Password not match",Utils.WARNING);
                        }
                        else {
                            auth.getCurrentUser().updatePassword(np1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.hide();
                                    Utils.showMessage((View) contentView,"Success",Utils.MESSAGE);
                                    dialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.hide();
                                    if (e.getMessage().equals("The password is invalid or the user does not have a password.")) {
                                        Utils.showMessage(view,"Old Password incorrect",Utils.WARNING);
                                    }else
                                    {
                                        Log.d("AUTH",e.getMessage());
                                        Utils.showMessage(view,"Unknown error.",Utils.WARNING);
                                    }
                                }
                            });
                        }
                    }

                });
                    }
                });
        dialog.show();
    }

    //Replace old order fragment with the new one according to the selected item in bottom nav
    public void replaceFrag(Fragment frag, String tag) {
        FragmentManager fMan = getSupportFragmentManager();
        FragmentTransaction fTran = fMan.beginTransaction();
        fTran.replace(R.id.orderFragLayout, frag,tag);
        fTran.commit();
    }

    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = OrderActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        Utils.updateStatus(usersRef,orderRef,auth);

            String tag = getVisibleFragment().getTag();
         if (tag.equals("create")){
             replaceFrag(new OrderCreateFragment(),"create");
             return;
         }
        if (tag.equals("orderList")){
            replaceFrag(new OrderDisplayFragment(),"orderList");
            return;
        }
        if (tag.equals("yourOrder")){
            replaceFrag(new YourOrderFragment(),"yourOrder");
            return;
        }
        if (tag.equals("orderToDeliver")){
            replaceFrag(new OrderToDeliverFragment(),"orderToDeliver");
            return;
        }
    }
    public void disableSwipe(){
        mSwipeRefreshLayout.setEnabled(false);
    }

    public void enableSwipe(){
        mSwipeRefreshLayout.setEnabled(true);
    }
}
