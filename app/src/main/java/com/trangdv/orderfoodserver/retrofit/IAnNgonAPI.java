package com.trangdv.orderfoodserver.retrofit;


import com.trangdv.orderfoodserver.model.FoodModel;
import com.trangdv.orderfoodserver.model.MaxOrderModel;
import com.trangdv.orderfoodserver.model.MenuModel;
import com.trangdv.orderfoodserver.model.OrderDetailModel;
import com.trangdv.orderfoodserver.model.OrderModel;
import com.trangdv.orderfoodserver.model.RestaurantMenuModel;
import com.trangdv.orderfoodserver.model.RestaurantOwnerModel;
import com.trangdv.orderfoodserver.model.ShipperModel;
import com.trangdv.orderfoodserver.model.ShippingOrderModel;
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
                                                                 @Field("fbid") String fbid,
                                                                 @Field("password") String password);

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

    @GET("menu")
    Observable<MenuModel> getCategories(@Query("key") String apiKey,
                                        @Query("restaurantId") int restaurantId);

    @GET("food")
    Observable<FoodModel> getFoodOfMenu(@Query("key") String apiKey,
                                        @Query("menuId") int menuId);

    @POST("createmenu")
    @FormUrlEncoded
    Observable<MenuModel> createMenu(@Field("key") String apiKey,
                                     @Field("name") String name,
                                     @Field("description") String description,
                                     @Field("image") String image);

    @PUT("updatemenu")
    @FormUrlEncoded
    Observable<MenuModel> updateMenu(@Field("key") String apiKey,
                                     @Field("menuId") int menuId,
                                     @Field("name") String name,
                                     @Field("description") String description,
                                     @Field("image") String image);

    @POST("restaurantmenu")
    @FormUrlEncoded
    Observable<RestaurantMenuModel> createRestaurantMenu(@Field("key") String apiKey,
                                                         @Field("menuId") int menuId,
                                                         @Field("restaurantId") int restaurantId);

    @POST("createfood")
    @FormUrlEncoded
    Observable<FoodModel> createFood(@Field("key") String apiKey,
                                     @Field("name") String name,
                                     @Field("description") String description,
                                     @Field("image") String image,
                                     @Field("price") float price,
                                     @Field("isSize") String isSize,
                                     @Field("isAddon") String isAddon,
                                     @Field("discount") int discount);

    @POST("menufood")
    @FormUrlEncoded
    Observable<RestaurantMenuModel> createMenuFood(@Field("key") String apiKey,
                                                   @Field("menuId") int menuId,
                                                   @Field("foodId") int foodId);

    @POST("foodsize")
    @FormUrlEncoded
    Observable<RestaurantMenuModel> createFoodSize(@Field("key") String apiKey,
                                                   @Field("foodId") int foodId,
                                                   @Field("sizeId") int sizeId);

    @GET("shipperrequestship")
    Observable<ShipperModel> getShipperRequestShip(@Query("key") String apiKey,
                                                   @Query("restaurantId") int restaurantId,
                                                   @Query("orderId") int orderId);

    @GET("shippingorder")
    Observable<ShippingOrderModel> getShippingOrder(@Query("key") String apiKey,
                                                   @Query("restaurantId") int restaurantId,
                                                   @Query("orderId") int orderId);

    @POST("shippingorder")
    @FormUrlEncoded
    Observable<ShippingOrderModel> setShippingOrder(@Field("key") String apiKey,
                                                    @Field("orderId") int orderId,
                                                    @Field("restaurantId") int restaurantId,
                                                    @Field("shipperId") String shipperId,
                                                    @Field("status") int status,
                                                    @Field("orderFBID") String orderFBID);

    @GET("orderneedship")
    Observable<OrderModel> getOrderNeedShip(@Query("key") String apiKey,
                                            @Query("restaurantId") int restaurantId);

    /*@DELETE("favorite")
    Observable<FavoriteModel> removeFavorite(@Query("key") String apiKey,
                                             @Query("fbid") String fbid,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);*/
}
