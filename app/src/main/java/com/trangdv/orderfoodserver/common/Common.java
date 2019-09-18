package com.trangdv.orderfoodserver.common;

import com.trangdv.orderfoodserver.model.Request;
import com.trangdv.orderfoodserver.model.User;

public class Common {
    public static User currentUser;
    public static Request currentRequest;
    public static final String DELETE = "Delete";
    public static final String UPDATE="Update";

    public static final int PIC_IMAGE_REQUEST = 71;

    public static String convertCodeToStatus(String status) {
        if (status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On My Way";
        else return "Shipped";
    }
}