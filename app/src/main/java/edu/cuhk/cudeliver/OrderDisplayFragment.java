package edu.cuhk.cudeliver;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import edu.cuhk.cudeliver.databinding.ActivityOrderBinding;
import model.Order;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderDisplayFragment extends Fragment {

    //View Binding of Order Activity to pass
    ActivityOrderBinding orderbinding;

    private RecyclerView mRecyclerView;
    private OrderListAdapter mAdapter;

    static LinkedList<Order> mOrderInfoList = new LinkedList<>();


    private FirebaseDatabase db;
    FirebaseAuth auth;
    DatabaseReference users;
    DatabaseReference orders;
    public OrderDisplayFragment() {
        // Required empty public constructor
    }

    public OrderDisplayFragment(ActivityOrderBinding incomingBinding) {
        orderbinding = incomingBinding;
    }

    public static OrderDisplayFragment newInstance(String param1, String param2) {
        OrderDisplayFragment fragment = new OrderDisplayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseDatabase.getInstance("https://cudeliver-523c3-default-rtdb.asia-southeast1.firebasedatabase.app/");
        users = db.getReference("Users");
        orders = db.getReference("Orders");
        auth = FirebaseAuth.getInstance();
        ((OrderActivity) getActivity()).enableSwipe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_display, container, false);
        // setup swipe refresh
//        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_order_display);
//        mSwipeRefreshLayout.setOnRefreshListener(this);
//        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary,
//                android.R.color.holo_green_dark,
//                android.R.color.holo_orange_dark,
//                android.R.color.holo_blue_dark);
        // setup recyclerview
        mRecyclerView = view.findViewById(R.id.order_list);
        mAdapter = new OrderListAdapter(getContext(), mOrderInfoList,Utils.DISPLAY, orderbinding);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mOrderInfoList.size() > 3){
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == mOrderInfoList.size() - 1) {
                        Utils.showMessage((View) getActivity().findViewById(android.R.id.content), "Reached bottom", Utils.NEUTRAL);
                    }
                }
            }
        });

        // update local list by fetching database
        orders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Utils.updateStatus(users,orders,auth);
                mOrderInfoList.clear();
                for(DataSnapshot postSnapshot: snapshot.getChildren()){
                    String id = postSnapshot.getKey();
                    Order order = postSnapshot.getValue(Order.class);
                    mOrderInfoList.add(order);
                }
                    Log.d("DB","LOAD");
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

//    @Override
//    public void onRefresh() {
//
//        // TODO: replace following by fetching data from database
//        new android.os.Handler(Looper.getMainLooper()).postDelayed(
//                new Runnable() {
//                    public void run() {
//                        Toast.makeText(getContext(),"Finish",Toast.LENGTH_SHORT).show();
//                        mSwipeRefreshLayout.setRefreshing(false);
//                    }
//                }, 3000);
//    }
}
