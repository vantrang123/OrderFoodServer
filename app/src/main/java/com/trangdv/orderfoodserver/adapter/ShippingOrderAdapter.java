package com.trangdv.orderfoodserver.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.model.Food;
import com.trangdv.orderfoodserver.model.Shipper;
import com.trangdv.orderfoodserver.model.User;

import java.util.ArrayList;
import java.util.List;

public class ShippingOrderAdapter extends RecyclerView.Adapter<ShippingOrderAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<Shipper> shipperList = new ArrayList<>();
    Context context;
    ItemListener listener;

    public ShippingOrderAdapter(List<Shipper> shipperList, Context context, ItemListener itemListener) {
        this.shipperList = shipperList;
        this.context = context;
        this.listener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.item_shipper, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvShipperName.setText(new StringBuilder("Tên NVGH: ").append(String.valueOf(shipperList.get(position).getName())));
        holder.tvShipperPhone.setText(new StringBuilder("Số điện thoại: ").append(String.valueOf(shipperList.get(position).getPhone())));
        holder.tvShipperAddress.setText(new StringBuilder("Địa chỉ: ").append(String.valueOf(shipperList.get(position).getAddress())));
    }

    @Override
    public int getItemCount() {
        return shipperList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvShipperName, tvShipperAddress, tvShipperPhone;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShipperName = itemView.findViewById(R.id.tv_shipper_name);
            tvShipperPhone = itemView.findViewById(R.id.tv_shipper_phone);
            tvShipperAddress = itemView.findViewById(R.id.tv_shipper_address);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.choiseShipper(shipperList.get(getAdapterPosition()).getId());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        itemView.setBackgroundColor(context.getColor(R.color.grey_40));
                    }
                }
            });
        }

    }

    public interface ItemListener {

        void choiseShipper(String fbid);
    }
}
