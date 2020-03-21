package com.trangdv.orderfoodserver.model;

import androidx.annotation.NonNull;

public class Status {
    private int id;
    private String description;

    public Status(int i, String placed) {
        this.id = i;
        this.description = placed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return description;
    }
}
