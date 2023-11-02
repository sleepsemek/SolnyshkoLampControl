package com.example.lampcontrol.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.lampcontrol.Models.ReceivedLampState;
import com.example.lampcontrol.Models.SentCommand;
import com.example.lampcontrol.R;
import com.example.lampcontrol.Utils.BluetoothConnectionThread;
import com.example.lampcontrol.Utils.ValuesSavingManager;
import com.example.lampcontrol.Views.MainControlButton;
import com.example.lampcontrol.Views.MainOnOffButton;
import com.example.lampcontrol.Views.TimerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
        hideInterface();
        bottomSheetTimer.secTimer.stopPreheat();
        bottomSheetTimer.secTimer.stopTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        connectedThread.cancel();
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

        connectedThread.setOnCommandReceivedListener(lampState -> {
            buttons.setState(lampState.getState());
        });

    }

    private void hideInterface() {
        lampName.setText("Установка соединения");
        buttons.hide();

        bottomSheetTimer.secTimer.stopPreheat();
        bottomSheetTimer.secTimer.stopTimer();

    }

    private void showInterface() {
        lampName.setText(name);
        buttons.show();
    }

    private class SecTimer {
        private CountDownTimer preheatTimer;
        private CountDownTimer mainTimer;

        private int iterations;
        private long iterationTimeMillis;

        public SecTimer() {
            iterations = valuesSavingManager.getIterations();
            iterationTimeMillis = ((valuesSavingManager.getLastTime().getMinutes() * 60) + valuesSavingManager.getLastTime().getSeconds()) * 1000;
        }

        private void start() {
            iterations = valuesSavingManager.getIterations();
            iterationTimeMillis = ((valuesSavingManager.getLastTime().getMinutes() * 60) + valuesSavingManager.getLastTime().getSeconds()) * 1000;
        }

        private void beginDeviceTimer() {
            connectedThread.sendCommand(new SentCommand("set", (iterationTimeMillis / 1000 * iterations)));
        }

        private void setPreheatTimerTime(long millis) {
            if (millis == 1) {
                return;
            }

            timerDialView.setBounds(60000, 1);

            preheatTimer = new CountDownTimer(millis, 1000) {
                @Override
                public void onTick(long l) {
                    setTimerViewTime(60000, l, 1);
                }

                @Override
                public void onFinish() {
                    connectedThread.sendCommand(new SentCommand(ReceivedLampState.RelayState.OFF.ordinal()));
                    stopPreheat();
                    bottomSheetTimer.secTimer.beginDeviceTimer();
                }
            };

        }

        private void runPreheat() {
            if (preheatTimer != null) {
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
        }

        private void cancelPreheat() {
            if (preheatTimer != null) {
                preheatTimer.cancel();
            }
        }

        private void setTimerTime(long millis) {
            millis -= 10;
            if (millis <= 0) {
                return;
            }

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
//                        connectedThread.sendCommand("timer:settimer:" + (iterationTimeMillis / 1000 * remainedIterations) + "#");
//                        connectedThread.sendCommand("timer:pause#");
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
            }

        }

        public void stopTimer() {
            if (mainTimer != null) {
                mainTimer.cancel();
            }
            timerDialView.setBounds(0, 1);
            setTimerViewTime(0, 0, 0);
        }

        public void pauseTimer() {
            if (mainTimer != null) {
                mainTimer.cancel();
            }
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

        private final MainControlButton mainButton;
        private final MainOnOffButton onOffButton;
        private final LinearLayout onOffButtonHolder;

        private final Context context;

        public Buttons(Context context) {
            mainButton = findViewById(R.id.main_button);
            onOffButton = findViewById(R.id.onOffBtn);
            onOffButtonHolder = findViewById(R.id.onOffBtnHolder);

            this.context = context;

            mainButton.setOnClickListener(view -> {
                switch (mainButton.getState()) {
                    case OFF:
                    case ON:
                        bottomSheetTimer.bottomSheetDialog.show();
                        break;
                    case ACTIVE:
                        connectedThread.sendCommand(new SentCommand("pause"));
                        break;
                    case PAUSED:
                        connectedThread.sendCommand(new SentCommand("resume"));
                        break;
                    case PREHEATING:
                        break;

                }
            });

            onOffButton.setOnClickListener(view1 -> {
                switch (mainButton.getState()) {
                    case ON:
                        connectedThread.sendCommand(new SentCommand(ReceivedLampState.RelayState.OFF.ordinal()));
                        break;

                    case OFF:
                        connectedThread.sendCommand(new SentCommand(ReceivedLampState.RelayState.ON.ordinal()));
                        break;

                    case ACTIVE:
                    case PAUSED:
                        connectedThread.sendCommand(new SentCommand("stop"));
                        break;

                    case PREHEATING:
                        displayAlert();
                        break;
                }
            });
        }

        public void setState(ReceivedLampState.RelayState state) {
            mainButton.setState(state);
            onOffButton.setState(state);
            switch (state) {
                case OFF:
                    bottomSheetTimer.secTimer.stopPreheat();
                    bottomSheetTimer.secTimer.stopTimer();
                    break;

                case ON:
                    break;

                case ACTIVE:
                    bottomSheetTimer.secTimer.startTimer();
                    break;

                case PAUSED:
                    bottomSheetTimer.secTimer.pauseTimer();
                    break;

                case PREHEATING:
                    bottomSheetTimer.secTimer.runPreheat();
                    break;

            }

        }

        private void hide() {
            mainButton.hideButton();
            onOffButtonHolder.setVisibility(View.INVISIBLE);
        }

        private void show() {
            mainButton.showButton();
            onOffButtonHolder.setVisibility(View.VISIBLE);
        }

        private void displayAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Предупреждение");
            builder.setMessage(R.string.off_warning);

            builder.setPositiveButton(Html.fromHtml("<font color='#e31e24'>Отключить в любом случае</font>"), (dialog, which) -> {
                connectedThread.sendCommand(new SentCommand(ReceivedLampState.RelayState.OFF.ordinal()));
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