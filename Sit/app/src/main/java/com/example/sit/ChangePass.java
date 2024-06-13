package com.example.sit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePass extends BaseActivity {
    EditText oldPass, newPass, confirmPass;
    Button changePass;
    OkHttpClient client;
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        oldPass = findViewById(R.id.edt_oldPass);
        newPass = findViewById(R.id.edt_newPass);
        confirmPass = findViewById(R.id.edt_confirmPass);

        changePass = findViewById(R.id.btn_change);

        client = new OkHttpClient();
        gson = new Gson();

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPass.getText().toString();
                String newPassword = newPass.getText().toString();
                String confirmedPassword = confirmPass.getText().toString();

                if (!newPassword.equals(confirmedPassword)) {
                    Toast.makeText(ChangePass.this, "New passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // đỏi mật khaaur
                changePassword(oldPassword, newPassword);
            }
        });
    }

    private void changePassword(String oldPassword, String newPassword) {
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("old_pass", hashPassword(oldPassword));
            requestData.put("new_pass", hashPassword(newPassword));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestData.toString());

        String ipAddress = IPAddressSingleton.getInstance().getIPAddress();
        String url = "http://" + ipAddress + ":5000/change_pass";
        Request request = new Request.Builder()
//                .url("http://172.20.10.2:5000/change_pass")
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChangePass.this, "Failedd to change password. Please try again.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    if (responseData.equals("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChangePass.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ChangePass.this, Login.class);
                                startActivity(intent);
                            }
                        });
                    } else if (responseData.equals("invalid_old_password")) {
                        runOnUiThread(() -> Toast.makeText(ChangePass.this, "Invalid old password. Please try again.", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChangePass.this, "Failedđ to change password. Please try again.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ChangePass.this, "Faileddd to change password. Please try again.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}