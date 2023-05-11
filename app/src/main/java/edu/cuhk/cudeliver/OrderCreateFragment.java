package edu.cuhk.cudeliver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import edu.cuhk.cudeliver.databinding.FragmentOrderCreateBinding;
import model.Order;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderCreateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderCreateFragment extends Fragment {

    //Fragment view binding
    private FragmentOrderCreateBinding createBinding;

    //Connect to firebase database
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference orderRef;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OrderCreateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderCreateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderCreateFragment newInstance(String param1, String param2) {
        OrderCreateFragment fragment = new OrderCreateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connect to database
        database = FirebaseDatabase.getInstance("https://cudeliver-523c3-default-rtdb.asia-southeast1.firebasedatabase.app");
        usersRef = database.getReference("Users");
        orderRef = database.getReference("Orders");
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_order_create, container, false);
        createBinding = FragmentOrderCreateBinding.inflate(inflater, container, false);
        //Add onClickListener to submit new order button
        createBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TAG", "Clicked the submit button!!!");

                String orderId = UUID.randomUUID().toString();
                double startLat = 22.41537;
                double startLong = 114.20898;
                double destinationLat = 22.419974;
                double destinationLong = 114.207259;
                String startName = "Chung Chi College";
                String destinationName = "Sir Run Run Shaw Hall";
                String startTime = createBinding.startTime.getText().toString();
                String arrTime = createBinding.arrivalTime.getText().toString();
                double price = Double.parseDouble(createBinding.price.getText().toString());
                String contact = createBinding.contact.getText().toString();
                String orderCreator = mAuth.getCurrentUser().getUid();
                String orderDeliver = "";
                String status = "Pending";
                Order newOrder = new Order(startLat, startLong, destinationLat, destinationLong, startName, destinationName, startTime, arrTime, price, contact, orderCreator, orderDeliver, status);
                usersRef.child(orderCreator).child("myOrders").child(orderId).setValue(newOrder);
                orderRef.child(orderId).setValue(newOrder);

                Log.i("TAG", "Successfully submit!!!");

                createBinding.startTime.setText("");
                createBinding.arrivalTime.setText("");
                createBinding.price.setText("");
                createBinding.contact.setText("");

            }
        });
        return createBinding.getRoot();
    }

    public void submitOrder() {
        Log.i("TAG", "Clicked");
    }
}
