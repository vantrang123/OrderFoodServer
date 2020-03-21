package com.trangdv.orderfoodserver.retrofit;


import com.trangdv.orderfoodserver.model.MaxOrderModel;
import com.trangdv.orderfoodserver.model.OrderDetailModel;
import com.trangdv.orderfoodserver.model.OrderModel;
import com.trangdv.orderfoodserver.model.RestaurantOwnerModel;
import com.trangdv.orderfoodserver.model.TokenModel;
import com.trangdv.orderfoodserver.model.UpdateOrderModel;
import com.trangdv.orderfoodserver.model.UpdateRestaurantOwnerModel;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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

    @GET("orderbyrestaurant")
    Observable<OrderModel> getOrder(@Query("key") String apiKey,
                                    @Query("restaurantId") int restaurantId,
                                    @Query("from") int from,
                                    @Query("to") int to);

    @GET("maxorderbyrestaurant")
    Observable<MaxOrderModel> getMaxOrder(@Query("key") String apiKey,
                                          @Query("restaurantId") int restaurantId);

    @PUT("updateOrder")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrderStatus(@Field("key") String apiKey,
                                             @Field("orderId") int orderId,
                                             @Field("orderStatus") int orderStatus);

    @GET("orderdetailbyrestaurant")
    Observable<OrderDetailModel> getOrderDetailModel(@Query("key") String apiKey,
                                                     @Query("orderId") int orderId);

    @GET("token")
    Observable<TokenModel> getToken(@Query("key") String apiKey,
                                    @Query("fbid") String fbid);

    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateToken(@Field("key") String apiKey,
                                       @Field("fbid") String fbid,
                                       @Field("token") String token);

    /*@DELETE("favorite")
    Observable<FavoriteModel> removeFavorite(@Query("key") String apiKey,
                                             @Query("fbid") String fbid,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);*/
}
