package com.rahuldharmkar.phonepeqrintegration;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;


public class MainActivity2 extends AppCompatActivity {

    private String apiUrl = "https://mercury-uat.phonepe.com/enterprise-sandbox/v3/qr/init";
    private String apiEndPoint = "/v3/qr/init";
    private String salt = "35d1ed5f-56bf-4b9c-a88f-5704875b2e83"; // salt key
    private ImageView imageView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Display QR Code in ImageView
        imageView = findViewById(R.id.imageView);

        // Call the function to initiate the transaction when the button is clicked
        Button button = findViewById(R.id.btnInitiateTransaction);
        button.setOnClickListener(v -> initiateTransaction());
    }


    /**
     * this function is called when we click button
     * to generate Dynamic QR string(transaction details)
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initiateTransaction() {
        try {
            // Construct the request parameters
            JSONObject params = new JSONObject();
            params.put("merchantId", "IVEPOSUAT");
            params.put("transactionId", System.currentTimeMillis());
            params.put("merchantOrderId", System.currentTimeMillis());
            params.put("amount", 100);
            params.put("expiresIn", 3600);
            params.put("storeId", "store1");
            params.put("terminalId", "terminal1");

            // Convert the parameters to JSON and encode to Base64
            String jsonString = params.toString();
            String base64EncodedString = Base64.getEncoder().encodeToString(jsonString.getBytes(StandardCharsets.UTF_8));

            // Generate X-VERIFY header
            String checksum = sha256(base64EncodedString + apiEndPoint + salt) + "###1";

            // Make a POST request to the API endpoint
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    String response = sendPostRequest(apiUrl, base64EncodedString, checksum);
                    Log.d("Response", response);
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                String qrString = jsonResponse.getJSONObject("data").getString("qrString");
                                // Generate QR code using the obtained QR string
                                generateAndDisplayQRCode(qrString);
                            } else {
                                // Handle unsuccessful response
                                Toast.makeText(MainActivity2.this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing error
                            Toast.makeText(MainActivity2.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle IO exception
                    runOnUiThread(() -> Toast.makeText(MainActivity2.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSON exception
        }
    }

    /**
     * Function to calculate SHA256 checksum
     */
    private String sha256(String base64Encoded) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(base64Encoded.getBytes(StandardCharsets.UTF_8));
            byte[] byteData = md.digest();
            StringBuilder checksum = new StringBuilder();
            for (byte b : byteData) {
                checksum.append(String.format("%02x", b));
            }
            return checksum.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * this function is for generating QR code
     */
    private void generateAndDisplayQRCode(String qrString) {
        // Generate QR code using the obtained QR string
        Bitmap qrCodeBitmap = generateQRCode(qrString);
        imageView.setImageBitmap(qrCodeBitmap);
    }

    private Bitmap generateQRCode(String qrString) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        int width = 512;
        int height = 512;
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(qrString, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Function to send a POST request
     */
    private String sendPostRequest(String apiUrl, String base64EncodedString, String checksum) throws IOException {
        URL url = new URL(apiUrl);
        Log.d("MainActivity2", "API URL: " + apiUrl);
        Log.d("MainActivity2", "Request Body: " + base64EncodedString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-verify", checksum);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = base64EncodedString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            scanner.useDelimiter("\\A");
            String response = scanner.hasNext() ? scanner.next() : "";
            // Log response
            Log.d("MainActivity2", "Response: " + response);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            // Handle IO exception
            Log.e("MainActivity2", "Error: " + e.getMessage());
            return "";
        } finally {
            // Ensure connection is disconnected after use
            conn.disconnect();
        }
    }

}
