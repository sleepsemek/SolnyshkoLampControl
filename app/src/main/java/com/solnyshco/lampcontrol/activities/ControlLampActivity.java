package com.solnyshco.lampcontrol.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.solnyshco.lampcontrol.R;
import com.solnyshco.lampcontrol.models.POJO.ReceivedLampState;
import com.solnyshco.lampcontrol.presenters.ControlLampPresenter;
import com.solnyshco.lampcontrol.ui.views.LampInfoBottomSheet;
import com.solnyshco.lampcontrol.ui.views.LampTimerBottomSheet;
import com.solnyshco.lampcontrol.ui.views.MainControlButton;
import com.solnyshco.lampcontrol.ui.views.MainOnOffButton;
import com.solnyshco.lampcontrol.ui.views.TimerView;
import com.solnyshco.lampcontrol.views.ControlLampView;

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
    private LampInfoBottomSheet infoBottomSheet;
    private AlertDialog alertDialog;
    private TimerView timerDialView;
    private TextView timerTextView;

    private MainControlButton mainButton;
    private MainOnOffButton onOffButton;
    private ConstraintLayout onOffButtonHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_lamp);

        timerBottomSheet = new LampTimerBottomSheet(this);
        infoBottomSheet = new LampInfoBottomSheet(this);

        infoBottomSheet.getCancelButton().setOnClickListener(view -> controlLampPresenter.handleInfoBottomSheetCancelButton());

        infoBottomSheet.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                controlLampPresenter.handleInfoBottomSheetCancelButton();
            }
        });
        lampName = findViewById(R.id.lampName);

        mainButton = findViewById(R.id.main_button);
        onOffButton = findViewById(R.id.onOffBtn);
        onOffButton.setTextView(findViewById(R.id.onOffBtnTextView));
        onOffButtonHolder = findViewById(R.id.onOffBtnHolder);
        timerDialView = findViewById(R.id.timerDialView);
        timerTextView = findViewById(R.id.timerTextView);

        timerTextView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                timerTextView.getLayoutParams().height = timerTextView.getWidth();
                timerTextView.requestLayout();
                timerTextView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

        mainButton.setOnClickListener(view -> controlLampPresenter.handleButtonClick(mainButton.getState()));

        timerBottomSheet.setOnStartClickListener(view -> controlLampPresenter.handleBottomSheetStart(timerBottomSheet.getMinutes(), timerBottomSheet.getSeconds(), timerBottomSheet.getCycles()));
        timerBottomSheet.setOnInfoClickListener(view -> controlLampPresenter.handleBottomSheetInfo());

        onOffButton.setOnClickListener(view -> controlLampPresenter.handleSideButtonClick(mainButton.getState()));

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
    public void startLoading(String name) {
        runOnUiThread(() -> {
            //TODO: Убрать надпись с кнопки во время загрузки
            lampName.setText(name);
            timerBottomSheet.cancel();
            timerTextView.setText("Выполняется подключение");
            timerDialView.clearView();
            mainButton.hideButton();
            onOffButtonHolder.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void stopLoading(String name) {
        runOnUiThread(() -> {
            lampName.setText(name);
            timerTextView.setText("Устройство готово");
            mainButton.showButton();
            onOffButtonHolder.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void drawTimerView(int minutes, int seconds, int iterations, int totalMinutes, int totalSeconds, int totalIterations) {
        runOnUiThread(() -> {
            timerDialView.setDialColor(ContextCompat.getColor(getApplicationContext(), R.color.main_blue));
            timerTextView.setText("Процедура " + (iterations) + "/" + totalIterations + "\nОсталось " + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            timerDialView.setTime(minutes, seconds, totalMinutes, totalSeconds);
        });
    }

    @Override
    public void drawPreheatView(int minutes, int seconds) {
        runOnUiThread(() -> {
            timerDialView.setDialColor(ContextCompat.getColor(getApplicationContext(), R.color.main_red));
            timerTextView.setText("Идет прогрев устройства\nОсталось " + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            timerDialView.setTime(minutes, seconds, 0, 60);
        });
    }

    @Override
    public void clearTimerView() {
        runOnUiThread(() -> {
            timerTextView.setText("Устройство готово");
            timerDialView.clearView();
        });

    }

    @Override
    public void showAlertDialog() {
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Предупреждение")
                .setMessage(R.string.off_warning).
                setPositiveButton(Html.fromHtml("<font color='#e31e24'>Отключить в любом случае</font>"), (dialog, which) -> controlLampPresenter.handleAlertConfirm())
                .setNegativeButton(Html.fromHtml("<font color='#0bbdff'>Отменить</font>"), (dialog, which) -> controlLampPresenter.handleAlertCancel()).create();

        alertDialog.show();
    }

    @Override
    public void hideAlertDialog() {
        if (alertDialog == null) return;
        alertDialog.cancel();
    }

    @Override
    public void makeMessage(String msg) {
        Toast.makeText(this, "Версия прошивки: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLampInfo(String name, String address, String version) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                infoBottomSheet.setInfo(name, address, version);
                infoBottomSheet.show();
            }
        });

    }

    @Override
    public void hideLampInfo() {
        infoBottomSheet.hide();
    }
}