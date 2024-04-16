package com.rahuldharmkar.phonepeqrintegration.java_data;

import com.google.gson.annotations.SerializedName;

public class QRInitResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private QRData data;

    public QRInitResponse(boolean success, String code, String message, QRData data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public QRData getData() {
        return data;
    }

    public void setData(QRData data) {
        this.data = data;
    }
}