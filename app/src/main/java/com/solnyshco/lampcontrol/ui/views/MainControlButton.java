package com.solnyshco.lampcontrol.ui.views;

import static com.solnyshco.lampcontrol.models.POJO.ReceivedLampState.RelayState.OFF;

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

import com.solnyshco.lampcontrol.R;
import com.solnyshco.lampcontrol.models.POJO.ReceivedLampState;

public class MainControlButton extends AppCompatButton implements View.OnClickListener {

    private ReceivedLampState.RelayState state = OFF;
    private final Context context;

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
        post(() -> {
            switch (state) {
                case ON:
                case OFF:
                    MainControlButton.this.setText("Таймер");
                    MainControlButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));

                    break;

                case ACTIVE:
                    MainControlButton.this.setText("Пауза");
                    MainControlButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.dark_blue)));
                    break;


                case PAUSED:
                    MainControlButton.this.setText("Пуск");
                    MainControlButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));

                    break;

                case PREHEATING:
                    MainControlButton.this.setText("");
                    MainControlButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.dark_blue)));

                    break;

            }
        });
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
