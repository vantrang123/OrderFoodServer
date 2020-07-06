package com.trangdv.orderfoodserver.ui.orderdetail;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.adapter.ShippingOrderAdapter;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.FCMSendData;
import com.trangdv.orderfoodserver.model.Shipper;
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

    private ImageView ivAcceptOrder, ivChoiseShipper, ivShipped, ivShipping, ivCancelled;
    private TextView tvShipperName, tvShipperAddress, tvShipperPhone, tvPost, tvCancel;
    private RecyclerView rvShipper;
    private ShippingOrderAdapter shippingOrderAdapter;
    private int statusId = 0;
    private String shipperFBID;
    private LinearLayoutManager layoutManager;
    private View vLine1Gradient, vLine1, vLine2, vLine3, vLine4, vLine2Gradient, vLine3Gradient, vLine4Gradient;

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
        ivChoiseShipper = view.findViewById(R.id.iv_choise_shipper);
        ivChoiseShipper.setOnClickListener(this);
        ivChoiseShipper.setClickable(false);
        ivShipped = view.findViewById(R.id.iv_shipped);
        ivShipped.setOnClickListener(this);
        ivCancelled = view.findViewById(R.id.iv_cancelled);
        ivCancelled.setOnClickListener(this);
        ivShipping = view.findViewById(R.id.iv_shipping);
        ivShipping.setOnClickListener(this);
        vLine1 = view.findViewById(R.id.line1);
        vLine2 = view.findViewById(R.id.line2);
        vLine3 = view.findViewById(R.id.line3);
        vLine4 = view.findViewById(R.id.line4);

        vLine1Gradient = view.findViewById(R.id.line1gradient);
        vLine2Gradient = view.findViewById(R.id.line2gradient);
        vLine3Gradient = view.findViewById(R.id.line3gradient);
        vLine4Gradient = view.findViewById(R.id.line4gradient);

//        tvUpdateStatus = view.findViewById(R.id.tv_update_status);
//        tvUpdateStatus.setOnClickListener(this);
    }

    public void initStatus() {
        int id;
        if (((OrderDetailActivity) getActivity()).status >= Common.currentOrder.getOrderStatus()) {
            id = ((OrderDetailActivity) getActivity()).status;
        } else {
            id = Common.currentOrder.getOrderStatus();
        }
        updateCorlorStatus(id);
        statusId = id;
    }

    public void updateOrderStatus() {
        dialogUtils.showProgress(getContext());
        if (statusId == -1) {
            updateOrder();
        }
        if (statusId == 1) {
            setShippingOrder("0", 0, Common.currentOrder.getOrderFBID());
            updateOrder();
        } else if (statusId == 3) {
            setShippingOrder(shipperFBID, 3, Common.currentOrder.getOrderFBID());
//            updateOrder();
        } else {
//            updateOrder();
            dialogUtils.showProgress(getContext());
        }
    }

    private void updateOrder() {
        compositeDisposable.add(anNgonAPI.updateOrderStatus(Common.API_KEY, Common.currentOrder.getOrderId(), statusId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateOrderModel -> {
                            getToken(Common.currentOrder.getOrderFBID());
                            if (statusId == -1) {
                                updateCorlorStatus(statusId);
                            }
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                        }
                ));
    }

    private void setShippingOrder(String shipperFBID, int status, String orderFBID) {
        compositeDisposable.add(anNgonAPI.setShippingOrder(Common.API_KEY,
                Common.currentOrder.getOrderId(),
                Common.currentOrder.getRestaurantId(),
                shipperFBID,
                status,
                orderFBID)
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
                                messageSend.put(Common.NOTIFI_TITLE, "Trạng thái đơn hàng mới");
                                messageSend.put(Common.NOTIFI_CONTENT, new StringBuilder("Đơn hàng ")
                                        .append(Common.currentOrder.getOrderId())
                                        .append(" đã ")
                                        .append(Common.convertCodeToStatus(statusId)).toString());

                                FCMSendData fcmSendData = new FCMSendData(tokenModel.getResult().get(0).getToken(), messageSend);
                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(fcmResponse -> {
                                            Common.currentOrder.setOrderStatus(statusId);
                                            ((OrderDetailActivity) getActivity()).sendResult();
                                            dialogUtils.dismissProgress();
                                            updateOrderNonNotify();
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

    private void updateOrderNonNotify() {
        compositeDisposable.add(anNgonAPI.updateOrderStatus(Common.API_KEY, Common.currentOrder.getOrderId(), statusId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(updateOrderModel -> {
                        }
                        , throwable -> {
                        }
                ));
    }

    private void updateCorlorStatus(int id) {
        switch (id) {
            case -1:
                ivCancelled.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivCancelled.setClickable(false);
                ivAcceptOrder.setClickable(false);
                break;
            case 5:
                ivShipped.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivShipped.setClickable(false);
                vLine3.setVisibility(View.INVISIBLE);
                vLine3Gradient.setVisibility(View.VISIBLE);
            case 4:
                ivShipping.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivShipping.setClickable(false);
                vLine2.setVisibility(View.INVISIBLE);
                vLine2Gradient.setVisibility(View.VISIBLE);
            case 3:
                ivChoiseShipper.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivChoiseShipper.setClickable(false);
            case 2:
                vLine1.setVisibility(View.INVISIBLE);
                vLine1Gradient.setVisibility(View.VISIBLE);
                ivChoiseShipper.setClickable(true);
            case 1:
                ivAcceptOrder.setBackground(getResources().getDrawable(R.drawable.bg_iv_status));
                ivAcceptOrder.setClickable(false);
                ivCancelled.setClickable(false);
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
                statusId = 1;
//                ivOrderPlaced.setClickable(true);
                showConfirmDialog();
                break;
            case R.id.iv_choise_shipper:
                statusId = 3;
//                ivShipping.setClickable(true);
                dialogUtils.showProgress(getContext());
                getShipperRequestShip();
                break;
            /*case R.id.iv_shipping:
                statusId = 4;
                dialogUtils.showProgress(getContext());
                getShipperRequestShip();
                break;
            case R.id.iv_shipped:
                statusId = 5;
//                ivShipped.setClickable(true);
                showConfirmDialog();
                break;*/
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
                statusId = 3;
                getToken(shipperFBID);
                setShippingOrder(shipperFBID, 3, Common.currentOrder.getOrderFBID());
                dialog.dismiss();
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
                                            dialogUtils.dismissProgress();
                                        }
                                    } else {
                                        dialogUtils.dismissProgress();
                                    }
                                },
                                throwable -> {
                                    dialogUtils.dismissProgress();
                                })
        );
    }

    @Override
    public void choiseShipper(String fbid) {
        shipperFBID = fbid;
    }
}
