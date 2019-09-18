package com.trangdv.orderfoodserver.remote;


import com.trangdv.orderfoodserver.model.MyResponse;
import com.trangdv.orderfoodserver.model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by dell on 4/26/2018.
 */

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAvF4O8f4:APA91bErznhYWHLZOce6gEKzNV5hdIsIe6NhOJgjnrqfsK3_4_2BO0d_6148SnKAPEpbu3BPStC5plXKdo00LE96eSp4vTA2VAd2cj0Wj8x6J_Xz5agbVzjMVFahkvq97HC7_WH43HfE"

            }
    )


    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
