package com.rahuldharmkar.phonepeqrintegration.java_data;

import com.google.gson.annotations.SerializedName;

public class QRData {
    @SerializedName("qrString")
    private String qrString;

    public QRData(String qrString) {
        this.qrString = qrString;
    }

    public String getQrString() {
        return qrString;
    }

    public void setQrString(String qrString) {
        this.qrString = qrString;
    }
}