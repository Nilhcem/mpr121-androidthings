# MPR121 driver for Android Things

Port of the [Adafruit MPR121](https://github.com/adafruit/Adafruit_MPR121/) or Android Things.

## Usage

### Registering with the system

```java
int[] keyCodes = new int[] { KevEvent.KEYCODE_1, KevEvent.KEYCODE_2, ... KevEvent.KEYCODE_12 };

Mpr121InputDriver inputDriver;

HandlerThread handlerThread = new HandlerThread("Mpr121Thread");
handlerThread.start();
handler = new Handler(handlerThread.getLooper());

try {
    inputDriver = new Mpr121InputDriver(i2cBusName, handler, keyCodes);
    mInputDriver.register();
} catch (IOException e) {
    // couldn't configure the input driver...
}

// Override key event callbacks in your Activity:

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
        case KeyEvent.KEYCODE_1:
            doSomethingAwesome();
            return true; // handle keypress
        // other cases...
    }
    return super.onKeyDown(keyCode, event);
}

// Unregister and close the input driver when finished:

mInputDriver.unregister;
try {
    mInputDriver.close();
} catch (IOException e) {
    // error closing input driver
}
```

### Calling the driver manually

You can call the `Mpr121` class directly if you prefer not to receive events from the system:

```java
// Instantiate the driver
Mpr121 peripheralDevice = new Mpr121(i2cName);

// Get the current state of each electrode
int data = peripheralDevice.getTouched();

// Loop to check the state of these electrodes
// Ideally, place this code in a thread to check continuously and add a listener when a state changes
for (int i = 0; i < Mpr121.NB_ELECTRODES; i++) {
    if ((data & (1 << i)) != 0) {
        Log.d(TAG, "#" + i + " touched");
    } else {
        Log.d(TAG, "#" + i + " released");
    }
}

// Finally, close the driver
peripheralDevice.close();
```
