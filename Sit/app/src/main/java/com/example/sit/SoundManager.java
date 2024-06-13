package com.example.sit;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundManager {
    private static MediaPlayer mediaPlayer;

    public static void playSound(Context context, int soundResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(context, soundResId);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
            }
        });
    }
}