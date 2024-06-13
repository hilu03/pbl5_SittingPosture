package com.example.sit;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class base64 extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    boolean isLooping = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base64);

        Switch mySwitch = findViewById(R.id.mySwitch);
        TextView myText = findViewById(R.id.myText);

        // Khởi tạo MediaPlayer với file âm thanh từ res/raw
        mediaPlayer = MediaPlayer.create(this, R.raw.sound1);

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    myText.setText("Hello");
                    // Phát âm thanh khi bật Switch
                    playAudio();
                } else {
                    myText.setText("Xin chào");
                    // Dừng phát âm thanh khi tắt Switch
                    stopAudio();
                }
            }
        });

        // Thiết lập sự kiện lặp lại âm thanh khi kết thúc
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isLooping) {
                    mp.seekTo(0); // Quay lại vị trí ban đầu
                    mp.start(); // Phát lại âm thanh
                }
            }
        });
    }

    private void playAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isLooping = true; // Cho phép lặp lại âm thanh khi kết thúc
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isLooping = false; // Không lặp lại âm thanh khi dừng
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng tài nguyên MediaPlayer khi hoạt động kết thúc
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}