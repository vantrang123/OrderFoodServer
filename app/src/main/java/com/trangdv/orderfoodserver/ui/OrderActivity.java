package com.trangdv.orderfoodserver.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.trangdv.orderfoodserver.model.ShipperOrder;
import com.trangdv.orderfoodserver.model.Token;
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
    private ImageView ivBack;
    RecyclerView.LayoutManager layoutManager;
    SwipeRefreshLayout refreshLayout;
    MaterialSpinner spinner, shipperSpinner;
    List<Order> orderList = new ArrayList<>();

    private int maxData = 0;
    private boolean isLoading = false;
    private int idItemSelected;
    public int status = 0;

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


        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void findViewById() {
        refreshLayout = findViewById(R.id.swr_order);
        rvListOrder = findViewById(R.id.listOrders);
        ivBack = findViewById(R.id.iv_back);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(this);
        rvListOrder.setLayoutManager(layoutManager);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
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

    public void getShippingOrder(int restaurantId, int orderId) {
        compositeDisposable.add(
                anNgonAPI.getShippingOrder(Common.API_KEY, restaurantId, orderId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(shippingOrderModel -> {
                                    if (shippingOrderModel.isSuccess()) {
                                        if (shippingOrderModel.getResult().size() > 0) {
                                            for (ShipperOrder shipperOrder : shippingOrderModel.getResult()) {
                                                if (shipperOrder.getShippingStatus() > status) {
                                                    status = shipperOrder.getShippingStatus();
                                                }
                                            }
                                            gotoOrderDetail();
                                        }
                                        else {
                                            status  = 0;
                                            gotoOrderDetail();
                                        }
                                    } else {
                                        dialogUtils.dismissProgress();
                                        status  = 0;
                                        gotoOrderDetail();
                                    }
                                },
                                throwable -> {
                                    dialogUtils.dismissProgress();
                                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                })
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

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
        Common.currentOrder = orderList.get(position);
        getShippingOrder(Common.currentOrder.getRestaurantId(), Common.currentOrder.getOrderId());
        idItemSelected = position;
    }

    private void gotoOrderDetail() {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("Status", status);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_ORDER_DETAIL);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Common.animateFinish(this);
    }
}
