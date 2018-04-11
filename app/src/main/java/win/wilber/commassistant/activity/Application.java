package win.wilber.commassistant.activity;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import win.wilber.commassistant.serialport.SerialPort;
import win.wilber.commassistant.serialport.SerialPortFinder;

public class Application extends android.app.Application {
    public static Boolean OpenFlag;
    private SerialPort mSerialPort = null;
    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            int data_bits;
            int check_bit;
            int stop_bit;
            SharedPreferences sp = getSharedPreferences(getPackageName() + "_preferences", 0);
            String path = sp.getString("DEVICE", "");
            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1")).intValue();
            String databits = sp.getString("DATABITS", "5");
            if (databits.equals("5")) {
                data_bits = 0;
            } else if (databits.equals("6")) {
                data_bits = 1;
            } else if (databits.equals("7")) {
                data_bits = 2;
            } else if (databits.equals("8")) {
                data_bits = 3;
            } else {
                data_bits = -1;
            }
            Log.i("setting", "data_bits=" + String.valueOf(data_bits));
            String checkbit = sp.getString("CHECKBIT", "None");
            if (checkbit.equals("None")) {
                check_bit = 0;
            } else if (checkbit.equals("Odd")) {
                check_bit = 1;
            } else if (checkbit.equals("Even")) {
                check_bit = 2;
            } else if (checkbit.equals("Space")) {
                check_bit = 3;
            } else {
                check_bit = -1;
            }
            Log.i("setting", "check_bit=" + String.valueOf(check_bit));
            String stopbit = sp.getString("STOPBIT", "1");
            if (stopbit.equals("1")) {
                stop_bit = 0;
            } else if (stopbit.equals("2")) {
                stop_bit = 1;
            } else {
                stop_bit = -1;
            }
            Log.i("setting", "stop_bit=" + String.valueOf(stop_bit));
            if (path.length() == 0 || baudrate == -1 /*|| data_bits == -1 || check_bit == -1 || stop_bit == -1*/) {
                Log.i("paramter", "path=" + path);
                Log.i("paramter", "baudrate=" + String.valueOf(baudrate));
                Log.i("paramter", "data_bits=" + String.valueOf(data_bits));
                Log.i("paramter", "check_bit=" + String.valueOf(check_bit));
                Log.i("paramter", "stop_bit=" + String.valueOf(stop_bit));
                throw new InvalidParameterException();
            }
//            mSerialPort = new SerialPort(new File(path), baudrate, data_bits, check_bit, stop_bit, 0);
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}
