package com.trangdv.orderfoodserver.model;

public class MenuFoodModel {
    private boolean success;
    private String message;
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
