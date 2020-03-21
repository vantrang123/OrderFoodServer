package com.trangdv.orderfoodserver.service;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.retrofit.IAnNgonAPI;
import com.trangdv.orderfoodserver.retrofit.RetrofitClient;

import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyFirebaseIdService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseIdService";
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        Paper.init(this);
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onNewToken(@NonNull String newToken) {
        super.onNewToken(newToken);
        String fbid = Paper.book().read(Common.REMENBER_FBID);
        String apiKey = Paper.book().read(Common.API_KEY_TAG);

        compositeDisposable.add(anNgonAPI.updateToken(apiKey,fbid,newToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tokenModel -> {

                        }
                        , throwable -> {
                        }
                ));

        /*FirebaseInstanceId.getInstance()
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
                        });*/
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String,String> dataRecv = remoteMessage.getData();
        if (dataRecv != null) {
            Common.showNotification(this,
                    new Random().nextInt(),
                    dataRecv.get(Common.NOTIFI_TITLE),
                    dataRecv.get(Common.NOTIFI_CONTENT),
                    null);
        }
    }

    /*private void updateToServer(String refreshedToken) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(refreshedToken, false);
        // false because token send from client app

        tokens.child(Common.currentUser.getUserPhone()).setValue(data);
    }*/

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
