package com.example.lampcontrol.Views;

import static com.example.lampcontrol.Models.ReceivedLampState.RelayState.OFF;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.lampcontrol.Models.ReceivedLampState;
import com.example.lampcontrol.R;

public class MainControlButton extends AppCompatButton implements View.OnClickListener {

    private ReceivedLampState.RelayState state = OFF;
    private Context context;

    public MainControlButton(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public MainControlButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MainControlButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void onClick(View v) {

    }

    public ReceivedLampState.RelayState getState() {
        return state;
    }

    public void setState(ReceivedLampState.RelayState state) {
        this.state = state;
        switch (state) {
            case OFF:
                this.setText("Таймер");
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));

                break;

            case ON:
                this.setText("Выкл");
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));

                break;

            case ACTIVE:
                this.setText("Пауза");
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));
                break;


            case PAUSED:
                this.setText("Пуск");
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));

                break;

            case PREHEATING:
                this.setText("Прогрев");
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.dark_blue)));

                break;

        }
    }

    public void startRotatingAnimation() {
        this.animate().rotationBy(360000).setDuration(500000).setInterpolator(new LinearInterpolator()).start();
    }

    public void stopRotatingAnimation() {
        this.animate().rotation(0).setDuration(0).setInterpolator(new DecelerateInterpolator());
        this.clearAnimation();
    }

    public void hideButton() {
        this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));
        this.setClickable(false);
        this.setText("");
        startRotatingAnimation();
    }

    public void showButton() {
        this.setClickable(true);
        stopRotatingAnimation();
    }
}
