package com.example.lampcontrol;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

public class TimerView extends View {

    private Paint outlinePaint;
    private Paint stripesPaint;
    private Paint currentPaint;
    private Paint backgroundPaint;

    private float maxTime = 0;
    private float sweepAngle = 0;
    private int stripes = 1;

    private int centerX;
    private int centerY;
    private int radius;

    public TimerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.outlinePaint = new Paint();

        stripesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stripesPaint.setColor(context.obtainStyledAttributes(R.style.Theme_LampControl, new int[]{R.attr.colorSecondary}).getColor(0, 0));
        stripesPaint.setStyle(Paint.Style.STROKE);
        stripesPaint.setStrokeWidth(12);

        currentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentPaint.setColor(context.obtainStyledAttributes(R.style.Theme_LampControl, new int[]{R.attr.colorOnSecondary}).getColor(0, 0));
        currentPaint.setStyle(Paint.Style.FILL);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(context.obtainStyledAttributes(R.style.Theme_LampControl, new int[]{R.attr.colorSecondary}).getColor(0, 0));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(12);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(context.obtainStyledAttributes(R.style.Theme_LampControl, new int[]{R.attr.colorOnPrimary}).getColor(0, 0));
        backgroundPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(centerX, centerY);
        canvas.rotate(-90);

        canvas.drawCircle(0, 0, radius - 6, backgroundPaint);

        Path path = new Path();
        path.moveTo(0, 0);
        path.arcTo(-radius, -radius, radius, radius, 0, sweepAngle, true);
        path.lineTo(0, 0);
        path.close();

        canvas.drawPath(path, currentPaint);

        float angle = (float) (Math.PI * 2 / stripes);
        for (int i = 1; i <= stripes; i++) {
            int startX = (int) (radius * Math.cos(i * angle));
            int startY = (int) (radius * Math.sin(i * angle));
            canvas.drawLine(0, 0, startX, startY, stripesPaint);
        }

        canvas.drawCircle(0, 0, radius - 6, outlinePaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        System.out.println(width + "  " + height);

        int dimen = 0;

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

    public void setBounds(float time, int amount) {
        this.maxTime = time;
        this.stripes = amount;
        invalidate();
    }

    public void setCurrentTime(float currentTime) {
        this.sweepAngle = (currentTime / maxTime) * 360;
        invalidate();
    }

}
