package com.rahuldharmkar.phonepeqrintegration


import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.*


class MainActivity : AppCompatActivity() {

    private val apiUrl = "https://mercury-uat.phonepe.com/enterprise-sandbox/v3/qr/init"
    var apiEndPoint = "/v3/qr/init"
    val salt = "" // salt key


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Display QR Code in ImageView
        val imageView = findViewById<ImageView>(R.id.imageView)

        // Call the function to initiate the transaction when the button is clicked
        findViewById<Button>(R.id.btnInitiateTransaction).setOnClickListener {
            initiateTransaction()

            val qrString =
                "upi://pay?pa=IVEPOSUAT&pn=IVEPOS&am=10.00&tr=TX12345678901331424256"
            // Generate QR Code
            val qrCodeBitmap = generateQRCode(qrString)
            imageView.setImageBitmap(qrCodeBitmap)
        }
    }


    /**
     * this function is called when we click button
     * to generate Dynamic QR string(transaction details)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initiateTransaction() {

        // Construct the request parameters
        val params = mapOf(
            "merchantId" to "",
            "transactionId" to "TX12345678901331",
            "merchantOrderId" to "M1234567013d34444",
            "amount" to 100,
            "expiresIn" to 1800,
            "storeId" to "store1",
            "terminalId" to "terminal1"
        )

        // Convert the parameters to JSON and encode to Base64
        val jsonString = params.toString()
        val encodedBytes: ByteArray = Base64.getEncoder().encode(jsonString.toByteArray())
        val base64EncodedString = String(encodedBytes)


        // Generate X-VERIFY header
        val checksum = sha256(base64EncodedString + apiEndPoint + salt) + "###1";


        // Make a POST request to the API endpoint
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = sendPostRequest(apiUrl, base64EncodedString, checksum)
                // Log the API response

                withContext(Dispatchers.Main) {
//                    val jsonResponse = JSONObject(response)
//                    if (jsonResponse.getBoolean("success")) {
//                        val qrString = jsonResponse.getJSONObject("data").getString("qrString")
//                        // Generate QR code using the obtained QR string
//                        generateQRCode(qrString)
//                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Handle the error here

                }
            }
        }
    }


    /**
     * Function to calculate SHA256 checksum
     */
    private fun sha256(base64Encoded: String): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        md.update(base64Encoded.toByteArray())
        val byteData: ByteArray = md.digest()
        val checksum = StringBuilder()
        for (b in byteData) {
            checksum.append(String.format("%02x", b))
        }
        return checksum.toString()
    }


    /**
     * this function is for generating QR code
     */
    private fun generateQRCode(qrString: String): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(qrString, BarcodeFormat.QR_CODE, 512, 512)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }


    /**
     * Function to send a POST request
     */
    @Throws(IOException::class)
    private fun sendPostRequest(
        apiUrl: String,
        base64EncodedString: String,
        checksum: String
    ): String {
        val url = URL(apiUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("X-verify", checksum)
        conn.doOutput = true

        val os: OutputStream = conn.outputStream
        val input: ByteArray = base64EncodedString.toByteArray(charset("utf-8"))
        os.write(input, 0, input.size)

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val `is`: InputStream = conn.inputStream
            val buffer = ByteArray(1024)
            val response = StringBuilder()
            var bytesRead: Int
            while (`is`.read(buffer).also { bytesRead = it } != -1) {
                response.append(String(buffer, 0, bytesRead))
            }
            return response.toString()
        } else {
            return "Error: $responseCode"
        }
    }


}
