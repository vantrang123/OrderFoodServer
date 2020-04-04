package com.trangdv.orderfoodserver.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
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
import com.trangdv.orderfoodserver.viewholder.FoodViewHolder;

import net.igenius.customcheckbox.CustomCheckBox;

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
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    int menuId;

    //Add new food
    private EditText editName, editDescription, editPrice, editDiscount;
    private CustomCheckBox ckbNone, ckbSmall, ckbMedium, ckbLarge;
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
        foodList = db.getReference("Foods");
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
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

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

    //showAddFoodDialog() method
    private void showAddFoodDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle(getResources().getString(R.string.txt_title_add_new_food));
        alertDialog.setMessage(getResources().getString(R.string.txt_content_add_new_menu));

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.layout_add_food, null);

        editName = add_menu_layout.findViewById(R.id.edit_name);
        editDescription = add_menu_layout.findViewById(R.id.edit_description);
        editPrice = add_menu_layout.findViewById(R.id.edt_price);
        editDiscount = add_menu_layout.findViewById(R.id.edt_discount);
        ckbNone = add_menu_layout.findViewById(R.id.ckb_none);
        ckbSmall = add_menu_layout.findViewById(R.id.ckb_small);
        ckbMedium = add_menu_layout.findViewById(R.id.ckb_medium);
        ckbLarge = add_menu_layout.findViewById(R.id.ckb_large);

        ivSelect = add_menu_layout.findViewById(R.id.iv_select_image);
        tvPost = add_menu_layout.findViewById(R.id.tv_post);
        tvCancel = add_menu_layout.findViewById(R.id.tv_cancel);

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_add_cart);
        final AlertDialog dialog = alertDialog.show();

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

        ckbNone.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                }
            }
        });
        ckbSmall.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                }
            }
        });
        ckbMedium.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                }
            }
        });
        ckbLarge.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                }
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
        }
    }

    private void createFood(String uri, AlertDialog dialog) {
        if (ckbNone.isChecked())
            isSize = "false";
        else isSize = "true";
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
                                if (!ckbNone.isChecked())
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
        if (ckbSmall.isChecked())
            createFoodSize(foodId, 1);
        if (ckbMedium.isChecked())
            createFoodSize(foodId, 2);
        if (ckbLarge.isChecked())
            createFoodSize(foodId, 3);
    }

    private void createFoodSize(int foodId, int sizeId) {
        compositeDisposable.add(anNgonAPI.createFoodSize(Common.API_KEY,
                foodId,
                sizeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                                //Todo
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
                    .load(saveUri)
                    .into(ivSelect);
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
        foodList.child(key).removeValue();
    }

    //showUpdateFoodDialog() method
    private void showUpdateFoodDialog(final String key, final Food item) {

        //just copy code from showAddFoodDialog() method
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodListActivity.this);
        alertDialog.setTitle("Edit Food Food");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout, null);

        editName = add_menu_layout.findViewById(R.id.edit_name_anf);
        editDescription = add_menu_layout.findViewById(R.id.edit_description_anf);
        editPrice = add_menu_layout.findViewById(R.id.edit_price_anf);
        editDiscount = add_menu_layout.findViewById(R.id.edit_discount_anf);

        //Set default value for View
        editName.setText(item.getName());
        editDescription.setText(item.getDescription());

        editDiscount.setText(item.getDiscount());

        ivSelect = add_menu_layout.findViewById(R.id.btn_select_anf);
        tvPost = add_menu_layout.findViewById(R.id.btn_upload_anf);

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
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_add_cart);

        //setButton
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Update information


                Snackbar.make(rootLayout, "Category " + item.getName() + " edited Successfully", Snackbar.LENGTH_SHORT).show();

            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }


    //changeImage() method
    private void changeImage(final Food item) {
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
                            Toast.makeText(FoodListActivity.this, "Upload Successfully!!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    item.setImage(uri.toString());
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

    private void fetchData(int menuId) {
        foods.clear();
        compositeDisposable.add(anNgonAPI.getFoodOfMenu(Common.API_KEY, menuId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foodModel -> {
                            if (foodModel.isSuccess()) {
                                foods.addAll(foodModel.getResult());
                                foodListAdapter.notifyDataSetChanged();
                                swrFood.setRefreshing(false);
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
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

    }
}
