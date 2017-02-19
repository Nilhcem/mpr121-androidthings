package com.nilhcem.androidthings.mpr121.driver;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;

import com.google.android.things.userdriver.InputDriver;
import com.google.android.things.userdriver.UserDriverManager;

import java.io.IOException;

public class Mpr121InputDriver implements AutoCloseable {

    private static final String TAG = Mpr121InputDriver.class.getSimpleName();
    private static final int SOFTWAREPOLL_DELAY_MS = 100;

    // Driver parameters
    private static final String DRIVER_NAME = "Mpr121";
    private static final int DRIVER_VERSION = 1;

    private Mpr121 peripheralDevice;

    // Framework input driver
    private InputDriver inputDriver;
    // Key codes mapped to input channels
    private int[] keycodes;

    private Handler inputHandler;
    private boolean[] inputStatus;

    /**
     * Callback invoked to poll the state of the controller
     * interrupt in software.
     */
    private final Runnable pollingCallback = new Runnable() {
        @Override
        public void run() {
            try {
                int data = peripheralDevice.getTouched();

                for (int i = 0; i < Mpr121.NB_ELECTRODES; i++) {
                    if ((data & (1 << i)) != 0) {
                        if (!inputStatus[i]) {
                            Log.d(TAG, "#" + i + " touched");
                            inputStatus[i] = true;
                            emitInputEvents(keycodes[i], KeyEvent.ACTION_DOWN);
                        }
                    } else {
                        if (inputStatus[i]) {
                            Log.d(TAG, "#" + i + " released");
                            inputStatus[i] = false;
                            emitInputEvents(keycodes[i], KeyEvent.ACTION_UP);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error getting data from peripheral device", e);
            } finally {
                inputHandler.postDelayed(this, SOFTWAREPOLL_DELAY_MS);
            }
        }
    };

    /**
     * Create a new Mpr121InputDriver to forward capacitive touch events
     * to the Android input framework.
     *
     * @param i2cName  I2C port name where the controller is attached. Cannot be null.
     * @param handler  optional {@link Handler} for software polling and callback events.
     * @param keyCodes {@link KeyEvent} codes to be emitted for each input channel.
     *                 Length must match the input channel count of the
     *                 touch controller.
     */
    public Mpr121InputDriver(String i2cName, Handler handler, int[] keyCodes) throws IOException {
        // Verify inputs
        if (keyCodes == null) {
            throw new IllegalArgumentException("Must provide a valid set of key codes.");
        }

        this.keycodes = keyCodes;
        this.peripheralDevice = new Mpr121(i2cName);

        this.inputHandler = new Handler(handler == null ? Looper.myLooper() : handler.getLooper());
        this.inputStatus = new boolean[Mpr121.NB_ELECTRODES];

        inputHandler.post(pollingCallback);
    }

    @Override
    public void close() throws IOException {
        unregister();

        inputHandler.removeCallbacks(pollingCallback);

        if (peripheralDevice != null) {
            try {
                peripheralDevice.close();
            } finally {
                peripheralDevice = null;
            }
        }
    }

    /**
     * Register this driver with the Android input framework.
     */
    public void register() {
        if (inputDriver == null) {
            UserDriverManager manager = UserDriverManager.getManager();
            inputDriver = InputDriver.builder(InputDevice.SOURCE_CLASS_BUTTON)
                    .setName(DRIVER_NAME)
                    .setVersion(DRIVER_VERSION)
                    .setKeys(keycodes)
                    .build();
            manager.registerInputDriver(inputDriver);
        }
    }

    /**
     * Unregister this driver with the Android input framework.
     */
    public void unregister() {
        if (inputDriver != null) {
            UserDriverManager manager = UserDriverManager.getManager();
            manager.unregisterInputDriver(inputDriver);
            inputDriver = null;
        }
    }

    /**
     * Emit input events through the registered driver to the
     * Android input framework using the defined set of key codes.
     */
    private void emitInputEvents(int keyCode, int keyAction) {
        if (inputDriver == null) {
            Log.w(TAG, "Driver not yet registered");
            return;
        }
        inputDriver.emit(new KeyEvent[]{new KeyEvent(keyAction, keyCode)});
    }
}
