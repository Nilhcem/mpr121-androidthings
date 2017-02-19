package com.nilhcem.androidthings.mpr121.sample;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmspeaker.Speaker;

import java.io.IOException;

public class PassiveBuzzerHelper {

    private static final String TAG = PassiveBuzzerHelper.class.getSimpleName();
    private static final int HANDLER_MSG_STOP = 0;
    private static final int HANDLER_MSG_PLAY = 1;

    private final HandlerThread handlerThread = new HandlerThread("BuzzerThread");
    private Handler handler;

    private Speaker buzzer;

    public void init(String gpioPin) throws IOException {
        buzzer = new Speaker(gpioPin);
        buzzer.stop(); // in case the PWM pin was enabled already

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    buzzer.stop();
                    if (msg.what == HANDLER_MSG_PLAY) {
                        buzzer.play(msg.arg1);
                        handler.sendEmptyMessageDelayed(HANDLER_MSG_STOP, 800);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Buzzer error", e);
                }
            }
        };
    }

    public void play(int id) {
        switch (id) {
            case 0:
                playNote(880);
                break;
            case 1:
                playNote(988);
                break;
            case 2:
                playNote(1047);
                break;
            case 3:
                playNote(1175);
                break;
            case 4:
                playNote(1319);
                break;
            case 5:
                playNote(1397);
                break;
            case 6:
                playNote(1568);
                break;
            case 7:
                playNote(1760);
                break;
            case 8:
                playNote(1976);
                break;
            case 9:
                playNote(2093);
                break;
            case 10:
                playNote(2349);
                break;
            case 11:
                playNote(2637);
                break;
            default:
                playNote(0);
                break;
        }
    }

    public void close() throws IOException {
        try {
            handler.removeMessages(HANDLER_MSG_STOP);
            handler.removeMessages(HANDLER_MSG_PLAY);
            handlerThread.quitSafely();
            buzzer.stop();
            buzzer.close();
        } finally {
            buzzer = null;
            handler = null;
        }
    }

    private void playNote(int frequency) {
        handler.removeMessages(HANDLER_MSG_STOP);

        Message msg = new Message();
        msg.what = frequency == 0 ? HANDLER_MSG_STOP : HANDLER_MSG_PLAY;
        msg.arg1 = frequency;
        handler.sendMessage(msg);
    }
}
