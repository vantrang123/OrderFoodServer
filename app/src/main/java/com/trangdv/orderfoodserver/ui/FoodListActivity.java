package com.trangdv.orderfoodserver.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.FoodListAdapter;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.Food;
import com.trangdv.orderfoodserver.model.FoodModel;
import com.trangdv.orderfoodserver.model.eventbus.FoodListEvent;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FoodListActivity extends AppCompatActivity implements FoodListAdapter.ItemListener {
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;
    RecyclerView rvListFood;
    FoodListAdapter foodListAdapter;
    LinearLayoutManager layoutManager;
    RelativeLayout rootLayout;

    FloatingActionButton fab;

    //Firebase
    FirebaseDatabase db;
    FirebaseStorage storage;
    StorageReference storageReference;

    int menuId;

    //Add new food
    private EditText editName, editDescription, editPrice, editDiscount;
    private TextView tvCancel, tvPost, tvTitle;
    private ImageView ivSelect, ivBack;
    private String isSize;
    private SwipeRefreshLayout swrFood;

    Food newFood;
    List<Food> foods = new ArrayList<>();
    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        findViewById();

        //Firebase
        db = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        init();

    }

    private void findViewById() {
        ivBack = findViewById(R.id.iv_back);
        rvListFood = findViewById(R.id.recycler_food);
        fab = findViewById(R.id.fab_foodList);
        rootLayout = findViewById(R.id.root_Layout);
        tvTitle = findViewById(R.id.tvTitle);
        swrFood = findViewById(R.id.swr_food);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFoodDialog();
            }
        });

        swrFood.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_orange_dark);

        swrFood.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData(menuId);
            }
        });
    }

    private void init() {
        compositeDisposable = new CompositeDisposable();
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvListFood.setLayoutManager(layoutManager);
        foodListAdapter = new FoodListAdapter(FoodListActivity.this, foods, this);
        rvListFood.setAdapter(foodListAdapter);
    }

    //showUpdateFoodDialog() method
    private void showUpdateFoodDialog(Food food, int i) {

        //just copy code from showAddFoodDialog() method
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Chỉnh sửa thông tin món");

        LayoutInflater inflater = this.getLayoutInflater();
        View edit_menu_layout = inflater.inflate(R.layout.dialog_add_food, null);

        editName = edit_menu_layout.findViewById(R.id.edt_name);
        editDescription = edit_menu_layout.findViewById(R.id.edt_address);
        editPrice = edit_menu_layout.findViewById(R.id.edt_price);
        editDiscount = edit_menu_layout.findViewById(R.id.edt_discount);
        ivSelect = edit_menu_layout.findViewById(R.id.iv_select_image);
        tvPost = edit_menu_layout.findViewById(R.id.tv_post);
        tvCancel = edit_menu_layout.findViewById(R.id.tv_cancel);

        //Set default value for View
        editName.setText(food.getName());
        editDescription.setText(food.getDescription());
        editPrice.setText(food.getPrice().toString());
        editDiscount.setText(String.valueOf(food.getDiscount()));

        Glide.with(this)
                .asBitmap()
                .load(food.getImage())
                .centerCrop()
                .fitCenter()
                .placeholder(R.drawable.image_default)
                .into(ivSelect);
        alertDialog.setView(edit_menu_layout);
        AlertDialog dialog = alertDialog.show();

        //Event for button
        ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(food, dialog, i);
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //showAddFoodDialog() method
    private void showAddFoodDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle(getResources().getString(R.string.txt_title_add_new_food));
        alertDialog.setMessage(getResources().getString(R.string.txt_content_add_new_menu));

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.dialog_add_food, null);

        editName = add_menu_layout.findViewById(R.id.edt_name);
        editDescription = add_menu_layout.findViewById(R.id.edt_address);
        editPrice = add_menu_layout.findViewById(R.id.edt_price);
        editDiscount = add_menu_layout.findViewById(R.id.edt_discount);

        ivSelect = add_menu_layout.findViewById(R.id.iv_select_image);
        tvPost = add_menu_layout.findViewById(R.id.tv_post);
        tvCancel = add_menu_layout.findViewById(R.id.tv_cancel);

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_add_cart);
        AlertDialog dialog = alertDialog.show();

        //Event for button
        ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Copy from HomeActivity
            }
        });

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(dialog); //Copy from HomeActivity
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //chooseImage() method
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PIC_IMAGE_REQUEST);
    }

    //uploadImage() method
    private void uploadImage(AlertDialog dialog) {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            //create random string
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    createFood(uri.toString(), dialog);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded: " + progress + "%");
                        }
                    });
        } else {
            createFood("", dialog);
        }
    }

    private void createFood(String uri, AlertDialog dialog) {
        compositeDisposable.add(anNgonAPI.createFood(Common.API_KEY,
                editName.getText().toString(),
                editDescription.getText().toString(),
                uri,
                Float.parseFloat(editPrice.getText().toString()),
                isSize,
                "false",
                Integer.parseInt(editDiscount.getText().toString())
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foodModel -> {
                            if (foodModel.isSuccess()) {
                                createFoodSize(foodModel);
                                createMenuFood(foodModel, dialog);
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void createFoodSize(FoodModel foodModel) {
        int foodId = foodModel.getResult().get(0).getId();
    }

    private void createFoodSize(int foodId, int sizeId) {
        compositeDisposable.add(anNgonAPI.createFoodSize(Common.API_KEY,
                foodId,
                sizeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void createMenuFood(FoodModel foodModel, AlertDialog dialog) {
        compositeDisposable.add(anNgonAPI.createMenuFood(Common.API_KEY,
                menuId,
                foodModel.getResult().get(0).getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                                dialog.dismiss();
                                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText(getResources().getString(R.string.txt_title_add_new_menu_success))
                                        .setContentText(getResources().getString(R.string.txt_content_add_new_menu_success))
                                        .show();
                                fetchData(menuId);
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    /*//loadListFood() method
    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.foodName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.foodImage);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Code late
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        rvListFood.setAdapter(adapter);
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PIC_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            saveUri = data.getData();
            Glide.with(this)
                    .asBitmap()
                    .load(saveUri)
                    .centerCrop()
                    .fitCenter()
                    .placeholder(R.drawable.image_default)
                    .into(ivSelect);
            tvPost.setEnabled(true);
            tvPost.setBackground(getResources().getDrawable(R.drawable.bg_button));
        }
    }


    //Method for delete and update food item
    //Press Ctrl+o
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
            //update food
//            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            //delete food
//            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    //deleteFood() method
    private void deleteFood(String key) {

    }


    //changeImage() method
    private void changeImage(Food food, AlertDialog dialog, int position) {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            //create random string
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
//                            Toast.makeText(FoodListActivity.this, "Upload Successfully!!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    food.setImage(uri.toString());
                                    foodListAdapter.notifyItemChanged(position);
                                    updateFood(uri, dialog, String.valueOf(food.getId()));
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodListActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded: " + progress + "%");
                        }
                    });
        }
    }

    private void updateFood(Uri uri, AlertDialog dialog, String foodId) {
        compositeDisposable.add(anNgonAPI.updateFood(Common.API_KEY,
                foodId,
                editName.getText().toString(),
                editDescription.getText().toString(),
                uri.toString(),
                Float.valueOf(editPrice.getText().toString()),
                "",
                "",
                Integer.valueOf(editDiscount.getText().toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                                dialog.dismiss();
                                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Chỉnh sửa thành công")
                                        .setContentText("Bạn hãy thêm món ăn mới để dể thu hút nhiều người mua hơn")
                                        .show();
                            } else {
                                Toast.makeText(this, "[UPDATE FOOD RESULT]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
//                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void fetchData(int menuId) {
        foods.clear();
        compositeDisposable.add(anNgonAPI.getFoodOfMenu(Common.API_KEY, menuId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foodModel -> {
                            if (foodModel.isSuccess()) {
                                foods.addAll(foodModel.getResult());
                                foodListAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            swrFood.setRefreshing(false);

                        },
                        throwable -> {
                            swrFood.setRefreshing(false);
                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    // listen EventBus
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void loadFoodListByCategory(FoodListEvent event) {
        if (event.isSuccess()) {
            tvTitle.setText(event.getCategory().getName());
            fetchData(event.getCategory().getId());
            menuId = event.getCategory().getId();
        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void dispatchToFoodDetail(int position) {

    }

    @Override
    public void dispatchToEditingFood(int position) {
        showUpdateFoodDialog(foods.get(position), position);
    }

    @Override
    public void deleteFood(int position) {
        compositeDisposable.add(anNgonAPI.deleteFoodMenu(Common.API_KEY,
                foods.get(position).getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuFoodModel -> {
                            if (menuFoodModel.isSuccess()) {
                                compositeDisposable.add(anNgonAPI.deleteFood(Common.API_KEY,
                                        foods.get(position).getId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(foodModel -> {
                                                    if (foodModel.isSuccess()) {
                                                        foods.remove(position);
                                                        foodListAdapter.notifyItemRemoved(position);
                                                        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                                                .setTitleText("Xóa món")
                                                                .setContentText("Đã xóa menu thành công!")
                                                                .show();
                                                    } else {
                                                        Toast.makeText(this, "[DELETE FOOD]" + foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                },
                                                throwable -> {
                                                    dialogUtils.dismissProgress();
                                                    Toast.makeText(this, "[DELETE FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                        ));
                            } else {
                                dialogUtils.dismissProgress();
                                Toast.makeText(this, "[DELETE FOOD]" + menuFoodModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            dialogUtils.dismissProgress();
                            Toast.makeText(this, "[DELETE FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Common.animateFinish(this);
    }
}
