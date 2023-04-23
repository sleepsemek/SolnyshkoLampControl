package com.example.lampcontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class detailedLampActivity extends AppCompatActivity {

    private ConnectedThread connectedThread;

    private String address;
    private String name;

    private TextView lampName;
    private TextView timerTextView;

    private MainButton mainButton;

    private BottomSheetTimer bottomSheetTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_lamp);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        bottomSheetTimer = new BottomSheetTimer(this);
        mainButton = new MainButton(R.id.onOffBtn, this);

        lampName = findViewById(R.id.lampName);
        timerTextView = findViewById(R.id.timerTextView);
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
        mainButton.hide();
    }

    private void showInterface() {
        lampName.setText(name);
        mainButton.show();
        connectedThread.getStatus();
    }

    private class SecTimer {
        private int time = 1;

        private CountDownTimer timer;
        private CountDownTimer preheat;

        private void start(int preheatTime, int timerTime) {
            this.time = timerTime;
            startPreheat(preheatTime);
            connectedThread.sendData("relay:on#");
        }

        private void startPreheat(int preheatTime) {
            int seconds = preheatTime;
            int millis = seconds * 1000;
            preheat = new CountDownTimer(millis, 1000) {
                @Override
                public void onTick(long l) {
                    int sec = (int) (l / 1000);
                    int min = sec / 60;
                    sec = sec % 60;
                    timerTextView.setText("Преднагрев:\n" + String.format("%02d", min) + ":" + String.format("%02d", sec));
                }

                @Override
                public void onFinish() {
                    startTimer(time);
                }
            };
        }

        private void displayPreheat() {
            if (preheat != null) {
                preheat.start();
            } else {
                stopPreheat();
            }
        }

        private void stopPreheat() {
            if (preheat != null) {
                preheat.cancel();
            }
            connectedThread.sendData("relay:off#");
        }

        private void startTimer(int time) {
            int seconds = time * 60;
            int millis = seconds * 1000;
            connectedThread.sendData("timer:settimer:" + seconds + "#");
            timer = new CountDownTimer(millis, 1000) {
                @Override
                public void onTick(long l) {
                    int sec = (int) (l / 1000);
                    int min = sec / 60;
                    sec = sec % 60;
                    timerTextView.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));
                }

                @Override
                public void onFinish() {

                }
            };
        }

        private void displayTimer() {
            if (!timer.isPaused()) {
                timer.start();
            } else {
                timer.resume();
            }
        }

        public void stopTimer() {
            if (timer != null) {
                timer.cancel();
            }
        }

        public void pauseTimer() {
            timer.pause();
        }

        public void resumeTimer() {
            timer.resume();
        }

    }

    private class BottomSheetTimer {

        private SharedPreferences sharedPreferences;
        private BottomSheetDialog bottomSheetDialog;

        private NumberPicker preheatPicker;
        private NumberPicker timerPicker;
        private AppCompatButton startTimer;

        private SecTimer timer;

        public BottomSheetTimer(Context context) {
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.lamp_bottom_sheet);

            preheatPicker = bottomSheetDialog.findViewById(R.id.preheat_picker);
            preheatPicker.setMinValue(30);
            preheatPicker.setMaxValue(120);

            timerPicker = bottomSheetDialog.findViewById(R.id.timer_picker);
            timerPicker.setMinValue(1);
            timerPicker.setMaxValue(30);

            timerPicker.setValue(getLastTime());

            startTimer = bottomSheetDialog.findViewById(R.id.start_timer);

            timer = new SecTimer();

            startTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timer.start(preheatPicker.getValue(), timerPicker.getValue());
                    setLastTime(timerPicker.getValue());
                    bottomSheetDialog.cancel();
                }
            });
        }

        private void setLastTime(int time) {
            sharedPreferences = getSharedPreferences("timer", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(address, time);
            editor.apply();
        }

        private int getLastTime() {
            sharedPreferences = getSharedPreferences("timer", MODE_PRIVATE);
            return sharedPreferences.getInt(address, 0);
        }
    }

    private class MainButton {
        private final Button btn;
        private int state = 0;
        private Context context;

        public MainButton(int view, Context context) {
            btn = findViewById(view);
            this.context = context;

            btn.setOnClickListener(view1 -> {
                switch (state) {
                    case 1: //preheating
                        displayAlert();
                        break;
                    case 0: //idle
                        bottomSheetTimer.bottomSheetDialog.show();
                        break;
                    case 3: //timer playing
                        connectedThread.sendData("timer:pause#");
                    case 4: //timer paused
                        connectedThread.sendData("timer:start#");
                }
            });
        }

        public void setState(int state) {
            switch (state) {
                case 2: //turned off
                    this.state = 0;
                    timerTextView.setText("00:00");
                    btn.setText("Включить");
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;
                case 1: //preheating
                    this.state = 1;
                    bottomSheetTimer.timer.displayPreheat();
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                    btn.setText("Преднагрев");
                    break;
                case 3: //timer started
                    bottomSheetTimer.timer.displayTimer();
                    this.state = 3;
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                    btn.setText("Пауза");
                    break;
                case 4: //timer finished
                    bottomSheetTimer.timer.stopTimer();
                    timerTextView.setText("00:00");
                    this.state = 0;
                    btn.setText("Включить");
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;
                case 5: //timer paused
                    this.state = 4;
                    bottomSheetTimer.timer.pauseTimer();
                    btn.setText("Возобновить");
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;
            }
        }

        private void hide() {
            btn.setText("");
            btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
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

        private void displayAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Предупреждение");
            builder.setMessage("В первую минуту работы происходит нагрев и стабилизация лампы.\nОтключение в течение этого периода может привести к нарушению работы устройства.");

            builder.setPositiveButton("Отключить в любом случае", (dialog, which) -> {
                bottomSheetTimer.timer.stopPreheat();

            });

            builder.setNegativeButton("Отменить", (dialog, which) -> {});

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

}