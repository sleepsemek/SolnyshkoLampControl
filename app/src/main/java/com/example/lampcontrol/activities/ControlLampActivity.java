package com.example.lampcontrol.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lampcontrol.models.POJO.ReceivedLampState;
import com.example.lampcontrol.models.POJO.SentCommand;
import com.example.lampcontrol.R;
import com.example.lampcontrol.repository.BluetoothConnectionThread;
import com.example.lampcontrol.models.LampTimer;
import com.example.lampcontrol.models.ValuesSavingManager;
import com.example.lampcontrol.ui.views.LampTimerBottomSheet;
import com.example.lampcontrol.ui.views.MainControlButton;
import com.example.lampcontrol.ui.views.MainOnOffButton;

public class ControlLampActivity extends AppCompatActivity {

    private BluetoothConnectionThread connectedThread;

    private String address;
    private String name;

    private TextView lampName;

    private Buttons buttons;

    private LampTimer timer;
    private LampTimerBottomSheet timerBottomSheet;
    private ValuesSavingManager valuesSavingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_lamp);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");

        valuesSavingManager = new ValuesSavingManager(this, address);

        timerBottomSheet = new LampTimerBottomSheet(this);
        buttons = new Buttons(this);

        lampName = findViewById(R.id.lampName);
        timer = new LampTimer(findViewById(R.id.timerTextView), findViewById(R.id.timerDialView));

        timerBottomSheet.setTimeAndIterations(valuesSavingManager.getLastTime(), valuesSavingManager.getIterations());

        timerBottomSheet.setOnStartClickListener(view -> {
            valuesSavingManager.setLastTime(timerBottomSheet.getTime());
            int iterations = timerBottomSheet.getIterations();
            int iterationTime = (int) (timerBottomSheet.getTime().getMinutes() * 60000
                                + timerBottomSheet.getTime().getSeconds() * 1000);
            valuesSavingManager.setIterations(iterations);

            beginDeviceTimer(iterations, iterationTime);
            timerBottomSheet.cancel();
        });

    }

    private void beginDeviceTimer(int cycles, long cycleTime) {
        connectedThread.sendCommand(new SentCommand("set", cycleTime, cycles));
    }

    @Override
    public void onPause() {
        super.onPause();
        hideInterface();
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
            buttons.setState(lampState);
        });

    }

    private void hideInterface() {
        lampName.setText("Установка соединения");
        buttons.hide();

        timer.stopPreheat();
        timer.stopTimer();
    }

    private void showInterface() {
        lampName.setText(name);
        buttons.show();
    }

    private class Buttons {

        private final MainControlButton mainButton;
        private final MainOnOffButton onOffButton;
        private final LinearLayout onOffButtonHolder;

        private final Context context;

        public Buttons(Context context) {
            mainButton = findViewById(R.id.main_button);
            onOffButton = findViewById(R.id.onOffBtn);
            onOffButton.setTextView(findViewById(R.id.onOffBtnTextView));
            onOffButtonHolder = findViewById(R.id.onOffBtnHolder);

            this.context = context;

            mainButton.setOnClickListener(view -> {
                switch (mainButton.getState()) {
                    case OFF:
                    case ON:
                        timerBottomSheet.show();
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

        public void setState(ReceivedLampState lampState) {
            mainButton.setState(lampState.getState());
            onOffButton.setState(lampState.getState());
            switch (lampState.getState()) {
                case OFF:
                case ON:
                    timer.stopPreheat();
                    timer.stopTimer();
                    break;

                case ACTIVE:
                    timer.startTimer(lampState.getTimer());
                    break;

                case PAUSED:
                    timer.pauseTimer(lampState.getTimer());
                    break;

                case PREHEATING:
                    timer.startPreheat(lampState.getPreheat());
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

}