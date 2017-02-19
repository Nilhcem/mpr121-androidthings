package com.nilhcem.androidthings.mpr121.sample;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;

import com.nilhcem.androidthings.mpr121.driver.Mpr121InputDriver;

import java.io.IOException;

public class Mpr121Helper {

    public interface OnTouchListener {
        void onSensorTouched(int id);

        void onSensorReleased(int id);
    }

    private static final String TAG = Mpr121Helper.class.getSimpleName();

    private static final int[] KEY_CODES = {
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3,
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7,
            KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_B
    };

    private final HandlerThread handlerThread = new HandlerThread("Mpr121Thread");
    private Handler handler;
    private Mpr121InputDriver inputDriver;
    private OnTouchListener listener;

    public void init(String i2cPin, final OnTouchListener listener) {
        this.listener = listener;

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        try {
            inputDriver = new Mpr121InputDriver(i2cPin, handler, KEY_CODES);
            inputDriver.register();
        } catch (IOException e) {
            Log.e(TAG, "Unable to initialize Mpr121 driver", e);
        }
    }

    public boolean onKeyDown(int keyCode) {
        int id = getIdForKeyCode(keyCode);
        if (id != -1) {
            Log.d(TAG, "onSensorTouched: " + id);
            listener.onSensorTouched(id);
            return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode) {
        int id = getIdForKeyCode(keyCode);
        if (id != -1) {
            Log.d(TAG, "onSensorReleased: " + id);
            listener.onSensorReleased(id);
            return true;
        }
        return false;
    }

    public void close() {
        try {
            handlerThread.quitSafely();

            if (inputDriver != null) {
                inputDriver.unregister();
                inputDriver.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to close Mpr121 driver", e);
        } finally {
            handler = null;
        }
    }

    private int getIdForKeyCode(int keyCode) {
        for (int i = 0; i < KEY_CODES.length; i++) {
            if (keyCode == KEY_CODES[i]) {
                return i;
            }
        }
        return -1;
    }
}
