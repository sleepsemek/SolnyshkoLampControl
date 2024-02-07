package com.solnyshco.lampcontrol.ui.views;

import android.content.Context;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.solnyshco.lampcontrol.R;

import java.util.Objects;

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
        Objects.requireNonNull(iterationPicker).setMinValue(1);
        iterationPicker.setMaxValue(10);

        minutesPicker = this.findViewById(R.id.minutes_picker);
        Objects.requireNonNull(minutesPicker).setMinValue(0);
        minutesPicker.setMaxValue(30);

        secondsPicker = this.findViewById(R.id.seconds_picker);
        Objects.requireNonNull(secondsPicker).setMinValue(0);
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

    public void setTimeAndCycles(long minutes, long seconds, int iterations) {
        minutesPicker.setValue((int) minutes);
        secondsPicker.setValue((int) seconds);
        iterationPicker.setValue(iterations);
    }

    public int getMinutes() {
        return minutesPicker.getValue();
    }

    public int getSeconds() {
        return  secondsPicker.getValue();
    }

    public int getCycles() {
        return iterationPicker.getValue();
    }

}
