package com.trangdv.orderfoodserver.service;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.model.Token;

public class MyFirebaseIdService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseIdService";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnSuccessListener(
                        (Activity) getApplicationContext()
                        , new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                String newToken = instanceIdResult.getToken();
                                Log.e("newToken", newToken);
                                if (Common.currentUser != null) {
                                    updateToServer(newToken);
                                }

                            }
                        });
    }

    private void updateToServer(String refreshedToken) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(refreshedToken, false);
        // false because token send from client app

        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }
}
