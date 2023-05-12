package edu.cuhk.cudeliver;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import edu.cuhk.cudeliver.databinding.FragmentOrderCreateBinding;
import model.Order;
import model.User;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderCreateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderCreateFragment extends Fragment {

    //Fragment view binding
    private FragmentOrderCreateBinding createBinding;
    private ProgressDialog progressDialog;

    //Connect to firebase database
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference orderRef;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Creating Order...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_order_create, container, false);
        createBinding = FragmentOrderCreateBinding.inflate(inflater, container, false);
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth= calendar.get(Calendar.MONTH)+1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        createBinding.textExpiryTime.setText(String.format("%02d : %02d", currentHour, currentMinute));
        createBinding.textExpiryDate.setText(String.format("%04d/%02d/%02d", currentYear, currentMonth,currentDay));
        createBinding.contact.setText(OrderActivity.currentUser.getPhone());

        createBinding.textExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Date date;
                int d1,d2,d3;
                try {
                    date = sdf.parse(createBinding.textExpiryDate.getText().toString());
                    d3 = date.getDate();
                    d2 = date.getMonth();
                    d1 = date.getYear()+1900;
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                DatePickerDialog dd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        createBinding.textExpiryDate.setText(String.format("%04d/%02d/%02d", i, i1+1,i2));
                    }
                },d1,d2,d3);
                dd.show();
            }
        });
        createBinding.textExpiryTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh : mm");
                Date date;
                int d1,d2;
                try {
                    date = sdf.parse(createBinding.textExpiryTime.getText().toString());
                    d2 = date.getMinutes();
                    d1 = date.getHours();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                TimePickerDialog td = new TimePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        createBinding.textExpiryTime.setText(String.format("%02d : %02d", i, i1));
                    }
                }, d1, d2, true);
                td.show();
            }
        });
        //Add onClickListener to submit new order button
        createBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(view,getContext());

                Log.i("TAG", "Clicked the submit button!!!");

                String orderId = UUID.randomUUID().toString();
                double startLat = 22.41537;
                double startLong = 114.20898;
                double destinationLat = 22.419974;
                double destinationLong = 114.207259;
                String title = createBinding.textCreateTitle.getText().toString();
                String description = createBinding.textDescription.getText().toString();
                String startName = "Chung Chi College";
                String destinationName = "Sir Run Run Shaw Hall";
                String expiryTime = createBinding.textExpiryTime.getText().toString();
                String expiryDate = createBinding.textExpiryDate.getText().toString();
                String contact = createBinding.contact.getText().toString();
                String orderCreator = mAuth.getCurrentUser().getUid();
                String orderDeliver = "";
                String status = "Pending";

                // input validation
                if(title.length() == 0) {
                    Utils.showMessage(view,"Please input title",Utils.WARNING);
                    return;
                }
                if(TextUtils.isEmpty(createBinding.price.getText().toString())){
                    Utils.showMessage(view,"Price cannot be empty",Utils.WARNING);
                    return;
                }
                double price = Double.parseDouble(createBinding.price.getText().toString());
                if (price < 0){
                    Utils.showMessage(view,"Price cannot be negative",Utils.WARNING);
                    return;
                }
                 if (contact.length() != 8){
                    Utils.showMessage(view,"Please enter valid HK phone number",Utils.WARNING);
                    return;
                }
                progressDialog.show();
                Order newOrder = new Order(title,description,startLat, startLong, destinationLat, destinationLong, startName, destinationName, expiryTime, expiryDate, price, contact, orderCreator, orderDeliver, status);
                usersRef.child(orderCreator).child("myOrders").child(orderId).setValue(newOrder).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        orderRef.child(orderId).setValue(newOrder).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i("TAG", "Successfully submit!!!");
                                createBinding.textExpiryTime.setText(String.format("%02d : %02d", currentHour, currentMinute));
                                createBinding.textExpiryDate.setText(String.format("%04d/%02d/%02d", currentYear, currentMonth,currentDay));
                                createBinding.contact.setText(OrderActivity.currentUser.getPhone());

                                progressDialog.hide();
                                Utils.showMessage(view,"Order submitted",Utils.MESSAGE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.hide();
                                Utils.showMessage(view,"Error, try again later",Utils.WARNING);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        Utils.showMessage(view,"Error, try again later",Utils.WARNING);
                    }
                });
            }
        });
        return createBinding.getRoot();
    }

    public void submitOrder() {
        Log.i("TAG", "Clicked");
    }

    public void onDestroy(){
        super.onDestroy();
        progressDialog.dismiss();
    }
}
