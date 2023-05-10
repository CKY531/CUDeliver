package edu.cuhk.cudeliver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import edu.cuhk.cudeliver.databinding.ActivityOrderBinding;

public class OrderActivity extends AppCompatActivity {

    ActivityOrderBinding orderBinding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderBinding = ActivityOrderBinding.inflate(getLayoutInflater());
        setContentView(orderBinding.getRoot());
        auth = FirebaseAuth.getInstance();
        replaceFrag(new OrderDisplayFragment());

        //Add Listener to listen to bottom nav bar
        orderBinding.bottomNavigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.create:
                    replaceFrag(new OrderCreateFragment());
                    break;
                case R.id.orderList:
                    replaceFrag(new OrderDisplayFragment());
                    break;
                case R.id.yourOrder:
                    replaceFrag(new YourOrderFragment());
                    break;
                case R.id.orderToDeliver:
                    replaceFrag(new OrderToDeliverFragment());
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


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.option_change_password:
                Log.d("USER", String.valueOf(auth.getCurrentUser()));
//                changePassword();
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

    //Replace old order fragment with the new one according to the selected item in bottom nav
    public void replaceFrag(Fragment frag) {
        FragmentManager fMan = getSupportFragmentManager();
        FragmentTransaction fTran = fMan.beginTransaction();
        fTran.replace(R.id.orderFragLayout, frag);
        fTran.commit();
    }

}
