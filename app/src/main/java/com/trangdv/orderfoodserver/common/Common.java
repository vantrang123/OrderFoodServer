package com.trangdv.orderfoodserver.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.core.app.NotificationCompat;

import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.model.Order;
import com.trangdv.orderfoodserver.model.Request;
import com.trangdv.orderfoodserver.model.RestaurantOwner;
//import com.trangdv.orderfoodserver.remote.APIService;
//import com.trangdv.orderfoodserver.remote.FCMRetrofitClient;
import com.trangdv.orderfoodserver.model.User;
import com.trangdv.orderfoodserver.remote.IFCMService;
import com.trangdv.orderfoodserver.retrofit.RetrofitFCMClient;
import com.trangdv.orderfoodserver.remote.IGeoCoordinates;
import com.trangdv.orderfoodserver.remote.RetrofitClient;

public class Common {
    public static final String API_KEY = "1234";
    public static final String API_ANNGON_ENDPOINT = "http://192.168.137.1:3000";
    public static final String REMENBER_FBID = "REMENBER_FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFI_TITLE = "title";
    public static final String NOTIFI_CONTENT = "content";

    public static User currentUser;
    public static RestaurantOwner currentRestaurantOwner;
    public static Request currentRequest;
    public static final String DELETE = "Delete";
    public static final String UPDATE = "Update";
    public static final String SHIPPER_TABLE = "Shippers";
    public static final String ORDER_NEED_SHIP_TABLE = "OrdersNeedShip";

    public static final int PIC_IMAGE_REQUEST = 71;

    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https://fcm.googleapis.com/";
    public static Order currentOrder;

    public static String convertCodeToStatus(int code) {
        switch (code) {
            case 0:
                return "Placed";
            case 1:
                return "Chấp nhận đơn hàng";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }

    public static IGeoCoordinates getGeoCodeServices() {
        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0, pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;


    }

    public static void showNotification(Context context, int notiId, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (pendingIntent != null)
            pendingIntent = PendingIntent.getActivity(context, notiId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "an_ngon";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "An Ngon Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("An Ngon Client App");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));

        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification mNotification = builder.build();
        notificationManager.notify(notiId, mNotification);
    }

    public static IFCMService getFCMService() {
        return com.trangdv.orderfoodserver.retrofit.RetrofitClient.getInstance("https://fcm.googleapis.com/").create(IFCMService.class);
    }

    public static String getTopicChannel(int restaurantId) {
        return new StringBuilder("Restaurant_").append(restaurantId).toString();
    }
}