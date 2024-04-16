package com.rahuldharmkar.phonepeqrintegration.java_data;

import com.google.gson.annotations.SerializedName;

public class QRInitRequest {
    @SerializedName("merchantId")
    private String merchantId;

    @SerializedName("transactionId")
    private String transactionId;

    @SerializedName("amount")
    private long amount;

    // Add other required parameters here

    public QRInitRequest(String merchantId, String transactionId, long amount) {
        this.merchantId = merchantId;
        this.transactionId = transactionId;
        this.amount = amount;
        // Initialize other parameters as needed
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }


}
