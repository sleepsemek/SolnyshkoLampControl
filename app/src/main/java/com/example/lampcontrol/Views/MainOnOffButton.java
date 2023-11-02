package com.example.lampcontrol.Views;

import static com.example.lampcontrol.Models.ReceivedLampState.RelayState.OFF;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.lampcontrol.Models.ReceivedLampState;
import com.example.lampcontrol.R;

public class MainOnOffButton extends AppCompatButton {

    private ReceivedLampState.RelayState state = OFF;
    private Context context;
    private String text = "Вкл";
    private Paint paint;

    public MainOnOffButton(@NonNull Context context) {
        super(context);
        this.context = context;
        this.paint = new Paint();
    }

    public MainOnOffButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.paint = new Paint();
    }

    public MainOnOffButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.paint = new Paint();
    }

    public ReceivedLampState.RelayState getState() {
        return state;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(getCurrentTextColor());
        paint.setTextSize(getTextSize());
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        float textX = (getWidth() - paint.measureText(text)) / 2;
        float textY = getHeight() + getTextSize();

        canvas.drawText(text, textX, textY, paint);
    }

    public void setState(ReceivedLampState.RelayState state) {
        this.state = state;
        switch (state) {
            case OFF:
                text = "Вкл";
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));
                break;

            case ON:
                text = "Выкл";
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));

                break;

            case ACTIVE:
                text = "Пауза";
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_blue)));

                break;

            case PAUSED:
                text = "Пуск";
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.main_red)));

                break;

            case PREHEATING:
                text = "Прогрев";
                this.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context.getApplicationContext(), R.color.dark_blue)));

                break;

        }
    }
}
