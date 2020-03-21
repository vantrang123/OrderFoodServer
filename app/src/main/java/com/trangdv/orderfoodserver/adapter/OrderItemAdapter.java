package com.trangdv.orderfoodserver.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.Addon;
import com.trangdv.orderfoodserver.model.Food;
import com.trangdv.orderfoodserver.model.OrderDetail;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<OrderDetail> orderDetailList = new ArrayList<>();
    Context context;
    LinearLayoutManager layoutManager;
    ItemListener listener;

    Locale locale;
    NumberFormat fmt;

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    public OrderItemAdapter(Context context, List<OrderDetail> orderDetails, ItemListener itemListener) {
        super();
        this.context = context;
        this.orderDetailList = orderDetails;
        listener = itemListener;
        compositeDisposable = new CompositeDisposable();
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.item_order_detail, parent, false);
        locale = new Locale("vi", "VN");
        fmt = NumberFormat.getCurrencyInstance(locale);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.tvNameFood.setText(new StringBuffer("Name: ").append(orderDetailList.get(position).getName()));

        holder.tvSize.setText(new StringBuffer("Size: ").append(orderDetailList.get(position).getSize()));
        holder.tvQuantity.setText(new StringBuffer("Quantity: ").append(orderDetailList.get(position).getQuantity()));

        if (orderDetailList.get(position).getBitmapImage() == null) {
            Glide.with(context)
                    .asBitmap()
                    .load(orderDetailList.get(position).getImage())
                    .centerCrop()
                    .fitCenter()
                    .placeholder(R.drawable.image_default)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            orderDetailList.get(position).setBitmapImage(resource);
                            return false;
                        }
                    })
                    .into(holder.imgFood);
        } else {
            holder.imgFood.setImageBitmap(orderDetailList.get(position).getBitmapImage());
        }

        if (orderDetailList.get(position).getAddOn().toLowerCase().equals("[]") ||
                orderDetailList.get(position).getAddOn().toLowerCase().equals("nomal")) {
            holder.lnAddon.setVisibility(View.GONE);
        } else {
            holder.lnAddon.setVisibility(View.VISIBLE);
            List<Addon> addons = new Gson().fromJson(orderDetailList.get(position).getAddOn(),
                    new TypeToken<List<Addon>>(){}.getType());
            StringBuilder addonText = new StringBuilder();
            for (Addon addon : addons)
                addonText.append(addon.getName()).append("\n");
            holder.tvAddon.setText(addonText);
        }

    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgFood;
        public TextView tvNameFood, tvSize, tvAddon, tvQuantity;
        public View lnAddon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.iv_food_image);
            tvNameFood = itemView.findViewById(R.id.tv_food_name);
            tvSize = itemView.findViewById(R.id.tv_food_size);
            lnAddon = itemView.findViewById(R.id.ln_addon);
            tvAddon = itemView.findViewById(R.id.tv_add_on);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToFoodDetail(getLayoutPosition());
                }
            });
        }
    }

    public void onStop() {
        compositeDisposable.clear();
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position);
    }
}