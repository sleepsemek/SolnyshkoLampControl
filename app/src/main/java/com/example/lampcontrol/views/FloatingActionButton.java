package com.example.lampcontrol.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.lampcontrol.R;

public class FloatingActionButton extends AppCompatButton implements OnClickListener {

    private ValueAnimator animator;
    private Context context;
    private boolean isToggled = false;

    public FloatingActionButton(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        animator = ValueAnimator.ofFloat(1f, 1.2f);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                FloatingActionButton.this.setScaleX(scale);
                FloatingActionButton.this.setScaleY(scale);
            }
        });

        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
    }

    @Override
    public void onClick(View view) {
        if (isToggled) {
            toggleOff();
        } else {
            toggleOn();
        }
        startRotatingAnimation();
    }

    public void toggleOn() {
        this.setForeground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_lightbulb_48));
        cancelBreathingAnimation();
        startRotatingAnimation();
        isToggled = true;
    }

    public void toggleOff() {
        this.setForeground(ContextCompat.getDrawable(context, R.drawable.ic_baseline_add_48));
        startRotatingAnimation();
        isToggled = false;
    }

    private void startRotatingAnimation() {
        this.animate().rotation(360).setDuration(500).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                FloatingActionButton.this.setRotation(0);
            }
        });
    }

    public void startBreathingAnimation() {
        animator.start();
    }

    public void cancelBreathingAnimation() {
        animator.cancel();
        this.setScaleX(1f);
        this.setScaleY(1f);
    }

    public boolean isToggled() {
        return isToggled;
    }
}
