package edu.cuhk.cudeliver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

public class OrderListAdapter extends Adapter<OrderListAdapter.OrderViewHolder>  {
    private Context context;
    private LayoutInflater mInflater;

    private LinkedList<String[]> mOrderInfoList;

    class OrderViewHolder extends RecyclerView.ViewHolder {

//        ImageView flowerImageItemView;
        TextView mOrderType;
        TextView mOrder;
        TextView mTime;

        final OrderListAdapter mAdapter;

        public OrderViewHolder(View itemView, OrderListAdapter adapter) {
            super(itemView);
//            flowerImageItemView = itemView.findViewById(R.id.image);
            mOrder = itemView.findViewById(R.id.text_order);
            mOrderType = itemView.findViewById(R.id.text_order_type);
            mTime = itemView.findViewById(R.id.text_finish_time);
            this.mAdapter = adapter;
//
//            // Event handling registration, page navigation goes here
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                     Get the position of the item that was clicked.
                    int position = getLayoutPosition();
                    Toast.makeText(v.getContext(), "Position " + position + " is clicked", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(v.getContext().getApplicationContext(), DetailActivity.class);
//                    intent.putExtra("position",position);
//                    ((Activity) v.getContext()).startActivityForResult(intent,1);
                }

            });

            // End of ViewHolder initialization
        }
    }

    public OrderListAdapter(Context context,
                            LinkedList<String[]> mOrderInfoList) {
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
        String type = (mOrderInfoList.get(position))[0];
        String time = (mOrderInfoList.get(position))[1];
        String order = (mOrderInfoList.get(position))[2];
//        Uri uri = Uri.parse(mImagePath);
//        System.out.println("path:" + mImagePath);
        // Update the following to display correct information based on the given position


        // Set up View items for this row (position), modify to show correct information read from the CSV
        holder.mOrderType.setText(type);
        holder.mOrder.setText(order);
        holder.mTime.setText(time);
//        holder.flowerImageItemView.setImageURI(uri);

    }

    public void updateInfo(LinkedList<String[]>mOrderInfoList)
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