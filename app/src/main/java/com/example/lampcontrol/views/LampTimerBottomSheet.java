package com.example.lampcontrol.views;

import android.content.Context;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.example.lampcontrol.Models.LampTimerTime;
import com.example.lampcontrol.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class LampTimerBottomSheet extends BottomSheetDialog {

    private NumberPicker iterationPicker;
    private NumberPicker minutesPicker;
    private NumberPicker secondsPicker;
    private AppCompatButton startTimerButton;

    public LampTimerBottomSheet(@NonNull Context context) {
        super(context);
        init();
    }

    public LampTimerBottomSheet(@NonNull Context context, int theme) {
        super(context, theme);
        init();
    }

    protected LampTimerBottomSheet(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        this.setContentView(R.layout.timer_bottom_sheet);

        iterationPicker = this.findViewById(R.id.preheat_picker);
        iterationPicker.setMinValue(1);
        iterationPicker.setMaxValue(10);

        minutesPicker = this.findViewById(R.id.minutes_picker);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(30);

        secondsPicker = this.findViewById(R.id.seconds_picker);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);

        startTimerButton = this.findViewById(R.id.start_timer);

    }

    public void setOnStartClickListener(View.OnClickListener onClickListener) {
        startTimerButton.setOnClickListener(view -> {
            if (minutesPicker.getValue() + secondsPicker.getValue() == 0) {
                Toast.makeText(view.getContext(), "Установите длительность таймера", Toast.LENGTH_SHORT).show();
            } else {
                onClickListener.onClick(view);
            }
        });
    }

    public void setTimeAndIterations(LampTimerTime time, int iterations) {
        minutesPicker.setValue((int) time.getMinutes());
        secondsPicker.setValue((int) time.getSeconds());
        iterationPicker.setValue(iterations);
    }

    public LampTimerTime getTime() {
        return new LampTimerTime(minutesPicker.getValue(), secondsPicker.getValue());
    }

    public int getIterations() {
        return iterationPicker.getValue();
    }

}
