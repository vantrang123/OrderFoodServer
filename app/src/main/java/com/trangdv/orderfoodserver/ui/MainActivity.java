package com.trangdv.orderfoodserver.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.MenuAdapter;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.Category;
import com.trangdv.orderfoodserver.model.RestaurantOwner;
import com.trangdv.orderfoodserver.model.eventbus.FoodListEvent;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.ui.dialog.AddMenuDialog;
import com.trangdv.orderfoodserver.ui.dialog.ConfirmLogoutDialog;
import com.trangdv.orderfoodserver.utils.DialogUtils;
import com.trangdv.orderfoodserver.utils.SharedPrefs;
import com.trangdv.orderfoodserver.viewholder.MenuViewHolder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.trangdv.orderfoodserver.ui.LoginActivity.SAVE_RESTAURANT_OWNER;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MenuAdapter.ItemListener {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;
    FirebaseStorage storage;
    StorageReference storageReference;

    FragmentManager fragmentManager;
    RecyclerView rvMenu;
    LinearLayoutManager layoutManager;
    Toolbar toolbar;
    AddMenuDialog addMenuDialog;
    private TextView txtUserName, tvUserPhone, tvPost, tvCancel;
    private EditText editName, edtDescription;
    private ImageView ivSelect;
    private SwipeRefreshLayout swrMenu;
    private FloatingActionButton fabAddMenu;

    Uri saveUri;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    private List<Category> categoryList = new ArrayList<>();
    private MenuAdapter menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();

        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        // event click
        eventClick();

        fragmentManager = getSupportFragmentManager();
        DrawerLayout drawer = findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        RestaurantOwner owner = SharedPrefs.getInstance().get(SAVE_RESTAURANT_OWNER, RestaurantOwner.class);
        Common.currentRestaurantOwner = owner;

        View headerView = navigationView.getHeaderView(0);
        txtUserName = headerView.findViewById(R.id.tv_username);
        tvUserPhone = headerView.findViewById(R.id.tv_userPhone);
        txtUserName.setText(Common.currentRestaurantOwner.getName());
        tvUserPhone.setText(Common.currentRestaurantOwner.getUserPhone());

        init();
        refreshToke();

        subscribeToTopic(Common.getTopicChannel(Common.currentRestaurantOwner.getRestaurantId()));
    }

    private void eventClick() {
        fabAddMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMenuDialog();
            }
        });
    }

    private void findViewById() {
        rvMenu = findViewById(R.id.recycler_menu);
        toolbar = findViewById(R.id.toolbar);
        swrMenu = findViewById(R.id.swr_menu);
        fabAddMenu = findViewById(R.id.fab_add_menu);
    }

    private void refreshToke() {
        Paper.book().write(Common.REMENBER_FBID, Common.currentRestaurantOwner.getFbid());
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        compositeDisposable.add(anNgonAPI.updateToken(Common.API_KEY,
                                Common.currentRestaurantOwner.getFbid(),
                                task.getResult().getToken())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(tokenModel -> {
                                        }
                                        , throwable -> {
                                        }
                                ));
                    }
                });
    }

    private void loadMenu() {
        categoryList.clear();
        compositeDisposable.add(
                anNgonAPI.getCategories(Common.API_KEY, Common.currentRestaurantOwner.getRestaurantId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(menuModel -> {
                            categoryList.clear();
                            categoryList.addAll(menuModel.getResult());
                            menuAdapter.notifyDataSetChanged();
                            swrMenu.setRefreshing(false);
                        }, throwable -> {
                            swrMenu.setRefreshing(false);
                            Toast.makeText(this, "[GET CATEGORY]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void init() {
        //Init FirebaseStorage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Paper.init(this);
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        rvMenu.setLayoutManager(layoutManager);
        menuAdapter = new MenuAdapter(this, categoryList, this);
        rvMenu.setAdapter(menuAdapter);

        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();

        swrMenu.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_orange_dark);

        swrMenu.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.currentRestaurantOwner.getRestaurantId()==0) {
                    new SweetAlertDialog(MainActivity.this)
                            .setContentText("Tài khoản của bạn chưa được xác thực, vui lòng liên lạc *** để được hướng dẫn!")
                            .setTitleText("Opps..")
                            .show();
                } else {
                    loadMenu();
                }
                swrMenu.setRefreshing(false);
            }
        });
        swrMenu.post(new Runnable() {
            @Override
            public void run() {
                if (Common.currentRestaurantOwner.getRestaurantId()==0) {
                    new SweetAlertDialog(MainActivity.this)
                            .setContentText("Tài khoản của bạn chưa được xác thực, vui lòng liên lạc *** để được hướng dẫn!")
                            .setTitleText("Opps..")
                            .show();
                } else {
                    loadMenu();
                }
            }
        });

        addMenuDialog = new AddMenuDialog();
    }

    private void subscribeToTopic(String topicChannel) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicChannel)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed! You may not receive", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        } else {
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
//            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(final Category item, int position) {

        //Just copy & past showDialog() and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Cập nhật Menu");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.layout_update_menu, null);

        editName = add_menu_layout.findViewById(R.id.edt_name);
        edtDescription = add_menu_layout.findViewById(R.id.edt_address);
        tvPost = add_menu_layout.findViewById(R.id.tv_post);
        tvPost.setEnabled(false);
        tvCancel = add_menu_layout.findViewById(R.id.tv_cancel);
        ivSelect = add_menu_layout.findViewById(R.id.iv_select_image);

        //set default name and description
        editName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        Glide.with(this)
                .asBitmap()
                .load(item.getImage())
                .centerCrop()
                .fitCenter()
                .placeholder(R.drawable.image_default)
                .into(ivSelect);

        //Event for button
        ivSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image from gallery and save Uri of this image
            }
        });
        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_add_list);
        final AlertDialog dialog = alertDialog.show();

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item, position, dialog);
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PIC_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PIC_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            saveUri = data.getData();

            if (addMenuDialog.isVisible())
                Glide.with(this)
                        .asBitmap()
                        .load(saveUri)
                        .centerCrop()
                        .fitCenter()
                        .placeholder(R.drawable.image_default)
                        .into(addMenuDialog.ivSelectIamge);
            else {
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
    }

    private void changeImage(final Category item, int position, AlertDialog dialog) {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            //create random string
            String imageName = UUID.randomUUID().toString();
            //Toast.makeText(this, imageName, Toast.LENGTH_LONG).show();
            final StorageReference imageFolder = storageReference.child("image/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
//                            Toast.makeText(MainActivity.this, "Upload Successfully!!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    item.setImage(uri.toString());
                                    menuAdapter.notifyItemChanged(position);
                                    updateMenu(uri, item.getId(), dialog);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void updateMenu(Uri uri, int menuId, AlertDialog dialog) {
        compositeDisposable.add(anNgonAPI.updateMenu(Common.API_KEY,
                menuId,
                editName.getText().toString(),
                edtDescription.getText().toString(),
                uri.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                                loadMenu();
                                dialog.dismiss();
//                                createRestaurantMenu(menuModel.getResult().get(0).getId());
                                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText(getResources().getString(R.string.txt_title_add_new_menu_success))
                                        .setContentText(getResources().getString(R.string.txt_content_add_new_menu_success))
                                        .show();
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
//                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }


    ////////////////////////////////////////
    private void showAddMenuDialog() {
        addMenuDialog.show(getSupportFragmentManager(), "addmenu dialog");
    }


    public void uploadImage(String name, String description, int key) {
        if (saveUri != null) {
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            //create random string
            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("image/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Upload Successfully!!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    createMenu(uri.toString(), name, description);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void createMenu(String uri, String name, String description) {
        compositeDisposable.add(anNgonAPI.createMenu(Common.API_KEY,
                name,
                description,
                uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                                categoryList.add(menuModel.getResult().get(0));
                                menuAdapter.notifyItemInserted(categoryList.size()-1);
                                createRestaurantMenu(menuModel.getResult().get(0).getId());
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void createRestaurantMenu(int menuId) {
        compositeDisposable.add(anNgonAPI.createRestaurantMenu(Common.API_KEY,
                menuId,
                Common.currentRestaurantOwner.getRestaurantId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText(getResources().getString(R.string.txt_title_add_new_menu_success))
                                        .setContentText(getResources().getString(R.string.txt_content_add_new_menu_success))
                                        .show();
                            } else {
                                Toast.makeText(this, "[GET FOOD RESULT]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            Toast.makeText(this, "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    public void Orders() {
        Intent intent = new Intent(MainActivity.this, OrderActivity.class);
        startActivity(intent);
        Common.animateStart(this);
    }

    private void confirmLogout() {
        new ConfirmLogoutDialog().show(getSupportFragmentManager(), "confirmlogutdialog");
    }

    public void reSelectItem() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                if (item.isChecked()) item.setChecked(false);
                else {
                }
                item.setChecked(true);
                break;

            case R.id.nav_order:
                if (item.isChecked()) item.setChecked(false);
                else {
                    Orders();
                }
                item.setChecked(true);
                break;

            case R.id.nav_exit:
                if (item.isChecked()) item.setChecked(false);
                else {
                    confirmLogout();
                }
                item.setChecked(true);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        dialogUtils.dismissProgress();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void dispatchToFoodList(int position) {
        EventBus.getDefault().postSticky(new FoodListEvent(true, categoryList.get(position)));
        startActivity(new Intent(this, FoodListActivity.class));
        Common.animateStart(this);
    }

    @Override
    public void dispatchToEditingMenu(int position) {
        showUpdateDialog(categoryList.get(position), position);
    }

    @Override
    public void deleteMenu(int position) {
        compositeDisposable.add(anNgonAPI.deleteRestaurantMenu(Common.API_KEY,
                categoryList.get(position).getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(restaurantMenuModel -> {
                            if (restaurantMenuModel.isSuccess()) {
                                compositeDisposable.add(anNgonAPI.deleteMenu(Common.API_KEY,
                                        categoryList.get(position).getId())
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(menuModel -> {
                                                    if (menuModel.isSuccess()) {
                                                        deleteMenuFood(position);
                                                    } else {
                                                        Toast.makeText(this, "[DELETE RESTAURANT_MENU]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }

                                                },
                                                throwable -> {
//                                                    Toast.makeText(this, "[DELETE RESTAURANT_MENU]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    deleteMenuFood(position);
                                                }
                                        ));
                            } else {
                                Toast.makeText(this, "[DELETE MENU]" + restaurantMenuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            Toast.makeText(this, "[DELETE MENU]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));

    }

    private void deleteMenuFood(int position) {
        compositeDisposable.add(anNgonAPI.deleteMenuFood(Common.API_KEY,
                categoryList.get(position).getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(menuModel -> {
                            if (menuModel.isSuccess()) {
                                categoryList.remove(position);
                                menuAdapter.notifyItemRemoved(position);
                                new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Xóa menu")
                                        .setContentText("Đã xóa menu thành công!")
                                        .show();
                            } else {
                                Toast.makeText(this, "[DELETE RESTAURANT_MENU]" + menuModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        },
                        throwable -> {
                            Toast.makeText(this, "[DELETE RESTAURANT_MENU]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }
}
