package com.trangdv.orderfoodserver.model;

import java.util.List;

public class ShipperModel {
    private boolean success;
    private String message;
    private List<Shipper> result;

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

    public List<Shipper> getResult() {
        return result;
    }

    public void setResult(List<Shipper> result) {
        this.result = result;
    }
}
