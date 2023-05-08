package edu.cuhk.cudeliver;

import androidx.annotation.ContentView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import edu.cuhk.cudeliver.databinding.ActivityOrderBinding;

public class OrderActivity extends AppCompatActivity {

    ActivityOrderBinding orderBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderBinding = ActivityOrderBinding.inflate(getLayoutInflater());
        setContentView(orderBinding.getRoot());
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
        inflater.inflate(R.menu.bottom_nav_menu, menu);
        return true;
    }

    //Replace old order fragment with the new one according to the selected item in bottom nav
    public void replaceFrag(Fragment frag) {
        FragmentManager fMan = getSupportFragmentManager();
        FragmentTransaction fTran = fMan.beginTransaction();
        fTran.replace(R.id.orderFragLayout, frag);
        fTran.commit();
    }

}
