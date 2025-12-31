package com.example.test;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IDCardActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private Button btnCapture;
    private TextView tvResult;
    private boolean isProcessing = false;

    private static final String SECRET_ID = "";
    private static final String SECRET_KEY = "";
    private static final String ENDPOINT = "ocr.tencentcloudapi.com";
    private static final String SERVICE = "ocr";
    private static final String REGION = "ap-guangzhou";
    private static final String ACTION = "IDCardOCR";
    private static final String VERSION = "2018-11-19";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard);

        btnCapture = findViewById(R.id.btnCapture);
        tvResult = findViewById(R.id.tvResult);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProcessing) {
                    Toast.makeText(IDCardActivity.this, "正在处理中", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkCameraPermission();
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 启动相机拍照
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "无法打开相机", Toast.LENGTH_SHORT).show();
        }
    }

    // 处理拍照结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    isProcessing = true;
                    btnCapture.setEnabled(false);
                    btnCapture.setText("识别中...");
                    tvResult.setText("正在识别身份证信息...");

                    new OCRTask().execute(imageBitmap);
                }
            }
        }
    }

    private class OCRTask extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            try {
                Bitmap imageBitmap = bitmaps[0];

                imageBitmap = compressBitmap(imageBitmap, 1024, 1024);

                String base64Image = bitmapToBase64(imageBitmap);
                Log.d("OCRTask", "Base64图片大小: " + (base64Image.length() / 1024) + "KB");
                JSONObject requestJson = new JSONObject();
                requestJson.put("ImageBase64", base64Image);

                String requestBody = requestJson.toString();
                Log.d("OCRTask", "请求体长度: " + requestBody.length());

                //计算V3签名
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

                //规范请求串
                String httpRequestMethod = "POST";
                String canonicalUri = "/";
                String canonicalQueryString = "";
                String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                        + "host:" + ENDPOINT + "\n";
                String signedHeaders = "content-type;host";

                String payloadHash = sha256Hex(requestBody);
                String canonicalRequest = httpRequestMethod + "\n"
                        + canonicalUri + "\n"
                        + canonicalQueryString + "\n"
                        + canonicalHeaders + "\n"
                        + signedHeaders + "\n"
                        + payloadHash;

                Log.d("OCRTask", "CanonicalRequest:\n" + canonicalRequest);

                //待签名字符串
                String algorithm = "TC3-HMAC-SHA256";
                String credentialScope = date + "/" + SERVICE + "/tc3_request";
                String hashedCanonicalRequest = sha256Hex(canonicalRequest);
                String stringToSign = algorithm + "\n"
                        + timestamp + "\n"
                        + credentialScope + "\n"
                        + hashedCanonicalRequest;

                Log.d("OCRTask", "StringToSign:\n" + stringToSign);

                //计算签名
                byte[] secretDate = hmacSha256(("TC3" + SECRET_KEY).getBytes(StandardCharsets.UTF_8), date);
                byte[] secretService = hmacSha256(secretDate, SERVICE);
                byte[] secretSigning = hmacSha256(secretService, "tc3_request");
                byte[] signatureBytes = hmacSha256(secretSigning, stringToSign);
                String signature = bytesToHex(signatureBytes).toLowerCase();

                Log.d("OCRTask", "Signature: " + signature);

                //拼接Authorization头
                String authorization = algorithm + " "
                        + "Credential=" + SECRET_ID + "/" + credentialScope + ", "
                        + "SignedHeaders=" + signedHeaders + ", "
                        + "Signature=" + signature;

                Log.d("OCRTask", "Authorization: " + authorization.substring(0, Math.min(100, authorization.length())) + "...");

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(mediaType, requestBody);

                Request request = new Request.Builder()
                        .url("https://" + ENDPOINT)
                        .post(body)
                        .addHeader("Authorization", authorization)
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Host", ENDPOINT)
                        .addHeader("X-TC-Action", ACTION)
                        .addHeader("X-TC-Timestamp", timestamp)
                        .addHeader("X-TC-Version", VERSION)
                        .addHeader("X-TC-Region", REGION)
                        .build();

                Response response = client.newCall(request).execute();

                int responseCode = response.code();
                String responseBody = response.body().string();


                if (response.isSuccessful()) {
                    return responseBody;
                } else {
                    return "{\"Error\":{\"Code\":\"HTTP_" + responseCode + "\",\"Message\":\"HTTP请求失败\"}}";
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("OCRTask", "识别失败: " + e.getMessage());
                return "{\"Error\":{\"Code\":\"Exception\",\"Message\":\"" + e.getMessage() + "\"}}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            isProcessing = false;
            btnCapture.setEnabled(true);
            btnCapture.setText("拍摄身份证");

            try {
                JSONObject jsonResult = new JSONObject(result);

                if (jsonResult.has("Response")) {
                    JSONObject response = jsonResult.getJSONObject("Response");

                    if (response.has("Error")) {
                        JSONObject error = response.getJSONObject("Error");
                        String errorCode = error.getString("Code");
                        String errorMsg = error.getString("Message");

                        handleError(errorCode, errorMsg);
                        return;
                    }

                    StringBuilder displayText = new StringBuilder();
                    displayText.append("身份证识别成功\n\n");

                    if (response.has("Name")) {
                        displayText.append("姓名: ").append(response.getString("Name")).append("\n");
                    }
                    if (response.has("Sex")) {
                        displayText.append("性别: ").append(response.getString("Sex")).append("\n");
                    }
                    if (response.has("Nation")) {
                        displayText.append("民族: ").append(response.getString("Nation")).append("\n");
                    }
                    if (response.has("Birth")) {
                        displayText.append("出生日期: ").append(response.getString("Birth")).append("\n");
                    }
                    if (response.has("Address")) {
                        displayText.append("地址: ").append(response.getString("Address")).append("\n");
                    }
                    if (response.has("IdNum")) {
                        displayText.append("身份证号: ").append(response.getString("IdNum")).append("\n");
                    }


                    tvResult.setText(displayText.toString());

                } else if (jsonResult.has("Error")) {
                    JSONObject error = jsonResult.getJSONObject("Error");
                    String errorCode = error.getString("Code");
                    String errorMsg = error.getString("Message");

                    handleError(errorCode, errorMsg);

                } else {
                    tvResult.setText("未知响应格式:\n" + result);
                }

            } catch (Exception e) {
                e.printStackTrace();
                tvResult.setText("解析响应失败: " + e.getMessage() + "\n\n原始响应:\n" + result);
            }
        }

        private void handleError(String errorCode, String errorMsg) {
            StringBuilder errorText = new StringBuilder();
            errorText.append("识别失败\n\n");
            errorText.append("错误代码: ").append(errorCode).append("\n");
            errorText.append("错误信息: ").append(errorMsg).append("\n\n");
            tvResult.setText(errorText.toString());

        }
    }



    // 压缩Bitmap
    private Bitmap compressBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    //Bitmap转Base64
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 压缩图片质量，降低文件大小
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    //计算SHA256
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //计算HMAC-SHA256
    private byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //字节数组转十六进制字符串
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}