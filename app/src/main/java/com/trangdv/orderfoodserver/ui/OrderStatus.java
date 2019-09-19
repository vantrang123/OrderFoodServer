package com.trangdv.orderfoodserver.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.listener.ItemClickListener;
import com.trangdv.orderfoodserver.model.Request;
//import com.trangdv.orderfoodserver.remote.APIService;
import com.trangdv.orderfoodserver.viewholder.OrderViewHolder;

public class OrderStatus extends AppCompatActivity implements OnMapReadyCallback {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adpter;
    FirebaseDatabase database;
    DatabaseReference request;
    MaterialSpinner spinner;
    //APIService mService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderstatus);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Requests");

        //mService = Common.getFCMClient();

        initView();
        loadOrders();
    }

    private void initView() {
        recyclerView = findViewById(R.id.listOrders);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void loadOrders() {
        adpter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_item,
                OrderViewHolder.class,
                request
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder holder, final Request request, int position) {
                holder.txtOrderId.setText(adpter.getRef(position).getKey());
                holder.txtOrderStatus.setText(Common.convertCodeToStatus(request.getStatus()));
                holder.txtOrderAddress.setText(request.getAddress());
                holder.txtOrderPhone.setText(request.getPhone());

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent intent = new Intent(OrderStatus.this, TrackingOrder.class);
                        Common.currentRequest = request;
                        startActivity(intent);

                    }
                });
            }
        };
        adpter.notifyDataSetChanged();
        recyclerView.setAdapter(adpter);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateDialog(adpter.getRef(item.getOrder()).getKey(), adpter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE)) {
            deleteOrder(adpter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order, null);

        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On may way", "Shipped");
        alertDialog.setView(view);

        final String localKey = key;

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                request.child(localKey).setValue(item);
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

    //
    private void deleteOrder(String key) {
        request.child(key).removeValue();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
