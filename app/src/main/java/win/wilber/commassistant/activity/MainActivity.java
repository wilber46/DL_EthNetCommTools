package win.wilber.commassistant.activity;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.yunovo.devicenode.DeviceNodeManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import win.wilber.commassistant.R;
import win.wilber.commassistant.util.Cmd;

import static win.wilber.commassistant.util.DataUtil.isAllHex;
import static win.wilber.commassistant.util.DataUtil.toByteArray;
import static win.wilber.commassistant.util.DataUtil.toHexString;

public class MainActivity extends SerialPortActivity implements View.OnClickListener, OnCheckedChangeListener {
    private final int MAX_LENGTH = 65536;
//    private byte[] OriginalReceive = new byte[65536];
    private StringBuilder mReceiver = new StringBuilder();
    private int ReceiveCount = 0;
    private int SendCount;
    private CheckBox auto_send;
    private Button clear,connect,send,setting,mConfigUdpMode,mReset;
    private int interval;
    private boolean isHex;
    private TextView mReceiveCount;
//    private EditText mReception;
    private TextView mReception;
    private EditText mSend;
    private TextView mSendCount;
    private EditText send_interval;
    private Timer timer = null;
    private ScrollView mScrollView;
    private CheckBox mPowerEth, mConfigStaticIp,hex;
    private ToggleButton mDataAtMode;
    private EditText mRemoteIp,mRemotePort,mLocalIp,mLocalNetMask,mLocalGateway;
    private LinearLayout mLLStaticIp;
    private boolean mIsAtMode = true;
    private boolean mIsStaticIpMode = true;

    private DeviceNodeManager mDeviceNodeManager;

    @Override
    public void onBackPressed() {
        new Builder(this).setTitle("提示").setMessage("确定要退出吗？").setPositiveButton("确定", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Handler().postDelayed(new Runnable() {
                    @Override
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
                handleData(buffer,size);
//                if (mReception != null) {
//                    OriginalReceive = appedByteArray(buffer, size);
//                    ReceiveCount = ReceiveCount + size;
//                    if (ReceiveCount > 65536) {
//                        ReceiveCount = 0;
//                    }
//                    mReceiveCount.setText(String.valueOf(ReceiveCount));
//                    isHex = hex.isChecked();
//                    if (isHex) {
//                        byte[] toShow = new byte[1000];
//                        if (ReceiveCount > 1000) {
//                            for (int i = 0; i < 1000; i++) {
//                                toShow[i] = OriginalReceive[(ReceiveCount - 1000) + i];
//                            }
//                        }
////                        mReception.setText(toHexString(toShow, 1000));
////                        mReception.setSelection(mReception.length());
//                        appendDisplay(toHexString(toShow,1000));
//                        return;
//                    }
//                    if (OriginalReceive != null) {
////                        mReception.setText(new String(OriginalReceive, 0, ReceiveCount));
//                        appendDisplay(new String(OriginalReceive, 0, ReceiveCount));
//                    }
////                    mReception.setSelection(mReception.length());
//                }
            }
        });
    }

    private void handleData(byte[] buffer, int size) {
        if (mReception != null) {
            String data = new String(buffer);
            mReceiver.append(data);

            ReceiveCount += size;
            mReceiveCount.setText(String.valueOf(ReceiveCount));
            isHex = hex.isChecked();
            if (isHex) {
                mReception.setText("");
                appendDisplay(toHexString(mReceiver.toString().getBytes(),mReceiver.length()));
                return;
            }

            appendDisplay(data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainactivity);

        Application.OpenFlag = Boolean.valueOf(false);

        initView();

        mConfigStaticIp.setChecked(mIsStaticIpMode);

        isHex = hex.isChecked();
        timer = null;

        mDeviceNodeManager = new DeviceNodeManager(this);
        if(null != mDeviceNodeManager) {
            mPowerEth.setChecked(mDeviceNodeManager.getEthNetStatus());
        }
    }

    void initView() {
        setting = (Button) findViewById(R.id.setting);
        connect = (Button) findViewById(R.id.connect);
        send = (Button) findViewById(R.id.send);
        clear = (Button) findViewById(R.id.clear);
//        mReception = (EditText) findViewById(R.id.display);
        mReception = (TextView) findViewById(R.id.display);
        mSend = (EditText) findViewById(R.id.sendbuf);
        hex = (CheckBox) findViewById(R.id.hex);
        auto_send = (CheckBox) findViewById(R.id.auto_send);
        send_interval = (EditText) findViewById(R.id.send_interval);
        mSendCount = (TextView) findViewById(R.id.sendCount);
        mReceiveCount = (TextView) findViewById(R.id.receiveCount);
        mScrollView = (ScrollView) findViewById(R.id.sv_display);
        mDataAtMode = (ToggleButton) findViewById(R.id.mode_at_data);
        mConfigUdpMode = (Button) findViewById(R.id.configUdpMode);
        mRemoteIp = (EditText) findViewById(R.id.remoteIp);
        mRemotePort = (EditText) findViewById(R.id.remotePort);
        mPowerEth = (CheckBox) findViewById(R.id.powerEth);
        mLLStaticIp = (LinearLayout) findViewById(R.id.ll_staticIp);
        mLocalIp = (EditText) findViewById(R.id.localIp);
        mLocalNetMask = (EditText) findViewById(R.id.localNetMask);
        mLocalGateway = (EditText) findViewById(R.id.localGateway);
        mConfigStaticIp = (CheckBox) findViewById(R.id.configStaticIp);
        mReset = (Button) findViewById(R.id.reset);

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
        mConfigUdpMode.setOnClickListener(this);
        mPowerEth.setOnCheckedChangeListener(this);
        mConfigStaticIp.setOnCheckedChangeListener(this);
        mReset.setOnClickListener(this);
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

//    byte[] appedByteArray(byte[] newAppend, int newLength) {
//        for (int i = 0; i < newLength; i++) {
//            OriginalReceive[ReceiveCount + i] = newAppend[i];
//        }
//        return OriginalReceive;
//    }

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
        else if(buttonView == mPowerEth) {
            powerEth(isChecked);
        }
        else if(buttonView == hex) {
            hex(isChecked);
        }
        else if(mConfigStaticIp == buttonView) {
            switchDhcp(isChecked);
        }
    }

    private void switchDhcp(boolean checked) {
        mIsStaticIpMode = checked;
        if(checked) {
            mLLStaticIp.setVisibility(View.VISIBLE);
        } else {
            mLLStaticIp.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send: {
                send("");
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
            case R.id.configUdpMode: {
                configUdpMode();
                break;
            }
            case R.id.reset: {
                reset();
                break;
            }
        }
    }

    private void reset() {
        if(!mIsAtMode) {
            Toast.makeText(this,"请退出数据模式再操作",Toast.LENGTH_SHORT).show();
        } else {
            send(Cmd.reset("admin"));   // 默认密码为admin
        }
    }

    void powerEth(boolean enable) {
        if(null != mDeviceNodeManager) {
            if(enable) {
                if(!mDeviceNodeManager.getEthNetStatus()) {
                    mDeviceNodeManager.setEthNetStatus(true);
                }
            } else {
                if(mDeviceNodeManager.getEthNetStatus()) {
                    mDeviceNodeManager.setEthNetStatus(false);
                }
            }
        }
    }

    private void configUdpMode() {
        send(Cmd.enableEcho(true));
        send(Cmd.configWorkMode(Cmd.MODE_UDP));
        send(Cmd.configDhcpMode(mIsStaticIpMode ? Cmd.MODE_STATIC_IP : Cmd.MODE_DHCP));
        send(Cmd.getLocalPort("5000"));
        String remotePort = "";
        String remoteIP = "";
        String localIp = "";
        String localMask = "";
        String localGateway = "";
        if(null != mRemotePort) {
            String tmpPort = mRemotePort.getText().toString();
            if(TextUtils.isEmpty(tmpPort)) {
                remotePort = "12345";
            } else {
                remotePort = tmpPort;
            }
        }
        send(Cmd.getRemotePort(remotePort));

        if(null != mRemoteIp) {
            String tmpIp = mRemoteIp.getText().toString();
            if(TextUtils.isEmpty(tmpIp)) {
                remoteIP = "192.168.1.99";
            } else {
                remoteIP = tmpIp;
            }
        }
        send(Cmd.getRemoteIp(remoteIP));

        if(mIsStaticIpMode) {
            if(null != mLocalIp) {
                String ip = mLocalIp.getText().toString();
                if(TextUtils.isEmpty(ip)) {
                    localIp = "192.168.1.77";
                } else {
                    localIp = ip;
                }
            }
            send(Cmd.getLocalIp(localIp));

            if(null != mLocalNetMask) {
                String mask = mLocalNetMask.getText().toString();
                if(TextUtils.isEmpty(mask)) {
                    localMask = "255.255.255.0";
                } else {
                    localMask = mask;
                }
            }
            send(Cmd.getLocalMask(localMask));

            if(null != mLocalGateway) {
                String gateway = mLocalGateway.getText().toString();
                if(TextUtils.isEmpty(gateway)) {
                    localGateway = "192.168.1.1";
                } else {
                    localGateway = gateway;
                }
            }
            send(Cmd.getLocalGateway(localGateway));
        }
//        send(Cmd.getRemoteIp("10.0.0.122"));
//        send(Cmd.getRemotePort("12345"));
        send(Cmd.EXIT_MODE_AT);
    }

    void send(String content) {
        boolean isSuccessful = false;
        if (!Application.OpenFlag.booleanValue()) {
            Toast warning = Toast.makeText(getApplicationContext(), "请先连接", 0);
            warning.setGravity(17, 0, 0);
            warning.show();
        } else if (!isHex) {
            isSuccessful = true;
            try {
                String text = "";
                if(TextUtils.isEmpty(content)) {
                    text = mSend.getText().toString();
                } else {
                    text = content;
                }

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
                                @Override
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
                                @Override
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
//            if (OriginalReceive != null) {
//                mReception.setText(new String(OriginalReceive, 0, ReceiveCount));
//            }
            if(mReceiver.length() > 0) {
                mReception.setText("");
                appendDisplay(mReceiver.toString());
            }
//            mReception.setSelection(mReception.length());
        }
//        else if (OriginalReceive != null) {
//            byte[] toShow = new byte[1000];
//            if (ReceiveCount > 1000) {
//                for (int i = 0; i < 1000; i++) {
//                    toShow[i] = OriginalReceive[(ReceiveCount - 1000) + i];
//                }
//            }
//            mReception.setText(toHexString(toShow, 1000));
////            mReception.setSelection(mReception.length());
//        }
        else {
            if(mReceiver.length() > 0) {
                mReception.setText("");
                appendDisplay(toHexString(mReceiver.toString().getBytes(),mReceiver.length()));
            }
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
