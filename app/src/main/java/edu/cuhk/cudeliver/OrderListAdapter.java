package edu.cuhk.cudeliver;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import androidx.annotation.NonNull;
//import android.support.v4.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import model.Order;

public class OrderListAdapter extends Adapter<OrderListAdapter.OrderViewHolder>  {
    private Context context;
    private LayoutInflater mInflater;

    private LinkedList<Order> mOrderInfoList;

    class OrderViewHolder extends RecyclerView.ViewHolder {

//        ImageView flowerImageItemView;
        TextView mPrice;
        TextView mTitle;
        TextView mExpiry;

        TextView mDest;

        TextView mSrc;

        TextView mStatus;

        final OrderListAdapter mAdapter;

        public OrderViewHolder(View itemView, OrderListAdapter adapter) {
            super(itemView);
//            flowerImageItemView = itemView.findViewById(R.id.image);
            mTitle = itemView.findViewById(R.id.list_item_title);
            mPrice = itemView.findViewById(R.id.list_item_price);
            mExpiry = itemView.findViewById(R.id.list_item_expiry);
            mStatus = itemView.findViewById(R.id.list_item_status);
            mDest = itemView.findViewById(R.id.list_item_dest);
            mSrc = itemView.findViewById(R.id.list_item_src);

            this.mAdapter = adapter;
//
//            // Event handling registration, page navigation goes here
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                     Get the position of the item that was clicked.
                    int position = getLayoutPosition();
                    Toast.makeText(v.getContext(), "Position " + position + " is clicked", Toast.LENGTH_SHORT).show();
                    Fragment fragment = OrderDetailFragment.newInstance(mOrderInfoList.get(position));

                    AppCompatActivity act = (AppCompatActivity) v.getContext();
                    FragmentTransaction transaction = act.getSupportFragmentManager().beginTransaction();

                    transaction.replace(R.id.orderFragLayout, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();

                }

            });

            // End of ViewHolder initialization
        }
    }

    public OrderListAdapter(Context context,
                            LinkedList<Order> mOrderInfoList) {
        mInflater = LayoutInflater.from(context);
        this.mOrderInfoList = mOrderInfoList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mItemView = mInflater.inflate(R.layout.order_list_item, parent, false);
        return new OrderViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        String status = (mOrderInfoList.get(position)).getStatus();
        String expiry = (mOrderInfoList.get(position)).getExpiryTime();
        String expiryDate = (mOrderInfoList.get(position)).getExpiryDate();
        double price = (mOrderInfoList.get(position)).getPrice();
        String title = (mOrderInfoList.get(position)).getTitle();
        String src = (mOrderInfoList.get(position)).getStartName();
        String dest = (mOrderInfoList.get(position)).getDestinationName();

//        Uri uri = Uri.parse(mImagePath);
//        System.out.println("path:" + mImagePath);
        // Update the following to display correct information based on the given position


        // Set up View items for this row (position), modify to show correct information read from the CSV
        holder.mStatus.setText(status);
        holder.mPrice.setText("$"+String.valueOf(price));
        holder.mTitle.setText(title);
        holder.mExpiry.setText(expiry);
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/ddhh : mm");
        try {
            Date date = sdf.parse(expiryDate+expiry);
            if (date.getTime() - currentDate.getTime() >= 82800000) holder.mExpiry.setText("> 1 day");
        } catch (ParseException e) {
            Log.e("date",e.getMessage());
        }
        holder.mSrc.setText(src);
        holder.mDest.setText(dest);


//        holder.flowerImageItemView.setImageURI(uri);

    }

    public void updateInfo(LinkedList<Order>mOrderInfoList)
    {
        this.mOrderInfoList = mOrderInfoList;
        notifyDataSetChanged();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mOrderInfoList.size();
    }

}
