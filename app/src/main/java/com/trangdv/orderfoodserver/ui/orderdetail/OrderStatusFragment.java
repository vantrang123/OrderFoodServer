package com.trangdv.orderfoodserver.ui.orderdetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.OrderAdapter;
import com.trangdv.orderfoodserver.adapter.OrderItemAdapter;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.FCMSendData;
import com.trangdv.orderfoodserver.remote.IFCMService;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.retrofit.RetrofitFCMClient;
import com.trangdv.orderfoodserver.ui.dialog.ConfirmUpdateOrderStatusDialog;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderStatusFragment extends Fragment implements View.OnClickListener {
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;
    IFCMService ifcmService;

    private ImageView ivOrderPlaced, ivShipping, ivShipped, ivCancelled;
//    private TextView tvUpdateStatus;
    private int statusId = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_status, container, false);
        findViewById(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        initStatus();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        dialogUtils = new DialogUtils();
        ifcmService = RetrofitFCMClient.getInstance(Common.fcmUrl).create(IFCMService.class);
    }

    private void findViewById(View view) {
        ivOrderPlaced = view.findViewById(R.id.iv_order_placed);
        ivOrderPlaced.setOnClickListener(this);
        ivShipping = view.findViewById(R.id.iv_shipping);
        ivShipping.setOnClickListener(this);
        ivShipped = view.findViewById(R.id.iv_shipped);
        ivShipped.setOnClickListener(this);
        ivCancelled = view.findViewById(R.id.iv_cancelled);
        ivCancelled.setOnClickListener(this);
//        tvUpdateStatus = view.findViewById(R.id.tv_update_status);
//        tvUpdateStatus.setOnClickListener(this);
    }

    public void initStatus() {
        int id = Common.currentOrder.getOrderStatus();
        updateCorlorStatus(id);
    }
    public void updateOrderStatus() {
        dialogUtils.showProgress(getContext());
        compositeDisposable.add(anNgonAPI.updateOrderStatus(Common.API_KEY,
                Common.currentOrder.getOrderId(),
                statusId
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateOrderModel -> {
                            getToken();
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                        }
                ));
    }

    private void getToken() {
        compositeDisposable.add(anNgonAPI.getToken(Common.API_KEY,
                Common.currentOrder.getOrderFBID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tokenModel -> {
                            if (tokenModel.isSuccess()) {
                                Map<String, String> messageSend = new HashMap<>();
                                messageSend.put(Common.NOTIFI_TITLE, "Your order has been updated");
                                messageSend.put(Common.NOTIFI_CONTENT, new StringBuilder("Your order ")
                                .append(Common.currentOrder.getOrderId())
                                .append(" has been update to")
                                .append(Common.convertCodeToStatus(statusId)).toString());

                                FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(), messageSend);
                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(fcmResponse -> {
                                            Toast.makeText(getContext(), "[UPDATE SUCCESS]", Toast.LENGTH_SHORT).show();
                                            updateCorlorStatus(statusId);
                                            Common.currentOrder.setOrderStatus(statusId);
                                            ((OrderDetailActivity)getActivity()).sendResult();
                                            dialogUtils.dismissProgress();
                                        }, throwable -> {
                                            Toast.makeText(getContext(), "[UPDATE FAILED]", Toast.LENGTH_SHORT).show();
                                            dialogUtils.dismissProgress();
                                        } )
                                );
                            }
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                        }
                ));
    }

    private void updateCorlorStatus(int id) {
        switch (id) {
            case -1:
                ivCancelled.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivCancelled.setClickable(false);
            case 2:
                ivShipped.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivShipped.setClickable(false);
            case 1:
                ivShipping.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivShipping.setClickable(false);
            case 0:
                ivOrderPlaced.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivOrderPlaced.setClickable(false);
                break;
            default:
                break;
        }
    }

    private void showConfirmDialog() {
        new ConfirmUpdateOrderStatusDialog().show(getFragmentManager(), "ConfirmUpdateStatus");
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_order_placed:
                statusId = 0;
//                ivOrderPlaced.setClickable(true);
                showConfirmDialog();
                break;
            case R.id.iv_shipping:
                statusId = 1;
//                ivShipping.setClickable(true);
                showConfirmDialog();
                break;
            case R.id.iv_shipped:
                statusId = 2;
//                ivShipped.setClickable(true);
                showConfirmDialog();
                break;
            case R.id.iv_cancelled:
                statusId = -1;
//                ivCancelled.setClickable(true);
                showConfirmDialog();
                break;
            default:
                break;
        }
    }
}
