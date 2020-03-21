package com.trangdv.orderfoodserver.ui.orderdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.OrderItemAdapter;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.OrderDetail;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderItemFragment extends Fragment implements OrderItemAdapter.ItemListener {
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    private RecyclerView rvOrderItem;
    private RecyclerView.LayoutManager layoutManager;
    private OrderItemAdapter orderItemAdapter;
    private List<OrderDetail> orderDetailList = new ArrayList<>();
    private boolean loaded = false;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loaded) {
            outState.putBoolean("loaded", true);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            loaded = savedInstanceState.getBoolean("loaded", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_detail_item, container, false);
        findViewById(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        layoutManager = new LinearLayoutManager(getContext());
        rvOrderItem.setLayoutManager(layoutManager);
        if (!loaded) {
            loadAllOrderDetail();
        } else {
            showDataLoaded();
        }

    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        dialogUtils = new DialogUtils();
    }

    private void showDataLoaded() {
        orderItemAdapter = new OrderItemAdapter(getActivity(), orderDetailList, this);
        rvOrderItem.setAdapter(orderItemAdapter);
    }

    private void findViewById(View view) {
        rvOrderItem = view.findViewById(R.id.rv_order_item);
    }

    private void loadAllOrderDetail() {
        dialogUtils.showProgress(getContext());
        compositeDisposable.add(anNgonAPI.getOrderDetailModel(Common.API_KEY, Common.currentOrder.getOrderId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderDetailModel -> {
                            if (orderDetailModel.isSuccess()) {
                                if (orderDetailModel.getResult().size() > 0) {
                                    orderDetailList = orderDetailModel.getResult();
                                    orderItemAdapter = new OrderItemAdapter(getActivity(), orderDetailList, this);
                                    rvOrderItem.setAdapter(orderItemAdapter);
                                }
                            }
                            dialogUtils.dismissProgress();
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                        }
                ));

    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void dispatchToFoodDetail(int position) {

    }
}
