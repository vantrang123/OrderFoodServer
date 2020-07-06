package com.trangdv.orderfoodserver.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.Food;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {
    private static final String TAG = "FoodListAdapter";
    private LayoutInflater mInflater;
    private List<Food> foods = new ArrayList<>();
    Context context;
    LinearLayoutManager layoutManager;
    ItemListener listener;

    Locale locale;
    NumberFormat fmt;

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    public FoodListAdapter(Context context, List<Food> foods, ItemListener itemListener) {
        super();
        this.context = context;
        this.foods = foods;
        listener = itemListener;
        compositeDisposable = new CompositeDisposable();
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.item_foood, parent, false);
        locale = new Locale("vi", "VN");
        fmt = NumberFormat.getCurrencyInstance(locale);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
//        Locale locale = new Locale("vi", "VN");
//        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
//        int price = (Integer.parseInt(foods.get(position).getPrice()));
        double price = foods.get(position).getPrice();

        holder.tvNameFood.setText("Tên: " + foods.get(position).getName());
        holder.tvPriceFood.setText("Giá: " + fmt.format(price));
        holder.tvDiscountFood.setText("Phí giao hàng: " + foods.get(position).getDiscount());

        if (foods.get(position).getBitmapImage() == null) {
            Glide.with(context)
                    .asBitmap()
                    .load(foods.get(position).getImage())
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
                            foods.get(position).setBitmapImage(resource);
                            return false;
                        }
                    })
                    .into(holder.imgFood);
        } else {
            holder.imgFood.setImageBitmap(foods.get(position).getBitmapImage());
        }

        /*if (Common.currentFav != null && Common.currentFav.size() > 0) {
            if (Common.checkFavorite(foods.get(position).getId())) {
                holder.ivFavorite.setImageResource(R.drawable.ic_favorite_red);
                holder.ivFavorite.setTag(true);
            } else {
                holder.ivFavorite.setImageResource(R.drawable.ic_favorite_gray);
                holder.ivFavorite.setTag(false);
            }
        } else {
            holder.ivFavorite.setTag(false);
        }*/

    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgFood, ivSetting;
        public TextView tvNameFood;
        public TextView tvPriceFood;
        public TextView tvDiscountFood;
        public ImageView ivFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.iv_food_image);
            tvNameFood = itemView.findViewById(R.id.tv_food_name);
            tvPriceFood = itemView.findViewById(R.id.tv_food_price);
            tvDiscountFood = itemView.findViewById(R.id.tv_food_discount);
//            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            ivSetting = itemView.findViewById(R.id.iv_setting);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToFoodDetail(getLayoutPosition());
                }
            });


            ivSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(v, getAdapterPosition());
                }
            });


            /*ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView fav = (ImageView)v;
                    if ((Boolean) v.getTag()) {
                        dialogUtils.showProgress(context);
//                        removeFavorite(getAdapterPosition(), fav);
                    } else {
                        dialogUtils.showProgress(context);
//                        insertFavorite(getAdapterPosition(), fav);
                    }
                }
            });*/

        }
    }

    private void showPopup(View v, int position) {
        PopupMenu popup = new PopupMenu(context, v, Gravity.START);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popup.getMenu());
        popup.setGravity(Gravity.END);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.search_item:
                        listener.dispatchToEditingFood(position);
                        return false;
                    case R.id.delete_item:
//                        delContributedReview();
                        listener.deleteFood(position);
                        return false;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    /*private void removeFavorite(int adapterPosition, ImageView fav) {
        compositeDisposable.add(
                anNgonAPI.removeFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foods.get(adapterPosition).getId(),
                        Common.currentRestaurant.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                fav.setImageResource(R.drawable.ic_favorite_gray);
                                fav.setTag(false);
                                if (Common.currentFav != null) {
                                    Common.removeFav(foods.get(adapterPosition).getId());
                                }
                            }
                            dialogUtils.dismissProgress();
                        }, throwable -> {
                            dialogUtils.dismissProgress();
                        })
        );
    }

    private void insertFavorite(int position, ImageView fav) {
        compositeDisposable.add(
                anNgonAPI.insertFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foods.get(position).getId(),
                        Common.currentRestaurant.getId(),
                        foods.get(position).getName(),
                        foods.get(position).getImage(),
                        foods.get(position).getPrice())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                fav.setImageResource(R.drawable.ic_favorite_red);
                                fav.setTag(true);
                                if (Common.currentFav != null) {
                                    Common.currentFav.add(new FavoriteOnlyId(foods.get(position).getId()));
                                }
                            }
                            dialogUtils.dismissProgress();
                        }, throwable -> {
                            dialogUtils.dismissProgress();
                        })
        );
    }*/

    public void onStop() {
        compositeDisposable.clear();
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position);
        void dispatchToEditingFood(int position);
        void deleteFood(int position);
    }
}
