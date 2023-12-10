package com.example.lampcontrol.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.lampcontrol.models.POJO.ReceivedLampState;
import com.example.lampcontrol.models.POJO.SentCommand;
import com.example.lampcontrol.R;
import com.example.lampcontrol.presenters.ControlLampPresenter;
import com.example.lampcontrol.ui.views.LampTimerBottomSheet;
import com.example.lampcontrol.ui.views.MainControlButton;
import com.example.lampcontrol.ui.views.MainOnOffButton;
import com.example.lampcontrol.ui.views.TimerView;
import com.example.lampcontrol.views.ControlLampView;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class ControlLampActivity extends MvpAppCompatActivity implements ControlLampView {

    @InjectPresenter
    ControlLampPresenter controlLampPresenter;

    @ProvidePresenter
    ControlLampPresenter provideControlLampPresenter() {
        return new ControlLampPresenter(getIntent().getStringExtra("address"), getIntent().getStringExtra("name"));
    }

    private TextView lampName;
    private LampTimerBottomSheet timerBottomSheet;
    private AlertDialog alertDialog;
    private TimerView timerDialView;
    private TextView timerTextView;

    private MainControlButton mainButton;
    private MainOnOffButton onOffButton;
    private LinearLayout onOffButtonHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_lamp);

        timerBottomSheet = new LampTimerBottomSheet(this);
        lampName = findViewById(R.id.lampName);

        mainButton = findViewById(R.id.main_button);
        onOffButton = findViewById(R.id.onOffBtn);
        onOffButton.setTextView(findViewById(R.id.onOffBtnTextView));
        onOffButtonHolder = findViewById(R.id.onOffBtnHolder);
        timerDialView = findViewById(R.id.timerDialView);
        timerTextView = findViewById(R.id.timerTextView);

        mainButton.setOnClickListener(view -> {
            controlLampPresenter.handleButtonClick(mainButton.getState());
        });

        timerBottomSheet.setOnStartClickListener(view -> {
            controlLampPresenter.handleBottomSheetStart(timerBottomSheet.getMinutes(), timerBottomSheet.getSeconds(), timerBottomSheet.getCycles());
        });

        onOffButton.setOnClickListener(view -> {
            controlLampPresenter.handleSideButtonClick(mainButton.getState());
        });

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void setButtonsState(ReceivedLampState.RelayState state) {
        mainButton.setState(state);
        onOffButton.setState(state);
    }

    @Override
    public void openTimerBottomSheet(int minutes, int seconds, int cycles) {
        timerBottomSheet.setTimeAndCycles(minutes, seconds, cycles);
        timerBottomSheet.show();
    }

    @Override
    public void closeTimerBottomSheet() {
        timerBottomSheet.cancel();
    }

    @Override
    public void startLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lampName.setText("Установка соединения");
                timerTextView.setText("");
                timerDialView.clearView();
                mainButton.hideButton();
                onOffButtonHolder.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void stopLoading(String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lampName.setText(name);
                timerTextView.setText("Устройство готово");
                mainButton.showButton();
                onOffButtonHolder.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void drawTimerView(int minutes, int seconds, int iterations, int totalMinutes, int totalSeconds, int totalIterations) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerDialView.setDialColor(ContextCompat.getColor(getApplicationContext(), R.color.main_blue));
                timerTextView.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + "\nПроцедура " + (iterations) + "/" + totalIterations);
                timerDialView.setTime(minutes, seconds, totalMinutes, totalSeconds);
            }
        });
    }

    @Override
    public void drawPreheatView(int minutes, int seconds) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerDialView.setDialColor(ContextCompat.getColor(getApplicationContext(), R.color.main_red));
                timerTextView.setText(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
                timerDialView.setTime(minutes, seconds, 0, 60);
            }
        });
    }

    @Override
    public void clearTimerView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerTextView.setText("Устройство готово");
                timerDialView.clearView();
            }
        });

    }

    @Override
    public void showAlertDialog() {
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Предупреждение")
                .setMessage(R.string.off_warning).
                setPositiveButton(Html.fromHtml("<font color='#e31e24'>Отключить в любом случае</font>"), (dialog, which) -> {
                    controlLampPresenter.handleAlertConfirm();
                })
                .setNegativeButton(Html.fromHtml("<font color='#0bbdff'>Отменить</font>"), (dialog, which) -> {
                    controlLampPresenter.handleAlertCancel();
                }).create();

        alertDialog.show();
    }

    @Override
    public void hideAlertDialog() {
        if (alertDialog == null) return;
        alertDialog.cancel();
    }

}