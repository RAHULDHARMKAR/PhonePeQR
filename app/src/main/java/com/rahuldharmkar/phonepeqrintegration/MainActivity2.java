package com.rahuldharmkar.phonepeqrintegration;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity {

    private String apiUrl = "https://mercury-t2.phonepe.com/v3/qr/init";
    private String apiEndPoint = "/v3/qr/init";
    private String salt = "fb9999a9-41d7-426d-954c-bc31764e1b1b"; // salt key
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initiateTransaction() {
        // Construct the request parameters
        Map<String, Object> params = new HashMap<>();
        params.put("merchantId", "IVEPOSDYNAMICQRUAT");
        params.put("transactionId", "IVEPOS");
        params.put("merchantOrderId", "IVEPOS");
        params.put("amount", 100L);
        params.put("expiresIn", 1800L);
        params.put("storeId", "teststore1");
        params.put("terminalId", "testterminal1");

        // Convert the parameters to JSON and encode to Base64
        JSONObject jsonObject = new JSONObject(params);
        String jsonString = jsonObject.toString();
        String base64EncodedString = Base64.encodeToString(jsonString.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        Log.d("MainActivity2", "Encoded Payload: " + base64EncodedString);

        // Generate X-VERIFY header
        String checksum = sha256(base64EncodedString + apiEndPoint + salt) + "###1";
        Log.d("MainActivity2", "Checksum: " + checksum);

        // Make a POST request to the API endpoint
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String response = sendPostRequest(apiUrl, base64EncodedString, checksum);
                Log.d("MainActivity2", "Response: " + response);
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Log.d("MainActivity2", "JSON Response: " + jsonResponse.toString());
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
                        Toast.makeText(MainActivity2.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                // Handle IO exception
                runOnUiThread(() -> Toast.makeText(MainActivity2.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

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

    private void generateAndDisplayQRCode(String qrString) {
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

    private String sendPostRequest(String apiUrl, String base64EncodedString, String checksum) throws IOException {
        URL url = new URL(apiUrl);
        Log.d("MainActivity2", "API URL: " + apiUrl);
        Log.d("MainActivity2", "Request Body: " + base64EncodedString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-VERIFY", checksum);
        conn.setDoOutput(true);

        // Wrap base64EncodedString in a JSON object
        String payload = "{\"request\":\"" + base64EncodedString + "\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        Log.d("MainActivity2", "Response Code: " + responseCode);

        if (responseCode == 200) {
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                Log.d("MainActivity2", "Response: " + response);
                return response;
            }
        } else {
            Log.e("MainActivity2", "Error Response Code: " + responseCode);
            try (Scanner scanner = new Scanner(conn.getErrorStream())) {
                scanner.useDelimiter("\\A");
                String errorResponse = scanner.hasNext() ? scanner.next() : "";
                Log.e("MainActivity2", "Error Response: " + errorResponse);
                return errorResponse;
            }
        }
    }
}



