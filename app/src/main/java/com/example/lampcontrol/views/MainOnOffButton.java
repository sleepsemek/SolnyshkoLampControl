package com.example.lampcontrol.views;

import static com.example.lampcontrol.Models.ReceivedLampState.RelayState.OFF;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.lampcontrol.Models.ReceivedLampState;
import com.example.lampcontrol.R;

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
        post(new Runnable() {
            @Override
            public void run() {
                switch (state) {
                    case OFF:
                        setTextBelow("Вкл");
                        MainOnOffButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));
                        break;

                    case ACTIVE:
                    case PREHEATING:
                    case PAUSED:
                        setTextBelow("Выкл");
                        MainOnOffButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));
                        break;

                    case ON:
                        setTextBelow("Выкл");
                        MainOnOffButton.this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));
                        break;

                }
            }
        });
    }

    private void setTextBelow(String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }
}
