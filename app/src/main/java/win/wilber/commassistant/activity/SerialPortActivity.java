package win.wilber.commassistant.activity;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import win.wilber.commassistant.R;
import win.wilber.commassistant.serialport.SerialPort;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidParameterException;

public abstract class SerialPortActivity extends Activity {
    protected Application mApplication;
    private InputStream mInputStream;
    protected OutputStream mOutputStream;
    private ReadThread mReadThread;
    protected SerialPort mSerialPort;

    private BufferedReader mBufferedReader;

    private class ReadThread extends Thread {
        private ReadThread() {
        }

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                try {
//                    byte[] buffer = new byte[3072];
//                    if (mInputStream != null) {
//
//                        int size = mInputStream.read(buffer);
//                        if (size > 0) {
//                            onDataReceived(buffer, size);
//                        }
//                    } else {
//                        return;
//                    }

                    if(null != mBufferedReader) {
                        String line = "";
                        line = mBufferedReader.readLine();
                        if(null != line) {
                            onDataReceived(line.getBytes(),line.getBytes().length);
                        }
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    protected abstract void onDataReceived(byte[] bArr, int i);

    private void DisplayError(int resourceId) {
        Builder b = new Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        b.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (Application) getApplication();
    }

    protected void openDevice() {
        try {
            mSerialPort = mApplication.getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
            mReadThread = new ReadThread();
            mReadThread.start();
            Application.OpenFlag = Boolean.valueOf(true);
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
            Application.OpenFlag = Boolean.valueOf(false);
        } catch (IOException e2) {
            DisplayError(R.string.error_unknown);
            Application.OpenFlag = Boolean.valueOf(false);
        } catch (InvalidParameterException e3) {
            DisplayError(R.string.error_configuration);
            Application.OpenFlag = Boolean.valueOf(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        mApplication.closeSerialPort();
        mSerialPort = null;
        super.onDestroy();
    }
}
