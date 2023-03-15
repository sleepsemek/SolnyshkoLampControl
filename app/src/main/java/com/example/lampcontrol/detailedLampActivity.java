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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class detailedLampActivity extends AppCompatActivity {

    private ConnectedThread connectedThread;

    private String address;
    private String name;

    private TextView lampName;

    private SecTimer timer;

    private MainButton mainButton;
    private TimerMenu timerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_lamp);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        mainButton = new MainButton(R.id.onOffBtn);
        timerMenu = new TimerMenu(R.id.sideMenu, R.id.openArrow);
        timer = new SecTimer(R.id.timePicker, R.id.startTimer);
        lampName = findViewById(R.id.lampName);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (address != null) {
            connectedThread.cancelRunning();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        connectedThread.cancelRunning();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideInterface();

        connectedThread = new ConnectedThread(this, address);
        connectedThread.start();

        connectedThread.setOnConnectionStateChangeListener(state -> {
            if (state) {
                runOnUiThread(this::showInterface);
            } else {
                runOnUiThread(this::hideInterface);
            }
        });

        connectedThread.setOnCommandReceivedListener(command -> mainButton.setState(command));
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
            connectedThread.sendData("timer:settimer:" + time * 60 + "#");
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
                case 4:
                    this.state = 0;
                    btn.setText("Включить");
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;
                case 1:
                case 3:
                    this.state = 1;
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                    btn.setText("Выключить");
                    break;
            }
        }

        private void hide() {
            btn.setText("");
            btn.setClickable(false);
            btn.animate().rotationBy(360000).setDuration(500000).setInterpolator(new LinearInterpolator()).start();
        }

        private void show() {
            btn.clearAnimation();
            btn.setText("Включить");
            btn.setClickable(true);
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