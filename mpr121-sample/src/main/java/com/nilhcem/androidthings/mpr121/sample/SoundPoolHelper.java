package com.nilhcem.androidthings.mpr121.sample;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundPoolHelper {

    private SoundPool soundPool;
    private boolean loaded;
    private int[] sounds = new int[12];

    public void init(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        // load R.raw.sound[0->11]
        for (int i = 0; i < 12; i++) {
            sounds[i] = soundPool.load(context, context.getResources().getIdentifier("sound" + i, "raw", context.getPackageName()), 1);
        }
    }

    public void play(int id) {
        if (loaded) {
            soundPool.play(sounds[id], 0.5f, 0.5f, 1, 0, 1f);
        }
    }

    public void close() {
        soundPool.release();
        soundPool = null;
    }
}
