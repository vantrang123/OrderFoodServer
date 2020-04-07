package com.trangdv.orderfoodserver.ui.orderdetail;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.ShippingOrderAdapter;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.FCMSendData;
import com.trangdv.orderfoodserver.model.Shipper;
import com.trangdv.orderfoodserver.model.User;
import com.trangdv.orderfoodserver.remote.IFCMService;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;
import com.trangdv.orderfoodserver.retrofit.RetrofitFCMClient;
import com.trangdv.orderfoodserver.ui.dialog.ConfirmUpdateOrderStatusDialog;
import com.trangdv.orderfoodserver.utils.DialogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderStatusFragment extends Fragment implements View.OnClickListener, ShippingOrderAdapter.ItemListener {
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;
    IFCMService ifcmService;

    private ImageView ivAcceptOrder, ivFindShipper, ivShipped, ivAcceptShipper, ivCancelled;
    private TextView tvShipperName, tvShipperAddress, tvShipperPhone, tvPost, tvCancel;
    private RecyclerView rvShipper;
    private ShippingOrderAdapter shippingOrderAdapter;
    private int statusId = 0;
    private String shipperFBID;
    private LinearLayoutManager layoutManager;

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
        ivAcceptOrder = view.findViewById(R.id.iv_accept_order);
        ivAcceptOrder.setOnClickListener(this);
        ivFindShipper = view.findViewById(R.id.iv_find_shipper);
        ivFindShipper.setOnClickListener(this);
        ivShipped = view.findViewById(R.id.iv_shipped);
        ivShipped.setOnClickListener(this);
        ivCancelled = view.findViewById(R.id.iv_cancelled);
        ivCancelled.setOnClickListener(this);
        ivAcceptShipper = view.findViewById(R.id.iv_accept_shipper);
        ivAcceptShipper.setOnClickListener(this);

//        tvUpdateStatus = view.findViewById(R.id.tv_update_status);
//        tvUpdateStatus.setOnClickListener(this);
    }

    public void initStatus() {
        int id;
        if (((OrderDetailActivity) getActivity()).status == 2) {
            id = ((OrderDetailActivity) getActivity()).status;
        } else {
            id = Common.currentOrder.getOrderStatus();
        }
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
                            getToken(Common.currentOrder.getOrderFBID());
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                        }
                ));
        if (statusId == 1) {
            setShippingOrder("0",0);
        }
        if (statusId == 3) {
            setShippingOrder(shipperFBID,1);
        }

    }

    private void setShippingOrder(String shipperFBID, int status) {
        compositeDisposable.add(anNgonAPI.setShippingOrder(Common.API_KEY,
                Common.currentOrder.getOrderId(),
                Common.currentOrder.getRestaurantId(),
                shipperFBID,
                status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shippingOrderModel -> {
                            if (shippingOrderModel.isSuccess()) {
                                dialogUtils.dismissProgress();
                                new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Chấp nhận đơn hàng")
                                        .setContentText("Bạn sẻ nhận được thông báo khi có yêu cầu từ nhân viên giao hàng")
                                        .show();
                                updateCorlorStatus(statusId);
                            } else {
                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    private void getToken(String fbid) {
        compositeDisposable.add(anNgonAPI.getToken(Common.API_KEY,
                fbid)
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
                                            updateCorlorStatus(statusId);
                                            Common.currentOrder.setOrderStatus(statusId);
                                            ((OrderDetailActivity) getActivity()).sendResult();
                                            dialogUtils.dismissProgress();
                                            Toast.makeText(getContext(), "[UPDATE SUCCESS]", Toast.LENGTH_SHORT).show();
                                        }, throwable -> {
                                            Toast.makeText(getContext(), "[UPDATE FAILED]", Toast.LENGTH_SHORT).show();
                                            dialogUtils.dismissProgress();
                                        })
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
            case 4:
                ivShipped.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivShipped.setClickable(false);
            case 3:
                ivAcceptShipper.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivAcceptShipper.setClickable(false);
            case 2:
                ivFindShipper.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivFindShipper.setClickable(false);
            case 1:
                ivAcceptOrder.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivAcceptOrder.setClickable(false);
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
            case R.id.iv_accept_order:
                statusId = 0;
//                ivOrderPlaced.setClickable(true);
                showConfirmDialog();
                break;
            case R.id.iv_find_shipper:
                statusId = 1;
//                ivShipping.setClickable(true);
                showConfirmDialog();
                break;
            case R.id.iv_shipped:
                statusId = 2;
//                ivShipped.setClickable(true);
                showConfirmDialog();
                break;
            case R.id.iv_accept_shipper:
                statusId = 3;
                dialogUtils.showProgress(getContext());
                getShipperRequestShip();
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

    private void showChoiseShipper(List<Shipper> results) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getResources().getString(R.string.txt_title_choise_shipper));
        alertDialog.setMessage(getResources().getString(R.string.txt_content_choise_shipper));

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.layout_choise_shipper, null);

        tvPost = add_menu_layout.findViewById(R.id.tv_post);
        tvCancel = add_menu_layout.findViewById(R.id.tv_cancel);
        rvShipper = add_menu_layout.findViewById(R.id.rv_shipper);

        alertDialog.setView(add_menu_layout);
        final AlertDialog dialog = alertDialog.show();

        layoutManager = new LinearLayoutManager(getContext());
        rvShipper.setLayoutManager(layoutManager);
        shippingOrderAdapter = new ShippingOrderAdapter(results, getContext(), this);
        rvShipper.setAdapter(shippingOrderAdapter);

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogUtils.showProgress(getContext());
                getToken(shipperFBID);
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialogUtils.dismissProgress();
            }
        });

    }

    private void getShipperRequestShip() {
        compositeDisposable.add(
                anNgonAPI.getShipperRequestShip(Common.API_KEY, Common.currentOrder.getRestaurantId(), Common.currentOrder.getOrderId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(shipperModel -> {
                                    if (shipperModel.isSuccess()) {
                                        if (shipperModel.getResult().size() > 0) {
                                            showChoiseShipper(shipperModel.getResult());
                                        }
                                    } else {

                                    }
                                },
                                throwable -> {

                                })
        );
    }

    @Override
    public void choiseShipper(String fbid) {
        shipperFBID = fbid;
    }
}
