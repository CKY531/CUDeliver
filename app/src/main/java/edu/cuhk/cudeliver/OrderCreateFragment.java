package edu.cuhk.cudeliver;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener;
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import edu.cuhk.cudeliver.databinding.FragmentOrderCreateBinding;
import googleMapDirection.Parser;
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

    //Google Mao
    private GoogleMap mMap;
    private ArrayList<LatLng> points;

    //Route latlng
    private ArrayList<LatLng> routePoints;

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
        ((OrderActivity) getActivity()).disableSwipe();
        
        //Start and Destination Points
        points = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_order_create, container, false);
        createBinding = FragmentOrderCreateBinding.inflate(inflater, container, false);
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE)+5;
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth= calendar.get(Calendar.MONTH)+1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        createBinding.textCreateStartLocation.setText("");
        createBinding.textCreateDestination.setText("");

        //Disable the start location and destination EditText
        createBinding.textCreateStartLocation.setFocusable(false);
        createBinding.textCreateDestination.setFocusable(false);

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
                SimpleDateFormat sdf = new SimpleDateFormat("HH : mm");
                Date date;
                int d1,d2;
                try {
                    date = sdf.parse(createBinding.textExpiryTime.getText().toString());
                    d2 = (date.getMinutes()+5>59)?date.getMinutes()-55:date.getMinutes()+5;
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

        //Initialize map fragment
        SupportMapFragment sMapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

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

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //Reset marker when already 2, which includes a starting point and destination already
                        if (points.size() == 2) {
                            points.clear();
                            mMap.clear();
                        }
                        //Save first point
                        points.add(latLng);

                        //Create marker
                        MarkerOptions markerOptions1 = new MarkerOptions();
                        markerOptions1.position((latLng));

                        if (points.size() == 1) {
                            //Only one point, which is starting point, mark it on map
                            markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            String startLocation = getAddress(points.get(0).latitude, points.get(0).longitude);
                            createBinding.textCreateStartLocation.setText(startLocation);
                            createBinding.textCreateDestination.setText("");
                        } else {
                            //Two points, mark the second point which is destination
                            markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        }
                        mMap.addMarker(markerOptions1);

                        if (points.size() == 2) {
                            String url = generateUrl(points.get(0), points.get(1));
                            //Draw route
                            ReqDirections reqDir = new ReqDirections();
                            reqDir.execute(url);
                        }
                    }
                });
            }
        });

        //Add onClickListener to submit new order button
        createBinding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.hideKeyboard(view,getContext());

                Log.i("TAG", "Clicked the submit button!!!");

                //Validation of starting and destination need to do first, otherwise program crashes
                String startName = createBinding.textCreateStartLocation.getText().toString();
                String destinationName = createBinding.textCreateDestination.getText().toString();
                if (TextUtils.isEmpty(startName)) {
                    Utils.showMessage(view,"Please choose starting location in the map",Utils.WARNING);
                    return;
                }
                if (TextUtils.isEmpty(destinationName)) {
                    Utils.showMessage(view,"Please choose Destination in the map",Utils.WARNING);
                    return;
                }

                List<Double> latArr = arrListToLatArr(routePoints);
                List<Double> lngArr = arrListToLngArr(routePoints);

                String orderId = UUID.randomUUID().toString();
                double startLat = points.get(0).latitude;
                double startLong = points.get(0).longitude;
                double destinationLat = points.get(1).latitude;
                double destinationLong = points.get(1).longitude;
                String title = createBinding.textCreateTitle.getText().toString();
                String description = createBinding.textDescription.getText().toString();
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
                Order newOrder = new Order(title, description, startLat, startLong, destinationLat, destinationLong, startName, destinationName, expiryTime, expiryDate, price, contact, orderCreator, orderDeliver, status, latArr, lngArr, orderId);
                usersRef.child(orderCreator).child("myOrders").child(orderId).setValue(newOrder).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        orderRef.child(orderId).setValue(newOrder).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i("TAG", "Successfully submit!!!");
                                createBinding.textCreateStartLocation.setText("");
                                createBinding.textCreateDestination.setText("");
                                createBinding.textExpiryTime.setText(String.format("%02d : %02d", currentHour, currentMinute));
                                createBinding.textExpiryDate.setText(String.format("%04d/%02d/%02d", currentYear, currentMonth,currentDay));
                                createBinding.contact.setText(OrderActivity.currentUser.getPhone());
                                createBinding.textCreateTitle.setText("");
                                createBinding.textDescription.setText("");
                                mMap.clear();

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

    //Given ArrayList<LatLng>, return Lat Double[]
    public List<Double> arrListToLatArr(ArrayList<LatLng> temp) {
        List<Double> latArr = new ArrayList<Double>();
        for (int i = 0;i < temp.size();i++) {
            latArr.add(temp.get(i).latitude);
        }
        return latArr;
    }

    //Given ArrayList<LatLng>, return Long Double[]
    public List<Double> arrListToLngArr(ArrayList<LatLng> temp) {
        List<Double> lngArr = new ArrayList<Double>();
        for (int i = 0;i < temp.size();i++) {
            lngArr.add(temp.get(i).longitude);
        }
        return lngArr;
    }

    //Get Address by latitude and longtitude
    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String add = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            add = obj.getAddressLine(0);
            Log.v("TAG", "Address" + add);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return add;
    }

    //Generate a Google map Direction API URL to be called
    private String generateUrl(LatLng start, LatLng destination) {
        //Origin
        String ori = "origin=" + start.latitude + "," + start.longitude;
        //Destination
        String dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=walking";
        //API Key
        String apiKey = "key=" + getResources().getString(R.string.google_maps_key);
        //URL Parameter
        String urlParam = ori + "&" + dest + "&" + sensor + "&" + mode + "&" + apiKey;
        //Full URL
        String url = "https://maps.googleapis.com/maps/api/directions/json" + "?" + urlParam + "";
        Log.i("TAG", "The URL:" + url);

        return url;
    }

    //Call Google Map Direction API
    public String requestDir(String reqUrl) {
        String res = "";
        InputStream inStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get response result
            inStream = httpURLConnection.getInputStream();
            InputStreamReader inStreamReader = new InputStreamReader(inStream);
            BufferedReader bufReader = new BufferedReader(inStreamReader);

            StringBuffer strBuf = new StringBuffer();
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                strBuf.append(line);
            }

            res = strBuf.toString();
            bufReader.close();
            inStreamReader.close();
            if (inStream != null) {
                inStream.close();
            }
        } catch (Exception e) {
            Log.e("TAG", "Error in calling direction API!!!");
            e.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }

        Log.i("TAG", "Success http?");
        return res;

    }

    public class ReqDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String res = "";
            try {
                res = requestDir(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "Error in Async Task");
            }
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse the response from Google Map API, get route, start and ending name
            RouteParser taskParser = new RouteParser();
            taskParser.execute(s);
//            StartLocParser startLocParser = new StartLocParser();
//            startLocParser.execute(s);
            DestinationParser destinationParser = new DestinationParser();
            destinationParser.execute(s);
        }
    }

    //Get Route
    public class RouteParser extends AsyncTask<String, Void, ArrayList<LatLng>> {
        @Override
        protected ArrayList<LatLng> doInBackground(String... strings) {
            JSONObject jObj = null;
            ArrayList<LatLng> routesPts = null;
            try {
                jObj = new JSONObject(strings[0]);
                Parser parser = new Parser();
                routesPts = parser.parse(jObj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routesPts;
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> latLngs) {
            //Draw the route
//            Log.i("TAG", "Arrive Draw !!!!!!");
//            Log.i("TAG", "Length of LATLNG:"+latLngs.size());
            routePoints = latLngs;
            PolylineOptions polylineOptions = new PolylineOptions();

            polylineOptions.addAll(latLngs);
            polylineOptions.width(15);
            polylineOptions.color(Color.BLUE);
            polylineOptions.geodesic(true);
            mMap.addPolyline(polylineOptions);
        }
    }

    //Get Destination
    public class DestinationParser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            JSONObject jObj = null;
            String destination = "";
            try {
                jObj = new JSONObject(strings[0]);
                Parser parser = new Parser();
                destination = parser.getEndLoc(jObj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return destination;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("TAG", "Destination:" + s);
            createBinding.textCreateDestination.setText(s);
        }
    }

    public void onDestroy(){
        super.onDestroy();
        progressDialog.dismiss();
    }

}
