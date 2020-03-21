package com.trangdv.orderfoodserver.ui.orderdetail;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.OrderItemAdapter;
import com.trangdv.orderfoodserver.adapter.PagerOrderDetailAdapger;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.OrderDetail;
import com.trangdv.orderfoodserver.model.Status;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.ui.OrderActivity;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderDetailActivity extends AppCompatActivity implements OrderItemAdapter.ItemListener, View.OnClickListener {
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    private TextView tvOrderNumber, tvOrderOn, tvPrice;
    private ImageView ivBack;
    private SwipeRefreshLayout refreshLayout;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        findViewById();
        init();
        initView();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void initView() {
        Date date = Common.currentOrder.getOrderDate();
        String day          = (String) DateFormat.format("dd",   date); // 20
        String monthString  = (String) DateFormat.format("MMM",  date); // Jun

        tvOrderOn.setText(new StringBuilder(day).append(" ").append(monthString));
        tvOrderNumber.setText(new StringBuffer("#").append(String.valueOf(Common.currentOrder.getOrderId())));
        tvPrice.setText(new StringBuilder(String.valueOf(Common.currentOrder.getTotalPrice())).append("đ"));
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        dialogUtils = new DialogUtils();

        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        viewPager.setAdapter(new PagerOrderDetailAdapger(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    /*private void initStatusSpinner() {
        List<Status> statusList = new ArrayList<>();
        statusList.add(new Status(0, "Placed"));
        statusList.add(new Status(1, "Shipping"));
        statusList.add(new Status(2, "Shipped"));
        statusList.add(new Status(-1, "Cancelled"));

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        *//*spStatus.setAdapter(arrayAdapter);
        spStatus.setSelection(Common.convertStatusToIndex(Common.currentOrder.getOrderStatus()));*//*

    }*/

    private void findViewById() {
//        spStatus = findViewById(R.id.status_spinner);
        tvOrderNumber = findViewById(R.id.tv_order_number);
        refreshLayout = findViewById(R.id.swr_order_detail);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tvOrderOn = findViewById(R.id.tv_ordered_on);
        tvOrderNumber = findViewById(R.id.tv_order_number);
        tvPrice = findViewById(R.id.tv_price);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
    }

    public Fragment getFragmentCurrent() {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem());
    }

    public void sendResult() {
        Intent intent = new Intent(this, OrderActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(OrderActivity.KEY_CHANGE_STATUS, true);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }
}
