package com.example.lampcontrol.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import com.example.lampcontrol.TimerView;
import com.example.lampcontrol.ValuesSavingManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ControlLampActivity extends AppCompatActivity {

    private BluetoothConnectionThread connectedThread;

    private String address;
    private String name;

    private TextView lampName;
    private TextView timerTextView;
    private TimerView timerDialView;

    private MainButton mainButton;

    private BottomSheetTimer bottomSheetTimer;
    private ValuesSavingManager valuesSavingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_lamp);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        valuesSavingManager = new ValuesSavingManager(this, address);

        bottomSheetTimer = new BottomSheetTimer(this);
        mainButton = new MainButton(R.id.onOffBtn, this);

        lampName = findViewById(R.id.lampName);
        timerTextView = findViewById(R.id.timerTextView);
        timerDialView = findViewById(R.id.timerDialView);

        timerTextView.setVisibility(View.GONE);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (address != null) {
            connectedThread.cancelRunning();
        }
        bottomSheetTimer.secTimer.stopPreheat();
        bottomSheetTimer.secTimer.stopTimer();
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
            public void onTimerTimeReceived(long millis) {
                bottomSheetTimer.secTimer.setTimerTime(millis);
            }

            @Override
            public void onPreheatTimeReceived(long millis) {
                bottomSheetTimer.secTimer.setPreheatTimerTime(millis);
            }

        });

    }

    private void hideInterface() {
        lampName.setText("Установка соединения");
        mainButton.hide();
        bottomSheetTimer.hideTimer();
        if (bottomSheetTimer.secTimer.isPreheating) {
            bottomSheetTimer.secTimer.stopPreheat();
        } else if (bottomSheetTimer.secTimer.isPlaying) {
            bottomSheetTimer.secTimer.stopTimer();
        }
        bottomSheetTimer.secTimer.hideTimer();
        mainButton.offBtn.setVisibility(View.GONE);
    }

    private void showInterface() {
        lampName.setText(name);
        mainButton.show();
        bottomSheetTimer.showTimer();
        connectedThread.getStatus();
    }

    private class SecTimer {

        private CountDownTimer preheatTimer;
        private CountDownTimer mainTimer;

        private boolean isPreheating = false;
        private boolean isPlaying = false;

        private int iterations;
        private long iterationTimeMillis;

        public SecTimer() {
            iterations = valuesSavingManager.getIterations();
            iterationTimeMillis = ((valuesSavingManager.getLastTime().getMinutes() * 60) + valuesSavingManager.getLastTime().getSeconds()) * 1000;
        }

        private void start() {
            iterations = valuesSavingManager.getIterations();
            iterationTimeMillis = ((valuesSavingManager.getLastTime().getMinutes() * 60) + valuesSavingManager.getLastTime().getSeconds()) * 1000;

            beginDevicePreheatTimer();
        }

        private void beginDevicePreheatTimer() {
            connectedThread.sendData("timer:preheat#");
        }

        private void setPreheatTimerTime(long millis) {
            if (millis == 0) {
                return;
            }

            bottomSheetTimer.disableButton();

            preheatTimer = new CountDownTimer(millis, 1000) {
                @Override
                public void onTick(long l) {
                    int sec = (int) (l / 1000);
                    int min = sec / 60;
                    sec = sec % 60;
                    timerTextView.setText("Преднагрев:\n" + String.format("%02d", min) + ":" + String.format("%02d", sec));
                }

                @Override
                public void onFinish() {
                    isPreheating = false;
                    connectedThread.sendData("timer:preheatstop#");
                    bottomSheetTimer.secTimer.beginDeviceTimer();
                }
            };

        }

        private void runPreheat() {
            if (preheatTimer != null) {
                isPreheating = true;
                preheatTimer.start();
                showTimer();
            } else {
                this.stopPreheat();
            }
        }

        private void stopPreheat() {
            isPreheating = false;
            if (preheatTimer != null) {
                preheatTimer.cancel();
                bottomSheetTimer.enableButton();
            }
        }

        private void beginDeviceTimer() {
            connectedThread.sendData("timer:settimer:" + (iterationTimeMillis / 1000 * iterations) + "#");
        }

        private void setTimerTime(long millis) {
            if (millis == 0) {
                return;
            }

            millis -= 1;
            int remainedIterations = (int) Math.floor(millis / (float) iterationTimeMillis);
            float remainderSec = (millis % (float) iterationTimeMillis) / 1000;
            long remainedTime = (long) (Math.ceil(remainderSec) * 1000L);

            stopTimer();

            timerDialView.setBounds(iterationTimeMillis * iterations, iterations);
            setTimerViewTime(iterationTimeMillis, remainedTime, remainedIterations + 1);

            mainTimer = new CountDownTimer(remainedTime, 1000) {
                @Override
                public void onTick(long l) {
                    setTimerViewTime(iterationTimeMillis, l, remainedIterations + 1);

                }

                @Override
                public void onFinish() {
                    if (remainedIterations != iterations && remainedIterations != 0) {
                        connectedThread.sendData("timer:settimer:" + (iterationTimeMillis / 1000 * remainedIterations) + "#");
                        connectedThread.sendData("timer:pause#");
                    }

                }
            };
        }

        private void startTimer() {
            if (mainTimer != null) {
                bottomSheetTimer.disableButton();
                mainTimer.start();
                isPlaying = true;
            }
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
            isPlaying = true;
        }

        private void showTimer() {
            if (isPreheating) {
                timerDialView.setVisibility(View.GONE);
            } else {
                timerDialView.setVisibility(View.VISIBLE);
            }
            timerTextView.setVisibility(View.VISIBLE);

        }

        private void hideTimer() {
            timerDialView.setVisibility(View.GONE);
            timerTextView.setVisibility(View.GONE);
        }
    }

    private class BottomSheetTimer {

        private final BottomSheetDialog bottomSheetDialog;
        private final AppCompatButton showBottomSheetButton;

        private final NumberPicker iterationPicker;
        private final NumberPicker minutesPicker;
        private final NumberPicker secondsPicker;

        private final AppCompatButton startTimerButton;

        private final SecTimer secTimer;

        public BottomSheetTimer(Context context) {
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.lamp_bottom_sheet);

            showBottomSheetButton = findViewById(R.id.timer_button);

            iterationPicker = bottomSheetDialog.findViewById(R.id.preheat_picker);
            assert iterationPicker != null;
            iterationPicker.setMinValue(1);
            iterationPicker.setMaxValue(10);

            minutesPicker = bottomSheetDialog.findViewById(R.id.minutes_picker);
            secondsPicker = bottomSheetDialog.findViewById(R.id.seconds_picker);

            assert minutesPicker != null;
            minutesPicker.setMinValue(0);
            minutesPicker.setMaxValue(30);

            assert secondsPicker != null;
            secondsPicker.setMinValue(0);
            secondsPicker.setMaxValue(59);

            minutesPicker.setValue((int) valuesSavingManager.getLastTime().getMinutes());
            secondsPicker.setValue((int) valuesSavingManager.getLastTime().getSeconds());
            iterationPicker.setValue(valuesSavingManager.getIterations());

            startTimerButton = bottomSheetDialog.findViewById(R.id.start_timer);

            secTimer = new SecTimer();

            showBottomSheetButton.setOnClickListener(view -> bottomSheetTimer.bottomSheetDialog.show());

            assert startTimerButton != null;
            startTimerButton.setOnClickListener(view -> {
                valuesSavingManager.setLastTime(minutesPicker.getValue(), secondsPicker.getValue());
                valuesSavingManager.setIterations(iterationPicker.getValue());
                secTimer.start();
                bottomSheetDialog.cancel();
            });

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
                connectedThread.sendData("timer:stop#");
            });

            btn.setOnClickListener(view1 -> {
                switch (state) {
                    case 1: //on
                        if (bottomSheetTimer.secTimer.isPreheating) {
                            displayAlert();
                        } else {
                            connectedThread.sendData("relay:off#");
                        }
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
                case 7: //preheat finished or not active
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
                    if (bottomSheetTimer.secTimer.isPlaying) {
                        return;
                    }
                    this.state = 1;
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                    btn.setText("Выключить");
                    offBtn.setVisibility(View.GONE);
                    if (!bottomSheetTimer.secTimer.isPreheating) {
                        bottomSheetTimer.enableButton();
                    }

                    break;
                case 3: //timer started
                    if (bottomSheetTimer.secTimer.isPreheating) {
                        return;
                    }
                    bottomSheetTimer.secTimer.stopPreheat();
                    bottomSheetTimer.secTimer.startTimer();
                    bottomSheetTimer.secTimer.showTimer();
                    this.state = 3;
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));
                    btn.setText("Пауза");
                    offBtn.setVisibility(View.VISIBLE);
                    bottomSheetTimer.disableButton();
                    break;
                case 4: //timer is not active
                    if (bottomSheetTimer.secTimer.isPreheating) {
                        return;
                    }
                    bottomSheetTimer.secTimer.stopTimer();
                    bottomSheetTimer.secTimer.hideTimer();
                    this.state = 0;
                    btn.setText("Включить");
                    offBtn.setVisibility(View.GONE);
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    bottomSheetTimer.enableButton();
                    break;
                case 5: //timer paused
                    this.state = 4;
                    bottomSheetTimer.secTimer.startTimer();
                    bottomSheetTimer.secTimer.showTimer();
                    bottomSheetTimer.secTimer.pauseTimer();
                    btn.setText("Пуск");
                    offBtn.setVisibility(View.VISIBLE);
                    btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    bottomSheetTimer.disableButton();
                    break;
                case 6: //preheat started
                    bottomSheetTimer.secTimer.runPreheat();
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
                bottomSheetTimer.secTimer.stopPreheat();
                connectedThread.sendData("timer:preheatstop#");
            });

            builder.setNegativeButton(Html.fromHtml("<font color='#0bbdff'>Отменить</font>"), (dialog, which) -> {});

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    private void setTimerViewTime(long iterationTimeMillis, long millis, int iterations) {
        int sec = (int) (millis / 1000);
        int min = sec / 60;
        sec = sec % 60;
        timerTextView.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));
        timerDialView.setCurrentTime(iterationTimeMillis * (iterations - 1) + millis);
    }

}