package win.wilber.commassistant.activity;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import win.wilber.commassistant.R;
import win.wilber.commassistant.util.Cmd;

public class MainActivity extends SerialPortActivity implements View.OnClickListener, OnCheckedChangeListener {
    private final int MAX_LENGTH = 65536;
    private byte[] OriginalReceive = new byte[65536];
    private int ReceiveCount = 0;
    private int SendCount;
    private CheckBox auto_send;
    private Button clear;
    private Button connect;
    private ToggleButton hex;
    private int interval;
    private boolean isHex;
    private TextView mReceiveCount;
//    private EditText mReception;
    private TextView mReception;
    private EditText mSend;
    private TextView mSendCount;
    private Button send;
    private EditText send_interval;
    private Button setting;
    private Timer timer = null;
    private ScrollView mScrollView;
    private ToggleButton mDataAtMode;
    private boolean mIsAtMode = true;

    @Override
    public void onBackPressed() {
        new Builder(this).setTitle("提示").setMessage("确定要退出吗？").setPositiveButton("确定", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        ExitApplication.getInstance().Exit();
                    }
                }, 500);
            }
        }).setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })/*.setNeutralButton("问题反馈", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })*/.create().show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(null != outState) {
            outState.putBoolean("mode",mIsAtMode);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(null != savedInstanceState) {
            mIsAtMode = savedInstanceState.getBoolean("mode",true);
        }
    }

    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mReception != null) {
                    OriginalReceive = appedByteArray(buffer, size);
                    ReceiveCount = ReceiveCount + size;
                    if (ReceiveCount > 65536) {
                        ReceiveCount = 0;
                    }
                    mReceiveCount.setText(String.valueOf(ReceiveCount));
                    isHex = hex.isChecked();
                    if (isHex) {
                        byte[] toShow = new byte[1000];
                        if (ReceiveCount > 1000) {
                            for (int i = 0; i < 1000; i++) {
                                toShow[i] = OriginalReceive[(ReceiveCount - 1000) + i];
                            }
                        }
//                        mReception.setText(toHexString(toShow, 1000));
//                        mReception.setSelection(mReception.length());
                        appendDisplay(toHexString(toShow,1000));
                        return;
                    }
                    if (OriginalReceive != null) {
//                        mReception.setText(new String(OriginalReceive, 0, ReceiveCount));
                        appendDisplay(new String(OriginalReceive, 0, ReceiveCount));
                    }
//                    mReception.setSelection(mReception.length());
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.OpenFlag = Boolean.valueOf(false);
        setContentView(R.layout.mainactivity);
        setting = (Button) findViewById(R.id.setting);
        connect = (Button) findViewById(R.id.connect);
        send = (Button) findViewById(R.id.send);
        clear = (Button) findViewById(R.id.clear);
//        mReception = (EditText) findViewById(R.id.display);
        mReception = (TextView) findViewById(R.id.display);
        mSend = (EditText) findViewById(R.id.sendbuf);
        hex = (ToggleButton) findViewById(R.id.hex);
        auto_send = (CheckBox) findViewById(R.id.auto_send);
        send_interval = (EditText) findViewById(R.id.send_interval);
        mSendCount = (TextView) findViewById(R.id.sendCount);
        mReceiveCount = (TextView) findViewById(R.id.receiveCount);
        mScrollView = (ScrollView) findViewById(R.id.sv_display);
        mDataAtMode = (ToggleButton) findViewById(R.id.mode_at_data);
        isHex = hex.isChecked();
        timer = null;
        send.setEnabled(false);
        auto_send.setEnabled(false);
        ExitApplication.getInstance().addActivity(this);
        connect.setOnClickListener(this);
        setting.setOnClickListener(this);
        hex.setOnCheckedChangeListener(this);
        send.setOnClickListener(this);
        clear.setOnClickListener(this);
        auto_send.setOnCheckedChangeListener(this);
        mDataAtMode.setOnCheckedChangeListener(this);
    }

    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg == null) {
            return "";
        }
        int i = 0;
        while (i < length) {
            result = new StringBuilder(String.valueOf(result)).append(Integer.toHexString(new Integer(arg[i] < (byte) 0 ? arg[i] + 256 : arg[i]).intValue())).append(" ").toString();
            Log.i("result", result);
            i++;
        }
        return result;
    }

    private boolean isAllHex(String arg) {
        if (arg == null) {
            return false;
        }
        char[] array = arg.toCharArray();
        int i = 0;
        while (i < array.length) {
            if (array[i] != ' ' && ((array[i] < '0' || array[i] > '9') && ((array[i] < 'a' || array[i] > 'f') && (array[i] < 'A' || array[i] > 'F')))) {
                return false;
            }
            i++;
        }
        return true;
    }

    private byte[] toByteArray(String arg) {
        if (arg != null) {
            int i;
            char[] NewArray = new char[65536];
            char[] array = arg.toCharArray();
            int length = 0;
            for (i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            int EvenLength = length % 2 == 0 ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                i = 0;
                while (i < length) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - 48;
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = (NewArray[i] - 97) + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = (NewArray[i] - 65) + 10;
                    }
                    i++;
                }
                byte[] byteArray = new byte[(EvenLength / 2)];
                for (i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) ((data[i * 2] * 16) + data[(i * 2) + 1]);
                }
                return byteArray;
            }
        }
        return new byte[0];
    }

    @Override
    protected void openDevice() {
        super.openDevice();
    }

    @Override
    protected void onDestroy() {
        auto_send.setChecked(false);
        super.onDestroy();
    }

    byte[] appedByteArray(byte[] newAppend, int newLength) {
        for (int i = 0; i < newLength; i++) {
            OriginalReceive[ReceiveCount + i] = newAppend[i];
        }
        return OriginalReceive;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == auto_send) {
            autoSend(isChecked);
        }
        else if(buttonView == mDataAtMode) {
            if(isChecked) {
                mIsAtMode = false;
            } else {
                mIsAtMode = true;
            }
        }
        else {
            hex(isChecked);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send: {
                send();
                break;
            }
            case R.id.clear: {
                clear();
                break;
            }
            case R.id.connect: {
                connect();
                break;
            }
            case R.id.setting: {
                goSetting();
                break;
            }
        }
    }

    void send() {
        boolean isSuccessful = false;
        if (!Application.OpenFlag.booleanValue()) {
            Toast warning = Toast.makeText(getApplicationContext(), "请先连接", 0);
            warning.setGravity(17, 0, 0);
            warning.show();
        } else if (!isHex) {
            isSuccessful = true;
            try {
                String text = mSend.getText().toString();

                if(mIsAtMode) {
                    text += "\r\n";
                }

                if(mIsAtMode && text.equals(Cmd.EXIT_MODE_AT + "\r\n")) {
                    mIsAtMode = false;
                    if(null != mDataAtMode) {
                        mDataAtMode.setChecked(true);
                    }
                }

                if(!mIsAtMode && text.equals(Cmd.EXIT_MODE_DATA)) {
                    mIsAtMode = true;
                    if(null != mDataAtMode) {
                        mDataAtMode.setChecked(false);
                    }
                }

                mOutputStream.write(text.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "发送失败！", 0).show();
                isSuccessful = false;
            }
            if (isSuccessful) {
                SendCount = SendCount + mSend.getText().toString().getBytes().length;
                mSendCount.setText(String.valueOf(SendCount));
            }
        } else if (isAllHex(mSend.getText().toString())) {
            isSuccessful = true;
            try {
                mOutputStream.write(toByteArray(mSend.getText().toString()));
            } catch (IOException e2) {
                e2.printStackTrace();
                Toast.makeText(MainActivity.this, "发送失败！", 0).show();
                isSuccessful = false;
            }
            if (isSuccessful) {
                SendCount = SendCount + toByteArray(mSend.getText().toString()).length;
                mSendCount.setText(String.valueOf(SendCount));
            }
        } else {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "您输入的字符有误，请重新输入！", 0).show();
                }
            });
        }
    }

    void clear() {
        mReception.setText("");
        mSend.setText("");
        ReceiveCount = 0;
        SendCount = 0;
        mSendCount.setText(String.valueOf(SendCount));
        mReceiveCount.setText(String.valueOf(ReceiveCount));
    }

    void connect() {
        if (Application.OpenFlag.booleanValue()) {
            Application.OpenFlag = Boolean.valueOf(false);
            connect.setText("打开串口");
            setting.setEnabled(true);
            send.setEnabled(false);
            auto_send.setEnabled(false);
            auto_send.setChecked(false);
            onDestroy();
            return;
        }
        openDevice();
        if (Application.OpenFlag.booleanValue()) {
            connect.setText("关闭串口");
            setting.setEnabled(false);
            send.setEnabled(true);
            auto_send.setEnabled(true);
            send_interval.setEnabled(true);
        }
    }

    void goSetting() {
        startActivity(new Intent(this, Setting.class));
    }

    void autoSend(boolean isChecked) {
        Log.i("click", "checked");
        if (isChecked) {
            send_interval.setEnabled(false);
            try {
                interval = Integer.valueOf(send_interval.getText().toString()).intValue();
            } catch (Exception e) {
            }
            if (timer == null) {
                timer = new Timer();
            }
            timer.schedule(new TimerTask() {
                public void run() {
                    Log.d("timer", "enter");
                    try {
                        interval = Integer.valueOf(send_interval.getText().toString()).intValue();
                    } catch (Exception e) {
                    }
                    boolean isSuccessful;
                    if (!isHex) {
                        isSuccessful = true;
                        try {
                            mOutputStream.write(mSend.getText().toString().getBytes());
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            isSuccessful = false;
                        }
                        if (isSuccessful) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    SendCount = SendCount + mSend.getText().toString().getBytes().length;
                                    mSendCount.setText(String.valueOf(SendCount));
                                }
                            });
                        }
                    } else if (isAllHex(mSend.getText().toString())) {
                        isSuccessful = true;
                        try {
                            mOutputStream.write(toByteArray(mSend.getText().toString()));
                        } catch (IOException e3) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, "发送失败！", 100).show();
                                }
                            });
                            isSuccessful = false;
                        }
                        if (isSuccessful) {
                            if (toByteArray(mSend.getText().toString()) != null) {
                                SendCount = SendCount + toByteArray(mSend.getText().toString()).length;
                            }
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mSendCount.setText(String.valueOf(SendCount));
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "您输入的字符有误，请重新输入！", 0).show();
                            }
                        });
                    }
                }
            }, 0, (long) interval);
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                send_interval.setEnabled(true);
            }
        });
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    void hex(boolean isChecked) {
        isHex = isChecked;
        if (!isChecked) {
            if (OriginalReceive != null) {
                mReception.setText(new String(OriginalReceive, 0, ReceiveCount));
            }
//            mReception.setSelection(mReception.length());
        } else if (OriginalReceive != null) {
            byte[] toShow = new byte[1000];
            if (ReceiveCount > 1000) {
                for (int i = 0; i < 1000; i++) {
                    toShow[i] = OriginalReceive[(ReceiveCount - 1000) + i];
                }
            }
            mReception.setText(toHexString(toShow, 1000));
//            mReception.setSelection(mReception.length());
        }
    }

    void appendDisplay(String str) {
        if(null != mReception) {
            mReception.append(str + "\n");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(null != mScrollView) {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }
            });
        }
    }
}
