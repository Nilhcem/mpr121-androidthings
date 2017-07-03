# MPR121 capacitive touch sensor driver for Android Things

A port of the [Adafruit MPR121][adafruit-mpr121] Arduino library for Android Things.


## Download

```groovy
dependencies {
    compile 'com.nilhcem.androidthings:driver-mpr121:0.0.1'
}
```

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
boolean[] inputStatus = new boolean[Mpr121.NB_ELECTRODES];
for (int i = 0; i < Mpr121.NB_ELECTRODES; i++) {
    if ((data & (1 << i)) != 0) {
        if (!inputStatus[i]) {
            Log.d(TAG, "#" + i + " touched");
            inputStatus[i] = true;
        }
    } else {
        if (inputStatus[i]) {
            Log.d(TAG, "#" + i + " released");
            inputStatus[i] = false;
        }
    }
}

// Finally, close the driver
peripheralDevice.close();
```

## Sample project

The sample project uses a passive buzzer to play a sound when an electrode is touched.

### Schematic:

![schematic][]

If you prefer playing a real sound file instead (e.g.: wav/mp3), use the `SoundPoolHelper` instead of the `PassiveBuzzerHelper` in the `MainActivity`, and place your assets in `res/raw` (from: `res/raw/sound0.mp3` to: `res/raw/sound11.mp3`)

[adafruit-mpr121]: https://github.com/adafruit/Adafruit_MPR121/
[schematic]: https://raw.githubusercontent.com/Nilhcem/mpr121-androidthings/master/assets/schematic.png
