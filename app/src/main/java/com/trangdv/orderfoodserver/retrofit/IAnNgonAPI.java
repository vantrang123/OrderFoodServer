package com.trangdv.orderfoodserver.retrofit;


import com.trangdv.orderfoodserver.model.RestaurantOwnerModel;
import com.trangdv.orderfoodserver.model.UpdateRestaurantOwnerModel;

import io.reactivex.Observable;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IAnNgonAPI {

    @GET("restaurantowner")
    Observable<RestaurantOwnerModel> getRestaurantOwner(@Query("key") String apiKey,
                                                        @Query("fbid") String fbid);

    @POST("restaurantowner")
    @FormUrlEncoded
    Observable<UpdateRestaurantOwnerModel> updateRestaurantOwner(@Field("key") String apiKey,
                                                                 @Field("userPhone") String userPhone,
                                                                 @Field("userName") String userName,
                                                                 @Field("fbid") String fbid);

    /*@DELETE("favorite")
    Observable<FavoriteModel> removeFavorite(@Query("key") String apiKey,
                                             @Query("fbid") String fbid,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);*/


}
