package com.example.sit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.google.gson.Gson;

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

public class SignUp extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    OkHttpClient client;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signupName = findViewById(R.id.edt_name);
        signupEmail = findViewById(R.id.edt_email);
        signupUsername = findViewById(R.id.edt_username);
        signupPassword = findViewById(R.id.edt_pass);
        signupButton = findViewById(R.id.btn_signup);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        client = new OkHttpClient();
        gson = new Gson();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = signupName.getText().toString();
                String email = signupEmail.getText().toString();
                String username = signupUsername.getText().toString();
                String password = signupPassword.getText().toString();

                HelperClass helperClass = new HelperClass(name, email, username, hashPassword(password));

                String json = gson.toJson(helperClass);

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

                // singleton IP
                String ipAddress = IPAddressSingleton.getInstance().getIPAddress();
                String url = "http://" + ipAddress + ":5000/sign_up";
                // Flask
                Request request = new Request.Builder()
//                        .url("http://172.20.10.2:5000/sign_up")
                        .url(url)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignUp.this, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string(); // lấy nội dung từ flask
                            if (responseData.equals("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SignUp.this, "You have signed up successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUp.this, Login.class);
                                        startActivity(intent);
                                    }
                                });
                            }
                            else if (responseData.equals("failed")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SignUp.this, "The email or username already exists. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SignUp.this, "Server can not connect to database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignUp.this, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
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