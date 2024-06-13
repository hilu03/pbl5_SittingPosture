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

public class Login extends AppCompatActivity {

    EditText signupName, signupEmail, loginUsername, loginPassword;
    TextView loginRedirectText;
    Button loginButton;
    OkHttpClient client;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        signupName = findViewById(R.id.edt_name);
//        signupEmail = findViewById(R.id.edt_email);
        loginUsername = findViewById(R.id.edt_username);
        loginPassword = findViewById(R.id.edt_pass);
        loginButton = findViewById(R.id.btn_login);
        loginRedirectText = findViewById(R.id.signupRedirectText);

        client = new OkHttpClient();
        gson = new Gson();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() | !validatePassword()) {

                }
                else {
                    checkUser();
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            return false;
        }
        else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        }
        else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser() {
        try {
            String username = loginUsername.getText().toString();
            String password = loginPassword.getText().toString();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);
            jsonObject.put("password", hashPassword(password));

            String json = gson.toJson(jsonObject);

            // gửi file json lên cho server
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

            // singleton IP
            String ipAddress = IPAddressSingleton.getInstance().getIPAddress();
            String url = "http://" + ipAddress + ":5000/login";
            // Flask
            Request request = new Request.Builder()
//                    .url("http://172.20.10.2:5000/login")
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
                            Toast.makeText(Login.this, "Failed to login. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string(); // lấy từ flask file json
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);

                            String status = jsonResponse.getString("status");
                            String name = jsonResponse.getString("name");
                            String email = jsonResponse.getString("email");
                            if (status.equals("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Login.this, "Login successfully", Toast.LENGTH_SHORT).show();

                                        HelperClass singleton = HelperClass.getInstance();
                                        singleton.setUsername(username);
                                        singleton.setEmail(email);
                                        singleton.setPassword(password);
                                        singleton.setName(name);

                                        Intent intent = new Intent(Login.this, Home1.class);
                                        startActivity(intent);
                                    }
                                });
                            } else if (responseData.equals("wrong password")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Login.this, "Wrong password. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Login.this, "Server can not connect to database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Login.this, "Failed to login. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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