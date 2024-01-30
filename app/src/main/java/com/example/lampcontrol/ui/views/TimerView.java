package com.example.lampcontrol.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.UiThread;
import androidx.core.content.ContextCompat;

import com.example.lampcontrol.R;
import com.example.lampcontrol.activities.ControlLampActivity;

public class TimerView extends View {

    private Paint backgroundPaint;
    private Paint currentPaint;
    private Paint circlePaint;

    private float maxTime = 0;
    private float sweepAngle = 0;

    private int centerX;
    private int centerY;
    private int radius;

    public TimerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(context.obtainStyledAttributes(R.style.Theme_LampControl, new int[]{com.google.android.material.R.attr.colorSecondaryContainer}).getColor(0, 0));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStrokeWidth(80);

        currentPaint = new Paint();
        currentPaint.setColor(ContextCompat.getColor(context, R.color.main_blue));
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeWidth(80);
        currentPaint.setAntiAlias(true);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);

        circlePaint = new Paint();
        circlePaint.setColor(ContextCompat.getColor(context, R.color.dark_blue));
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(centerX, centerY);
        canvas.rotate(-90);
//        canvas.scale(1f, -1f, 0, 0);

        canvas.drawCircle(0, 0, radius - 40, backgroundPaint);
        canvas.drawCircle(0, 0, radius - 100, circlePaint);

        RectF rectF = new RectF(40 - radius, 40 - radius, radius - 40, radius - 40);
        canvas.drawArc(rectF, 0, sweepAngle, false, currentPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        int dimen;

        if ((height - width) < 650) {
            dimen = height - 650;
        } else {
            dimen = width;
        }

        centerX = dimen / 2;
        centerY = dimen / 2;
        radius = dimen / 2;

        setMeasuredDimension(dimen, dimen);
    }

    public void setTime(int minutes, int seconds, int totalMinutes, int totalSeconds) {
        this.maxTime = convertSecondsAndMinutesToMillis(totalMinutes, totalSeconds);
        this.sweepAngle = (convertSecondsAndMinutesToMillis(minutes, seconds) / maxTime) * 360;
        invalidate();
    }

    public void clearView() {
        this.maxTime = 0;
        this.sweepAngle = 0;
        invalidate();
    }

    @UiThread
    public void setDialColor(int color) {
        currentPaint.setColor(color);
        invalidate();
    }

    private long convertSecondsAndMinutesToMillis(int minutes, int seconds) {
        return minutes * 60000L + seconds * 1000L;
    }

}
