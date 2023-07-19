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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.lampcontrol.BluetoothConnectionThread;
import com.example.lampcontrol.R;
import com.example.lampcontrol.TimerView;
import com.example.lampcontrol.ValuesSavingManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.nio.file.FileSystemLoopException;

public class ControlLampActivity extends AppCompatActivity {

    private BluetoothConnectionThread connectedThread;

    private String address;
    private String name;

    private TextView lampName;
    private TextView timerTextView;
    private TimerView timerDialView;

    private Buttons buttons;

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
        buttons = new Buttons(this);

        lampName = findViewById(R.id.lampName);
        timerTextView = findViewById(R.id.timerTextView);
        timerDialView = findViewById(R.id.timerDialView);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (address != null) {
            connectedThread.cancelRunning();
        }
        bottomSheetTimer.secTimer.stopPreheat();
        bottomSheetTimer.secTimer.stopTimer();
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
                buttons.setState(command);
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
        buttons.hide();

        if (bottomSheetTimer.secTimer.isPreheating) {
            bottomSheetTimer.secTimer.stopPreheat();
        } else if (bottomSheetTimer.secTimer.isPlaying) {
            bottomSheetTimer.secTimer.stopTimer();
        }

    }

    private void showInterface() {
        lampName.setText(name);
        buttons.show();
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

        private void beginDeviceTimer() {
            connectedThread.sendData("timer:settimer:" + (iterationTimeMillis / 1000 * iterations) + "#");
        }

        private void setPreheatTimerTime(long millis) {
            if (millis == 0) {
                return;
            }
            isPreheating = true;

            timerDialView.setBounds(60000, 1);

            preheatTimer = new CountDownTimer(millis, 1000) {
                @Override
                public void onTick(long l) {
                    setTimerViewTime(60000, l, 1);
                }

                @Override
                public void onFinish() {
                    connectedThread.sendData("timer:preheatstop#");
                    stopPreheat();
                    bottomSheetTimer.secTimer.beginDeviceTimer();
                }
            };

        }

        private void runPreheat() {
            if (preheatTimer != null) {
                isPreheating = true;
                preheatTimer.start();
            } else {
                this.stopPreheat();
            }
        }

        private void stopPreheat() {
            if (preheatTimer != null) {
                preheatTimer.cancel();
            }

            timerDialView.setBounds(0, 1);
            setTimerViewTime(0, 0, 0);
            isPreheating = false;
        }

        private void cancelPreheat() {
            if (preheatTimer != null) {
                preheatTimer.cancel();
            }
            isPreheating = false;
        }

        private void setTimerTime(long millis) {
            millis -= 10;
            if (millis == 0) {
                return;
            }
            isPlaying = true;

            int remainedIterations = (int) Math.floor(millis / (float) iterationTimeMillis);
            long remainedTime = millis % iterationTimeMillis;


            timerDialView.setBounds(iterationTimeMillis * iterations, iterations);
            setTimerViewTime(iterationTimeMillis, remainedTime, remainedIterations + 1);

            if (mainTimer != null) {
                mainTimer.cancel();
            }
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
            if (preheatTimer != null) {
                preheatTimer.cancel();
            }

            if (mainTimer != null) {
                mainTimer.start();
                timerDialView.setBounds(iterationTimeMillis * iterations, iterations);
                isPlaying = true;
            }

        }

        public void stopTimer() {
            if (mainTimer != null) {
                mainTimer.cancel();
            }
            timerDialView.setBounds(0, 1);
            setTimerViewTime(0, 0, 0);
            isPlaying = false;
        }

        public void pauseTimer() {
            if (mainTimer != null) {
                mainTimer.cancel();
            }
            isPlaying = true;
        }

    }

    private class BottomSheetTimer {

        private final BottomSheetDialog bottomSheetDialog;

        private final NumberPicker iterationPicker;
        private final NumberPicker minutesPicker;
        private final NumberPicker secondsPicker;

        private final AppCompatButton startTimerButton;

        private final SecTimer secTimer;

        public BottomSheetTimer(Context context) {
            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(R.layout.lamp_bottom_sheet);

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

            assert startTimerButton != null;
            startTimerButton.setOnClickListener(view -> {
                if (minutesPicker.getValue() + secondsPicker.getValue() == 0) {
                    Toast.makeText(context, "Установите длительность таймера", Toast.LENGTH_SHORT).show();
                    return;
                }
                valuesSavingManager.setLastTime(minutesPicker.getValue(), secondsPicker.getValue());
                valuesSavingManager.setIterations(iterationPicker.getValue());
                secTimer.start();
                bottomSheetDialog.cancel();
            });

        }

    }

    private class Buttons {
        private final AppCompatButton mainButton;

        private final Button onOffButton;
        private final LinearLayout onOffButtonHolder;
        private final TextView onOffButtonText;

        private int state = 0;
        private final Context context;

        public Buttons(Context context) {
            mainButton = findViewById(R.id.main_button);

            onOffButton = findViewById(R.id.onOffBtn);
            onOffButtonHolder = findViewById(R.id.onOffBtnHolder);
            onOffButtonText = findViewById(R.id.onOffBtnText);

            this.context = context;

            mainButton.setOnClickListener(view -> {
                switch (state) {
                    case 0:
                    case 1: //relay is on/idle
                        if (bottomSheetTimer.secTimer.isPlaying || bottomSheetTimer.secTimer.isPreheating) {
                            return;
                        }

                        bottomSheetTimer.bottomSheetDialog.show();
                        break;

                    case 3: //timer playing
                        connectedThread.sendData("timer:pause#");
                        break;
                    case 4: //timer paused
                        connectedThread.sendData("timer:resume#");
                        break;
                    case 5: //preheating
                        break;

                }
            });

            onOffButton.setOnClickListener(view1 -> {
                switch (state) {
                    case 1: //on
                        if (bottomSheetTimer.secTimer.isPlaying || bottomSheetTimer.secTimer.isPreheating) {
                            return;
                        }

                        connectedThread.sendData("relay:off#");
                        break;

                    case 0: //idle
                        if (bottomSheetTimer.secTimer.isPlaying || bottomSheetTimer.secTimer.isPreheating) {
                            return;
                        }

                        connectedThread.sendData("relay:on#");
                        break;

                    case 3: //timer playing
                    case 4: //timer paused
                        if (bottomSheetTimer.secTimer.isPreheating) {
                            displayAlert();
                        } else if (bottomSheetTimer.secTimer.isPlaying) {
                            connectedThread.sendData("timer:stop#");
                        }
                        break;

                    case 5: //preheating
                        displayAlert();
                        break;
                }
            });
        }

        public void setState(int state) {
            switch (state) {
                case 2: //turned off
                    if (bottomSheetTimer.secTimer.isPlaying || bottomSheetTimer.secTimer.isPreheating) {
                        return;
                    }

                    this.state = 0;

                    bottomSheetTimer.secTimer.stopPreheat();

                    mainButton.setText("Таймер");
                    mainButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));

                    onOffButtonText.setText("Вкл");
                    onOffButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));

                    break;

                case 1: //turned on
                    if (bottomSheetTimer.secTimer.isPlaying || bottomSheetTimer.secTimer.isPreheating) {
                        return;
                    }

                    this.state = 1;

                    onOffButtonText.setText("Выкл");
                    onOffButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));

                    break;

                case 3: //timer started
                    this.state = 3;

                    bottomSheetTimer.secTimer.startTimer();

                    mainButton.setText("Пауза");
                    mainButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));

                    onOffButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    onOffButtonText.setText("Отмена");
                    break;

                case 4: //timer is not active
                    if (bottomSheetTimer.secTimer.isPreheating) {
                        return;
                    }
                    this.state = 0;

                    bottomSheetTimer.secTimer.stopTimer();

                    mainButton.setText("Таймер");
                    mainButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));

                    onOffButtonText.setText("Вкл");
                    onOffButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;

                case 5: //timer paused
                    this.state = 4;

                    bottomSheetTimer.secTimer.pauseTimer();

                    onOffButtonText.setText("Отмена");
                    onOffButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));

                    mainButton.setText("Пуск");
                    mainButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;

                case 6: //preheat started
                    this.state = 5;

                    mainButton.setText("Прогрев");
                    mainButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.dark_blue)));

                    onOffButtonText.setText("Отмена");
                    onOffButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));

                    bottomSheetTimer.secTimer.runPreheat();
                    break;
                case 7: //preheat finished or not active
                    this.state = 0;

                    bottomSheetTimer.secTimer.cancelPreheat();

                    mainButton.setText("Таймер");
                    mainButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_blue)));

                    onOffButtonText.setText("Вкл");
                    onOffButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
                    break;

            }

        }

        private void hide() {
            mainButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.main_red)));
            mainButton.setClickable(false);
            mainButton.setText("");
            onOffButtonHolder.setVisibility(View.INVISIBLE);
            mainButton.animate().rotationBy(360000).setDuration(500000).setInterpolator(new LinearInterpolator()).start();
        }

        private void show() {
            mainButton.clearAnimation();
            mainButton.setClickable(true);
            onOffButtonHolder.setVisibility(View.VISIBLE);
            mainButton.animate().cancel();
            mainButton.animate().rotation(0).setDuration(0).setInterpolator(new DecelerateInterpolator());
        }

        private void displayAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Предупреждение");
            builder.setMessage(R.string.off_warning);

            builder.setPositiveButton(Html.fromHtml("<font color='#e31e24'>Отключить в любом случае</font>"), (dialog, which) -> {
                connectedThread.sendData("timer:preheatstop#");
                connectedThread.sendData("relay:off#");
            });

            builder.setNegativeButton(Html.fromHtml("<font color='#0bbdff'>Отменить</font>"), (dialog, which) -> {});

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    private void setTimerViewTime(long iterationTimeMillis, long millis, int iterations) {
        int sec = (int) ((millis + 50) / 1000);
        int min = sec / 60;
        sec = sec % 60;
        timerTextView.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));
        timerDialView.setCurrentTime(iterationTimeMillis * (iterations - 1) + millis);
    }

}