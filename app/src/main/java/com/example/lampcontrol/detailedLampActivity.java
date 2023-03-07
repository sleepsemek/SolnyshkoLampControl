package com.example.lampcontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class detailedLampActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private ConnectedThread connectedThread;

    private static final int RECEIVE_MESSAGE = 1;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String address;
    private String name;

    private TextView lampName;

    private SecTimer timer;

    private Handler handler;

    private MainButton mainButton;
    private TimerMenu timerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_lamp);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mainButton = new MainButton(R.id.onOffBtn);
        timerMenu = new TimerMenu(R.id.sideMenu, R.id.openArrow);
        timer = new SecTimer(R.id.timePicker, R.id.startTimer);

        lampName = findViewById(R.id.lampName);

        hideInterface();
    }

    private void hideInterface() {
        lampName.setText("Установка соединения");
        timerMenu.hide();
        mainButton.hide();
    }

    private void showInterface() {
        lampName.setText(name);
        timerMenu.show();
        mainButton.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (address != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        connectedThread.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(() -> {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            try {
                checkPermission(101);
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bluetoothSocket.connect();
            } catch (IOException e) {
                try {
                    bluetoothSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            connectedThread = new ConnectedThread(bluetoothSocket);
            connectedThread.start();

            if (connectedThread.getConnectionState().equals("CONNECTED")) {
                runOnUiThread(this::showInterface);
            } else {
                hideInterface();
                onResume();
            }
        }).start();
        receiveState();
    }

    public void checkPermission(int requestCode) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE}, requestCode);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private void receiveState() {
        StringBuilder stringBuilder = new StringBuilder();
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    byte[] readBuf = (byte[]) msg.obj;
                    String incoming = new String(readBuf, 0, msg.arg1);
                    stringBuilder.append(incoming);
                    int endOfLineIndex = stringBuilder.indexOf("#");
                    System.out.println(stringBuilder);
                    if (endOfLineIndex > 0) {
                        String print = stringBuilder.substring(0, endOfLineIndex);
                        mainButton.setState(Integer.parseInt(print));
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }
            }
        };
    }

    private class SecTimer {
        private int time = 1;
        private final Button btnStart;
        private final NumberPicker numberPicker;

        public SecTimer(int timePicker, int startTimer) {
            numberPicker = findViewById(timePicker);
            btnStart = findViewById(startTimer);

            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(30);

            btnStart.setOnClickListener(view -> {
                time = numberPicker.getValue();
                startTimer(time);
            });
        }

        private void startTimer(int time) {
            connectedThread.sendData("!settimer:" + time * 60 + "#");
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private String connectionState;

        public ConnectedThread(BluetoothSocket socket) {
            connectionState = "DISCONNECTED";
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                connectionState = "CONNECTED";
            } catch (IOException e) {
                connectedThread.cancel();
                connectionState = "DISCONNECTED";
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_DENIED) {
                bluetoothAdapter.cancelDiscovery();
            } else {
                connectedThread.cancel();
                connectionState = "DISCONNECTED";
                return;
            }
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    handler.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    connectedThread.cancel();
                    connectionState = "DISCONNECTED";
                    break;
                }
            }
        }

        public void sendData(String message) {
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                connectedThread.cancel();
                connectionState = "DISCONNECTED";
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
                connectionState = "DISCONNECTED";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getConnectionState() {
            return connectionState;
        }
    }

    private class MainButton {
        private final Button btn;
        private int state = 0;

        public MainButton(int view) {
            btn = findViewById(view);

            btn.setOnClickListener(view1 -> {
                switch (state) {
                    case 1:
                        connectedThread.sendData("relay:off#");
                        break;
                    case 0:
                        connectedThread.sendData("relay:on#");
                        break;
                }
            });
        }

        public void setState(int state) {
            switch (state) {
                case 2:
                    this.state = 0;
                    btn.setText("Включить");
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;
                case 1:
                    this.state = 1;
                    btn.setText("Выключить");
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                    break;
            }
        }

        private void hide() {
            btn.setText("");
            btn.animate().rotationBy(36000).setDuration(50000).setInterpolator(new LinearInterpolator()).start();
        }

        private void show() {
            btn.clearAnimation();
            btn.setVisibility(View.VISIBLE);
            btn.setText("Включить");
            btn.animate().cancel();
            btn.animate().rotation(0).setDuration(0).setInterpolator(new DecelerateInterpolator());
        }

        public int getState() {
            return state;
        }

    }

    private class TimerMenu {
        private final Button openArrow;
        private final RelativeLayout sideMenu;
        private int state = 0;

        public TimerMenu(int view, int btn) {
            sideMenu = findViewById(view);
            openArrow = findViewById(btn);

            openArrow.setOnClickListener(view1 -> {
                switch (state) {
                    case 0:
                        sideMenu.animate().translationX(350).setDuration(300).setInterpolator(new DecelerateInterpolator());
                        openArrow.animate().rotation(180).setDuration(300).setInterpolator(new DecelerateInterpolator());
                        state = 1;
                        break;
                    case 1:
                        sideMenu.animate().translationX(0).setDuration(300).setInterpolator(new DecelerateInterpolator());
                        openArrow.animate().rotation(0).setDuration(300).setInterpolator(new DecelerateInterpolator());
                        state = 0;
                        break;
                }
            });
        }

        private void hide() {
            openArrow.setVisibility(View.INVISIBLE);
        }

        private void show() {
            openArrow.setVisibility(View.VISIBLE);
        }

        public void setState(int state) {
            this.state = state;
            openArrow.callOnClick();
        }

        public int getState() {
            return state;
        }

    }

}