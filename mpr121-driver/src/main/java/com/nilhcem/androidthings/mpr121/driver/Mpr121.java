package com.nilhcem.androidthings.mpr121.driver;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Android Things port of Adafruit MPR121
 *
 * @see "https://github.com/adafruit/Adafruit_MPR121"
 */
public class Mpr121 implements AutoCloseable {

    public static final int NB_ELECTRODES = 12;

    private static final int I2C_ADDRESS = 0x5A;

    private static final int TOUCHSTATUS_L = 0x00;
    private static final int MHDR = 0x2B;
    private static final int NHDR = 0x2C;
    private static final int NCLR = 0x2D;
    private static final int FDLR = 0x2E;
    private static final int MHDF = 0x2F;
    private static final int NHDF = 0x30;
    private static final int NCLF = 0x31;
    private static final int FDLF = 0x32;
    private static final int NHDT = 0x33;
    private static final int NCLT = 0x34;
    private static final int FDLT = 0x35;
    private static final int TOUCHTH_0 = 0x41;
    private static final int RELEASETH_0 = 0x42;
    private static final int DEBOUNCE = 0x5B;
    private static final int CONFIG1 = 0x5C;
    private static final int CONFIG2 = 0x5D;
    private static final int ECR = 0x5E;
    private static final int SOFTRESET = 0x80;

    private I2cDevice device;

    public Mpr121(String i2cName) throws IOException {
        PeripheralManagerService manager = new PeripheralManagerService();
        device = manager.openI2cDevice(i2cName, I2C_ADDRESS);
        init();
    }

    public int getTouched() throws IOException {
        return device.readRegWord(TOUCHSTATUS_L) & 0x0FFF;
    }

    private void init() throws IOException {
        // Soft reset
        device.writeRegByte(SOFTRESET, (byte) 0x63);

        device.writeRegByte(ECR, (byte) 0x00);

        setThresholds((byte) 12, (byte) 6);
        device.writeRegByte(MHDR, (byte) 0x01);
        device.writeRegByte(NHDR, (byte) 0x01);
        device.writeRegByte(NCLR, (byte) 0x0E);
        device.writeRegByte(FDLR, (byte) 0x00);

        device.writeRegByte(MHDF, (byte) 0x01);
        device.writeRegByte(NHDF, (byte) 0x05);
        device.writeRegByte(NCLF, (byte) 0x01);
        device.writeRegByte(FDLF, (byte) 0x00);

        device.writeRegByte(NHDT, (byte) 0x00);
        device.writeRegByte(NCLT, (byte) 0x00);
        device.writeRegByte(FDLT, (byte) 0x00);

        device.writeRegByte(DEBOUNCE, (byte) 0x00);
        device.writeRegByte(CONFIG1, (byte) 0x10); // default, 16uA charge current
        device.writeRegByte(CONFIG2, (byte) 0x20); // 0.5uS encoding, 1ms period

        // enable all electrodes
        device.writeRegByte(ECR, (byte) 0x8F); // start with first 5 bits of baseline tracking
    }

    private void setThresholds(byte touch, byte release) throws IOException {
        for (int i = 0; i < NB_ELECTRODES; i++) {
            device.writeRegByte(TOUCHTH_0 + 2 * i, touch);
            device.writeRegByte(RELEASETH_0 + 2 * i, release);
        }
    }

    @Override
    public void close() throws IOException {
        if (device != null) {
            try {
                device.close();
            } finally {
                device = null;
            }
        }
    }
}
