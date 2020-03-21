package com.trangdv.orderfoodserver.model;

import java.util.List;

public class TokenModel {
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

    public List<RestaurantToken> getResult() {
        return result;
    }

    public void setResult(List<RestaurantToken> result) {
        this.result = result;
    }

    private List<RestaurantToken> result;
}
