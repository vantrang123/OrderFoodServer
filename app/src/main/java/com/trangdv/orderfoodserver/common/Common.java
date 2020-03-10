package com.trangdv.orderfoodserver.common;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.trangdv.orderfoodserver.model.Request;
import com.trangdv.orderfoodserver.model.RestaurantOwner;
import com.trangdv.orderfoodserver.model.User;
//import com.trangdv.orderfoodserver.remote.APIService;
//import com.trangdv.orderfoodserver.remote.FCMRetrofitClient;
import com.trangdv.orderfoodserver.remote.APIService;
import com.trangdv.orderfoodserver.remote.FCMRetrofitClient;
import com.trangdv.orderfoodserver.remote.IGeoCoordinates;
import com.trangdv.orderfoodserver.remote.RetrofitClient;

public class Common {
    public static final String API_KEY = "1234";
    public static final String API_ANNGON_ENDPOINT = "http://192.168.137.1:3000";

    public static User currentUser;
    public static RestaurantOwner currentRestaurantOwner;
    public static Request currentRequest;
    public static final String DELETE = "Delete";
    public static final String UPDATE="Update";
    public static final String SHIPPER_TABLE ="Shippers" ;
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip";

    public static final int PIC_IMAGE_REQUEST = 71;

    public static final String baseUrl="https://maps.googleapis.com";
    private static final String fcmUrl="https://fcm.googleapis.com/";

    public static String convertCodeToStatus(String code){

        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "Preparing Orders";
        else if(code.equals("2"))
            return "Shipping";
        else
            return "Delivered";
    }

    public static APIService getFCMClient(){
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static IGeoCoordinates getGeoCodeServices(){
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){
        Bitmap scaledBitmap=Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);
        float scaleX=newWidth/(float)bitmap.getWidth();
        float scaleY=newHeight/(float)bitmap.getHeight();
        float pivotX=0,pivotY=0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;


    }
}