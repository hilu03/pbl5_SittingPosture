package com.example.sit;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class test_server2 extends AppCompatActivity {
    private OkHttpClient client = new OkHttpClient();
    private Handler handler = new Handler();
    private Runnable postureCheckRunnable;
    private WebView webView;
    private TextView postureStatusTextView;
    private Button startButton;
    private ImageView helpImageView;
    private PopupWindow popupWindow;
    private Switch mySwitch;
    private MediaPlayer mediaPlayer;
    private boolean isSwitchOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_server2);

        mediaPlayer = MediaPlayer.create(this, R.raw.sound1);

        webView = findViewById(R.id.webView);
        postureStatusTextView = findViewById(R.id.postureStatusTextView);
        startButton = findViewById(R.id.startButton);
        mySwitch = findViewById(R.id.mySwitch);
        helpImageView = findViewById(R.id.helpImageView);

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSwitchOn = isChecked;
                if (!isChecked) {
                    stopAudio();
                }
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVideoFeed();
                startPostureCheck();
            }
        });

        helpImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
    }

    private void showPopupWindow(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(anchorView, 0, 0);

        TextView popupTextView = popupView.findViewById(R.id.popupTextView);
        popupTextView.setText("When the switch is turned on, you will receive an audible alert if you are sitting incorrectly.");
    }

    private void startVideoFeed() {
        webView.loadUrl("http://172.20.10.6:5000");
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
        Request request = new Request.Builder()
                .url("http://192.168.1.76:5000/check_posture")
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
}
