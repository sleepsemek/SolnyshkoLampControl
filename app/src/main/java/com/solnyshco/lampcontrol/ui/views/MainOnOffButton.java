package com.solnyshco.lampcontrol.ui.views;

import static com.solnyshco.lampcontrol.models.POJO.ReceivedLampState.RelayState.OFF;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.solnyshco.lampcontrol.R;
import com.solnyshco.lampcontrol.models.POJO.ReceivedLampState;

public class MainOnOffButton extends AppCompatButton {

    private ReceivedLampState.RelayState state = OFF;
    private final Context context;
    private TextView textView;

    public MainOnOffButton(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public MainOnOffButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MainOnOffButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public ReceivedLampState.RelayState getState() {
        return state;
    }


    public void setState(ReceivedLampState.RelayState state) {
        this.state = state;
        post(() -> {
            switch (state) {
                case OFF:
                    setTextBelow("Включить");
                    MainOnOffButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));
                    break;

                case ACTIVE:
                case PREHEATING:
                case PAUSED:
                    setTextBelow("Выключить");
                    MainOnOffButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));
                    break;

                case ON:
                    setTextBelow("Выключить");
                    MainOnOffButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));
                    break;

            }
        });
    }

    private void setTextBelow(String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }
}
