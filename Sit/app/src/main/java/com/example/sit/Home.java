package com.example.sit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Play();
        Statistic();
        Home1();
    }


    private void Play() {
        Button power = findViewById(R.id.button_power);
        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                power.setVisibility(View.GONE);
//
//                WebView webView = findViewById(R.id.webview);
//                webView.setVisibility(View.VISIBLE);
//
//                webView.getSettings().setJavaScriptEnabled(true);
//                webView.setWebViewClient(new WebViewClient());
//                webView.loadUrl("http://192.168.100.74:5000");
                Intent intent = new Intent(Home.this, test_server_webview.class);
                startActivity(intent);
            }
        });
    }
    private void Statistic() {
        LinearLayout btnStatistic = findViewById(R.id.btnStatistic);
        btnStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Statistic.class);
                startActivity(intent);
            }
        });
    }

    private void Home1() {
        LinearLayout btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Home1.class);
                startActivity(intent);
            }
        });
    }
    // webview
    private void buttonPower() {
        Button buttonPower = findViewById(R.id.button_power);
        buttonPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}