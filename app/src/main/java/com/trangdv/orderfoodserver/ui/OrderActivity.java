package com.trangdv.orderfoodserver.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.OrderAdapter;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.Notification;
import com.trangdv.orderfoodserver.model.Order;
import com.trangdv.orderfoodserver.model.Request;
//import com.trangdv.orderfoodserver.remote.APIService;
import com.trangdv.orderfoodserver.model.Sender;
import com.trangdv.orderfoodserver.model.Token;
import com.trangdv.orderfoodserver.remote.IFCMService;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.ui.orderdetail.OrderDetailActivity;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderActivity extends AppCompatActivity implements OnMapReadyCallback, OrderAdapter.ItemListener {
    public static final String KEY_CHANGE_STATUS = "change status";
    private static final int REQUEST_CODE_ORDER_DETAIL = 2020;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    FirebaseDatabase database;
    DatabaseReference request;

    OrderAdapter orderAdapter;
    RecyclerView rvListOrder;
    RecyclerView.LayoutManager layoutManager;
    SwipeRefreshLayout refreshLayout;
    MaterialSpinner spinner, shipperSpinner;
    List<Order> orderList = new ArrayList<>();

    private int maxData = 0;
    private boolean isLoading = false;
    private int idItemSelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        findViewById();
        initView();
        init();
        initScrollListener();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        dialogUtils = new DialogUtils();
        database = FirebaseDatabase.getInstance();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                orderList.clear();
                loadMaxOrder();
            }
        });
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadMaxOrder();
            }
        });
    }

    private void loadMaxOrder() {
        dialogUtils.showProgress(this);
        compositeDisposable.add(anNgonAPI.getMaxOrder(Common.API_KEY, Common.currentRestaurantOwner.getRestaurantId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(maxOrderModel -> {
                            if (maxOrderModel.isSuccess()) {
                                if (maxOrderModel.getResult().size() > 0) {
                                    maxData = maxOrderModel.getResult().get(0).getMaxRowNum();
                                    orderAdapter = null;
                                    loadAllOrders(0, 10);
                                }
                            }
                            dialogUtils.dismissProgress();
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                        }
                ));
        refreshLayout.setRefreshing(false);
    }

    private void loadAllOrders(int form, int to) {
        dialogUtils.showProgress(this);
        compositeDisposable.add(anNgonAPI.getOrder(Common.API_KEY, Common.currentRestaurantOwner.getRestaurantId(),
                form, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderModel -> {
                            if (orderModel.isSuccess()) {
                                if (orderModel.getResult().size() > 0) {
                                    if (orderAdapter == null) {
                                        orderList = new ArrayList<>();
                                        orderList = orderModel.getResult();
                                        orderAdapter = new OrderAdapter(OrderActivity.this, orderList, this);
                                        rvListOrder.setAdapter(orderAdapter);
                                    } else {
                                        orderAdapter.removeNull();
                                        orderList = orderModel.getResult();
                                        orderAdapter.addItem(orderList);
                                    }
                                }
                            } else {
                                orderAdapter.notifyItemRemoved(orderAdapter.getItemCount());
                            }
                            dialogUtils.dismissProgress();
                            isLoading = false;

                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                            isLoading = false;
                        }
                ));
    }

    private void findViewById() {
        refreshLayout = findViewById(R.id.swr_order);
        rvListOrder = findViewById(R.id.listOrders);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(this);
        rvListOrder.setLayoutManager(layoutManager);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
//            showUpdateDialog(adpter.getRef(item.getOrder()).getKey(), adpter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
//            deleteOrder(adpter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(String key, final Request item) {

        /*final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please Choose Status");

        final LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);

        spinner = (MaterialSpinner) view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "Preparing Orders", "Shipping", "Delivered");

        shipperSpinner = (MaterialSpinner) view.findViewById(R.id.shipperSpinner);

        //load all shipper to spinner
        final List<String> shipperList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.SHIPPER_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot shipperSnapshot : dataSnapshot.getChildren())
                            shipperList.add(shipperSnapshot.getKey());
                        shipperSpinner.setItems(shipperList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        alertDialog.setView(view);

        //final String localKey = key;
        globalKey = key;
        iRequest = item;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                item.getLatitude();
                item.getLongitude();
                if (item.getStatus().equals("2")) {
                    sendOrderShipRequestToShipper(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString(), item);

                } else {
                    request.child(globalKey).setValue(item);
                    adpter.notifyDataSetChanged(); //add to update item size

                    sendOrderStatusToUser(globalKey, item);
                }

            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertDialog.show();*/

    }

    //
    private void deleteOrder(String key) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    private void sendOrderStatusToUser(final String key, final Request item) {
        /*DatabaseReference tokens = database.getReference("Tokens");
        tokens.child(item.getPhone())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Token token = dataSnapshot.getValue(Token.class);

                            //make raw payload
                            Notification notification = new Notification("OrderFood", "Your order " + key + " was updated");
                            Sender content = new Sender(token.getToken(), notification);

                            mService.sendNotification(content).enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.body().success == 1) {
                                        Toast.makeText(OrderActivity.this, "Order was updated!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(OrderActivity.this, "Order was updated but failed to send notification!"
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/
    }

    private void sendOrderShipRequestToShipper(String shipperPhone, Request item) {

        DatabaseReference tokens = database.getReference("Tokens");

        tokens.child(shipperPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Token token = dataSnapshot.getValue(Token.class);

                            //make raw payload
//                            Notification notification = new Notification("OrderFood", "You have new order need ship");
                            Notification notification = new Notification("OrderFood", getString(R.string.text_notification_order_to_shipper));
                            Sender content = new Sender(token.getToken(), notification);

//                            mService.sendNotification(content).enqueue(new Callback<FCMResponse>() {
//                                @Override
//                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                                    if (response.body().success == 1) {
//                                        updateInfo();
//                                        Toast.makeText(OrderActivity.this, "Sent to Shippers!", Toast.LENGTH_SHORT).show();
//                                    } else {
//
//
//                                        Toast.makeText(OrderActivity.this, "Failed to send notification!"
//                                                , Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<FCMResponse> call, Throwable t) {
//                                    Log.e("ERROR", t.getMessage());
//
//                                }
//                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateInfo() {
        /*FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIP_TABLE)
                .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                .child(globalKey)
                .setValue(iRequest);

        request.child(globalKey).setValue(iRequest);
        adpter.notifyDataSetChanged(); //add to update item size

        sendOrderStatusToUser(globalKey, iRequest);*/
    }

    private void initScrollListener() {
        rvListOrder.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == orderAdapter.getItemCount() - 1) {
                        isLoading = true;
                        loadMoreData();

                    }
                }
            }
        });
    }

    private void loadMoreData() {
        if (orderAdapter.getItemCount() < maxData) {
            int from = orderAdapter.getItemCount() + 1;
            orderAdapter.addNull();
            loadAllOrders(from, from + 10);
        } else {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ORDER_DETAIL && resultCode == Activity.RESULT_OK && data != null) {
            orderList.get(idItemSelected).setOrderStatus(Common.currentOrder.getOrderStatus());
            orderAdapter.notifyItemChanged(idItemSelected);
        }
    }

    @Override
    protected void onStop() {
        dialogUtils.dismissProgress();
        super.onStop();
    }

    @Override
    public void dispatchToOrderDetail(int position) {
        dialogUtils.showProgress(this);
        idItemSelected = position;
        startActivityForResult(new Intent(this, OrderDetailActivity.class), REQUEST_CODE_ORDER_DETAIL);
    }

}
