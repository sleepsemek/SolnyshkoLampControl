package com.example.lampcontrol.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.lampcontrol.BluetoothConnectionThread;
import com.example.lampcontrol.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ControlLampActivity extends AppCompatActivity {

    private BluetoothConnectionThread connectedThread;

    private String address;
    private String name;

    private TextView lampName;
    private TextView timerTextView;

    private MainButton mainButton;

    private BottomSheetTimer bottomSheetTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_lamp);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        bottomSheetTimer = new BottomSheetTimer(this);
        mainButton = new MainButton(R.id.onOffBtn, this);

        lampName = findViewById(R.id.lampName);
        timerTextView = findViewById(R.id.timerTextView);

        timerTextView.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (address != null) {
            connectedThread.cancelRunning();
        }
        bottomSheetTimer.millisTimer.stopPreheat();
        timerTextView.setVisibility(View.GONE);
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

        connectedThread = new BluetoothConnectionThread(this, address);
        connectedThread.start();

        connectedThread.setOnConnectionStateChangeListener(state -> {
            if (state) {
                runOnUiThread(this::showInterface);
            } else {
                runOnUiThread(this::hideInterface);
            }
        });

        connectedThread.setOnCommandReceivedListener(new BluetoothConnectionThread.onCommandReceivedListener() {
            @Override
            public void onCommandReceived(int command) {
                mainButton.setState(command);
            }

            @Override
            public void onTimeReceived(long millis) {
                bottomSheetTimer.millisTimer.startTimer(millis);
                bottomSheetTimer.millisTimer.mainTimer.start();
            }

        });

    }

    private void hideInterface() {
        lampName.setText("Установка соединения");
        mainButton.hide();
        bottomSheetTimer.hideTimer();
        if (bottomSheetTimer.millisTimer.isPreheating) {
            bottomSheetTimer.millisTimer.stopPreheat();
        } else if (bottomSheetTimer.millisTimer.isPlaying) {
            bottomSheetTimer.millisTimer.stopTimer();
        }
        bottomSheetTimer.millisTimer.hideTimer();
        mainButton.offBtn.setVisibility(View.GONE);
    }

    private void showInterface() {
        lampName.setText(name);
        mainButton.show();
        bottomSheetTimer.showTimer();
        connectedThread.getStatus();
    }

    private class SecTimer {
        private long time = 1;

        private CountDownTimer mainTimer;
        private CountDownTimer preheatTimer;

        private boolean isPlaying = false;
        private boolean isPreheating = false;

        private void start(long preheatTime, long timerTime) {
            this.time = timerTime * 1000 * 60;
            startPreheat(preheatTime * 1000);
            connectedThread.sendData("relay:on#");
        }

        private void startPreheat(long preheatMillis) {
            isPreheating = true;
            bottomSheetTimer.disableButton();
            preheatTimer = new CountDownTimer(preheatMillis, 1000) {
                @Override
                public void onTick(long l) {
                    int sec = (int) (l / 1000);
                    int min = sec / 60;
                    sec = sec % 60;
                    timerTextView.setText("Преднагрев:\n" + String.format("%02d", min) + ":" + String.format("%02d", sec));
                }

                @Override
                public void onFinish() {
                    connectedThread.sendData("timer:settimer:" + time / 1000 + "#");
                    isPreheating = false;
                }
            };
            displayPreheat();
        }

        private void displayPreheat() {
            if (preheatTimer != null) {
                preheatTimer.start();
                timerTextView.setVisibility(View.VISIBLE);
            } else {
                this.stopPreheat();
            }
        }

        private void showTimer() {
            int sec = (int) (time / 1000);
            int min = sec / 60;
            sec = sec % 60;
            timerTextView.setVisibility(View.VISIBLE);
            timerTextView.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));

        }

        private void hideTimer() {
            timerTextView.setVisibility(View.GONE);
            timerTextView.setText("00:00");
        }

        private void stopPreheat() {
            if (preheatTimer != null) {
                preheatTimer.cancel();
                bottomSheetTimer.enableButton();
//                connectedThread.sendData("relay:off#");
            }
        }

        private void startTimer(long millis) {
            time = millis;
            isPlaying = true;
            mainTimer = new CountDownTimer(time, 1000) {
                @Override
                public void onTick(long l) {
                    time = l;
                    int sec = (int) (l / 1000);
                    int min = sec / 60;
                    sec = sec % 60;
                    timerTextView.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));
                }

                @Override
                public void onFinish() {

                }
            };
            showTimer();
        }

        public void stopTimer() {
            if (mainTimer != null) {
                mainTimer.cancel();
            }
            isPlaying = false;
        }

        public void pauseTimer() {
            if (mainTimer != null) {
                mainTimer.cancel();
            }
        }

        public void resumeTimer() {
            this.stopTimer();
            connectedThread.sendData("timer:gettime#");
        }

    }

    private class BottomSheetTimer {

        private SharedPreferences sharedPreferences;
        private final BottomSheetDialog bottomSheetDialog;
        private final AppCompatButton showBottomSheetButton;

        private final NumberPicker preheatTimePicker;
        private final NumberPicker mainTimePicker;
        private final AppCompatButton startTimerButton;

        private final SecTimer millisTimer;

        public BottomSheetTimer(Context context) {
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.lamp_bottom_sheet);

            showBottomSheetButton = findViewById(R.id.timer_button);

            preheatTimePicker = bottomSheetDialog.findViewById(R.id.preheat_picker);
            assert preheatTimePicker != null;
            preheatTimePicker.setMinValue(30);
            preheatTimePicker.setMaxValue(120);

            mainTimePicker = bottomSheetDialog.findViewById(R.id.timer_picker);
            assert mainTimePicker != null;
            mainTimePicker.setMinValue(1);
            mainTimePicker.setMaxValue(30);

            mainTimePicker.setValue(getLastTime());

            startTimerButton = bottomSheetDialog.findViewById(R.id.start_timer);

            millisTimer = new SecTimer();

            showBottomSheetButton.setOnClickListener(view -> bottomSheetTimer.bottomSheetDialog.show());

            assert startTimerButton != null;
            startTimerButton.setOnClickListener(view -> {
                millisTimer.start(preheatTimePicker.getValue(), mainTimePicker.getValue());
                setLastTime(mainTimePicker.getValue());
                bottomSheetDialog.cancel();
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

        private void hideTimer() {
            showBottomSheetButton.setEnabled(false);
        }

        private void showTimer() {
            showBottomSheetButton.setEnabled(true);
        }

        private void enableButton() {
            startTimerButton.setEnabled(true);
            startTimerButton.setBackgroundResource(R.drawable.background_blue);
        }

        private void disableButton() {
            startTimerButton.setEnabled(false);
            startTimerButton.setBackgroundResource(R.drawable.btn_background_grey);
        }

    }

    private class MainButton {
        private final Button btn;
        private final AppCompatButton offBtn;
        private int state = 0;
        private final Context context;

        public MainButton(int view, Context context) {
            btn = findViewById(view);
            offBtn = findViewById(R.id.off_button);
            this.context = context;

            offBtn.setOnClickListener(view12 -> {
                connectedThread.sendData("relay:off#");
            });

            btn.setOnClickListener(view1 -> {
                switch (state) {
                    case 1: //on
                        displayAlert();
                        break;
                    case 0: //idle
                        connectedThread.sendData("relay:on#");
                        break;
                    case 3: //timer playing
                        connectedThread.sendData("timer:pause#");
                        break;
                    case 4: //timer paused
                        connectedThread.sendData("timer:resume#");
                        break;
                }
            });
        }

        public void setState(int state) {
            switch (state) {
                case 2: //turned off
                    this.state = 0;
                    timerTextView.setText("00:00");
                    timerTextView.setVisibility(View.GONE);
                    btn.setText("Включить");
                    offBtn.setVisibility(View.GONE);
                    bottomSheetTimer.enableButton();
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;
                case 1: //turned on
                    if (!bottomSheetTimer.millisTimer.isPlaying) {
                        this.state = 1;
                        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                        btn.setText("Выключить");
                        offBtn.setVisibility(View.GONE);
                        if (!bottomSheetTimer.millisTimer.isPreheating) {
                            bottomSheetTimer.enableButton();
                        }
                    }
                    break;
                case 3: //timer started
                    bottomSheetTimer.millisTimer.resumeTimer();
                    this.state = 3;
                    bottomSheetTimer.millisTimer.showTimer();
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                    btn.setText("Пауза");
                    offBtn.setVisibility(View.VISIBLE);
                    bottomSheetTimer.disableButton();
                    break;
                case 4: //timer is not active
                    bottomSheetTimer.millisTimer.stopTimer();
                    bottomSheetTimer.millisTimer.hideTimer();
                    this.state = 0;
                    btn.setText("Включить");
                    offBtn.setVisibility(View.GONE);
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    bottomSheetTimer.enableButton();
                    break;
                case 5: //timer paused
                    this.state = 4;
                    bottomSheetTimer.millisTimer.pauseTimer();
                    btn.setText("Пуск");
                    offBtn.setVisibility(View.VISIBLE);
                    bottomSheetTimer.millisTimer.showTimer();
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    bottomSheetTimer.disableButton();
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

        private void displayAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Предупреждение");
            builder.setMessage(R.string.off_warning);

            builder.setPositiveButton(Html.fromHtml("<font color='#e31e24'>Отключить в любом случае</font>"), (dialog, which) -> {
                bottomSheetTimer.millisTimer.stopPreheat();
                connectedThread.sendData("relay:off#");
            });

            builder.setNegativeButton(Html.fromHtml("<font color='#0bbdff'>Отменить</font>"), (dialog, which) -> {});

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

}