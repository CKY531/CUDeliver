package edu.cuhk.cudeliver;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;

import model.Order;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrderDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class YourOrderFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private OrderListAdapter mAdapter;
    FirebaseAuth auth;
    private FirebaseDatabase db;
    DatabaseReference users;
    DatabaseReference orders;

    static LinkedList<Order> mOrderInfoList = new LinkedList<>();


    public YourOrderFragment() {
        // Required empty public constructor
    }

    public static OrderDisplayFragment newInstance(String param1, String param2) {
        OrderDisplayFragment fragment = new OrderDisplayFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://cudeliver-523c3-default-rtdb.asia-southeast1.firebasedatabase.app/");
        users = db.getReference("Users");
        orders = users.child(auth.getCurrentUser().getUid()).child("myOrders");
        // dummy data
//        String[] a = {"Dummy0","Dummy1","Dummy2"};
//        mOrderInfoList.add(a);
//        mOrderInfoList.add(a);
//        mOrderInfoList.add(a);
//        mOrderInfoList.add(a);
//        mOrderInfoList.add(a);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_display, container, false);
        // setup swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_order_display);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        // setup recyclerview
        mRecyclerView = view.findViewById(R.id.order_list);
        mAdapter = new OrderListAdapter(getContext(), mOrderInfoList);
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

        orders.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mOrderInfoList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    mOrderInfoList.add(postSnapshot.getValue(Order.class));
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    @Override
    public void onRefresh() {

        // TODO: replace following by fetching data from database
        new android.os.Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(),"Finish",Toast.LENGTH_SHORT).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
    }
}
