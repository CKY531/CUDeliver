package edu.cuhk.cudeliver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import edu.cuhk.cudeliver.databinding.FragmentOrderDetailBinding;
import edu.cuhk.cudeliver.databinding.FragmentOrderDetailOrderBinding;
import model.Order;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderOrderDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderOrderDetailFragment extends Fragment {

    //View binding
    private FragmentOrderDetailOrderBinding fDetailBinding;

    //Connect to firebase database
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference orderRef;

    //Google Mao
    private GoogleMap mMap;
    private ArrayList<LatLng> points;

    //Route latlng
    private ArrayList<LatLng> routePoints;

    //Order
    static private Order order;

    //Require permission
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    Toast.makeText(getContext(), "Please allow location permission! Otherwise, the app cannot work.", Toast.LENGTH_SHORT).show();
                }
            });

    public OrderOrderDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OrderDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrderOrderDetailFragment newInstance(Order thisOrder) {
        OrderOrderDetailFragment fragment = new OrderOrderDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        order = thisOrder;
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

        ((OrderActivity) getActivity()).disableSwipe();

        //Start and Destination Points
        points = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fDetailBinding = FragmentOrderDetailOrderBinding.inflate(inflater, container, false);
        Calendar calendar = Calendar.getInstance();

        //Disable all edittext
        fDetailBinding.textDetailStartLocation.setFocusable(false);
        fDetailBinding.textDetailDestination.setFocusable(false);
        fDetailBinding.textDetailTitle.setFocusable(false);
        fDetailBinding.textDetailExpiryDate.setFocusable(false);
        fDetailBinding.textDetailExpiryTime.setFocusable(false);
        fDetailBinding.detailPrice.setFocusable(false);
        fDetailBinding.detailContact.setFocusable(false);
        fDetailBinding.textDetailDescription.setFocusable(false);

        //Set the detail
        fDetailBinding.textDetailStartLocation.setText(order.getStartName());
        fDetailBinding.textDetailDestination.setText(order.getDestinationName());
        fDetailBinding.textDetailTitle.setText(order.getTitle());
        fDetailBinding.textDetailExpiryDate.setText(order.getExpiryDate());
        fDetailBinding.textDetailExpiryTime.setText(order.getExpiryTime());
        fDetailBinding.detailPrice.setText("$"+order.getPrice());
        fDetailBinding.detailContact.setText(order.getContact());
        fDetailBinding.textDetailDescription.setText(order.getDescription());

        //Set Map
        SupportMapFragment sMapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.detail_map);
        // Async map
        sMapFrag.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                // When map is loaded
                mMap = googleMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);

                //Ask for Location
                if (ContextCompat.checkSelfPermission(
                        getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }

                //Zoom to the CUHK
                mMap.setMyLocationEnabled(true);
                LatLng home = new LatLng(22.419871, 114.206169);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 15));

                //Create marker for start and destination
                LatLng start = new LatLng(order.getStartLat(), order.getStartLong());
                MarkerOptions markerOptions1 = new MarkerOptions();
                markerOptions1.position((start));
                markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                LatLng destination = new LatLng(order.getDestinationLat(), order.getDestinationLong());
                MarkerOptions markerOptions2 = new MarkerOptions();
                markerOptions2.position((destination));
                markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                mMap.addMarker(markerOptions1);
                mMap.addMarker(markerOptions2);

                //Combine Lat and Long array
                routePoints = new ArrayList<LatLng>();
                List<Double> orderLat = order.getRouteLat();
                List<Double> orderLong = order.getRouteLong();
                for (int i = 0;i < orderLat.size();i++) {
                    LatLng temp = new LatLng(orderLat.get(i), orderLong.get(i));
                    routePoints.add(temp);
                }

                //Draw route
                if (routePoints.size() > 0) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.addAll(routePoints);
                    polylineOptions.width(15);
                    polylineOptions.color(Color.BLUE);
                    polylineOptions.geodesic(true);
                    mMap.addPolyline(polylineOptions);
                }

            }
        });

        fDetailBinding.completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.child(mAuth.getCurrentUser().getUid()).child("myOrders").child(order.getId()).child("status").setValue("Completed");
                if (order.getStatus().equals("pending")){
                    orderRef.child(order.getId()).removeValue();
                }else {
                    usersRef.child(order.getOrderCreator()).child("myJobs").child(order.getId()).child("status").setValue("Completed");
                }
                Utils.showMessage(view,"Order Completed",Utils.MESSAGE);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fTran = fm.beginTransaction();
                fTran.replace(R.id.orderFragLayout, new YourOrderFragment(),"yourOrder");
                fTran.commit();

//                orderRef.child(order.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        Log.i("TAG", "Order Id:" + order.getId());
//                        Log.i("TAG", "User Id:" + mAuth.getCurrentUser().getUid());
//                        if(dataSnapshot.exists()){
//                            orderRef.child(order.getId()).removeValue();
//                            Utils.showMessage(view,"Order will be removed",Utils.MESSAGE);
//                            FragmentManager fm = getActivity().getSupportFragmentManager();
//                            fm.popBackStack();
//                        }else {
//                            //Change deliver's status
//                            usersRef.child(order.getOrderCreator()).child("myJobs").child(order.getId()).child("status").setValue("completed").addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void unused) {
//                                    Utils.showMessage(view,"Order Completed",Utils.MESSAGE);
//                                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                                    fm.popBackStack();
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });


            }
        });

        return fDetailBinding.getRoot();

    }

    public void end() {
                getActivity().onBackPressed();
    }
}