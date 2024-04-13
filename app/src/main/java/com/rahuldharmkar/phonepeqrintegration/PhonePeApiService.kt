package com.rahuldharmkar.phonepeqrintegration

import com.rahuldharmkar.phonepeqrintegration.data.QRInitRequest
import com.rahuldharmkar.phonepeqrintegration.data.QRInitResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PhonePeApiService {
    @POST("/v3/qr/init")
    fun initQRCode(@Body request: QRInitRequest): Call<QRInitResponse>
}