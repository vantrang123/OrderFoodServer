package com.trangdv.orderfoodserver.remote;


import com.trangdv.orderfoodserver.model.MyResponse;
import com.trangdv.orderfoodserver.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAADXD-qcQ:APA91bFGJEu8uSNhP9CdMbm_SqsC7WZyf8WSw65OWfD1KZZFwgrV2t9GVfDnx7hWjSCn-FwyZtFoNrf3BYe-Gq6DkBJXLM_EtHCn8e61xhU8d0iLorykhNoodUjr7dTMqxj71VOYGuck"
            }

    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
