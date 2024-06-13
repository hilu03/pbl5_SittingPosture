package com.example.sit;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class test_server_webview extends BaseActivity {
    private OkHttpClient client = new OkHttpClient();
    private Handler handler = new Handler();
    private Runnable postureCheckRunnable;
    private WebView webView;
    private ImageView imgHelp;
    private PopupWindow popupWindow;
    private Switch mySwitch;
    private MediaPlayer mediaPlayer;
    private boolean isSwitchOn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_server_webview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // singleton IP
        String ipAddress = IPAddressSingleton.getInstance().getIPAddress();
        String webviewURL = "http://" + ipAddress + ":5000";

        imgHelp = findViewById(R.id.img_help);
        mediaPlayer = MediaPlayer.create(this, R.raw.sound2);
        mySwitch = findViewById(R.id.mySwitch);

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl("http://172.20.10.2:5000");
        webView.loadUrl(webviewURL);

        startPostureCheck();


        imgHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSwitchOn = isChecked;
                if (!isChecked) {
                    stopAudio();
                }
            }
        });
        Back();
    }

    private void showPopupWindow(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(anchorView, 0, 20);

        TextView popupTextView = popupView.findViewById(R.id.popupTextView);
        popupTextView.setText("When the switch is turned on, you will receive an audible alert if you are sitting incorrectly.");
    }

    private void startPostureCheck() {
        postureCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkPosture();
                handler.postDelayed(this, 1000); // Kiểm tra mỗi giây
            }
        };
        handler.post(postureCheckRunnable);
    }

    private void checkPosture() {
        String ipAddress = IPAddressSingleton.getInstance().getIPAddress();
        String url = "http://" + ipAddress + ":5000/check_posture";
        Request request = new Request.Builder()
//                .url("http://172.20.10.2:5000/check_posture")
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String status = jsonResponse.getString("status");
                        String posture = jsonResponse.getString("posture");
                        runOnUiThread(() -> {
//                            postureStatusTextView.setText(status.equals("wrong") ? "Incorrect posture: " + posture : "Correct posture");

                            if (isSwitchOn) {
                                if (status.equals("wrong")) {
                                    playAudio();
                                } else {
                                    stopAudio();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    Log.e("MainActivity", "Unexpected response: " + response);
                }
            }
        });
    }

    private void playAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(postureCheckRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    private void Back() {
        ConstraintLayout back = findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.stopLoading();
                Intent intent = new Intent(test_server_webview.this, Home1.class);
                startActivity(intent);
            }
        });
    }
}