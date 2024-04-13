package com.rahuldharmkar.phonepeqrintegration.data

import com.google.gson.annotations.SerializedName

data class QRInitRequest(
    @SerializedName("merchantId") val merchantId: String,
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("amount") val amount: Long,
    // Add other required parameters here
)
