package com.example.lampcontrol.Utils;

import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.lampcontrol.Models.ReceivedLampState;
import com.example.lampcontrol.views.TimerView;

public class LampTimer {

    private CountDownTimer preheatTimer;
    private CountDownTimer mainTimer;

    private TextView timerTextView;
    private TimerView timerDialView;

    public LampTimer(TextView timerTextView, TimerView timerDialView) {
        this.timerTextView = timerTextView;
        this.timerDialView = timerDialView;
    }

    public void startPreheat(ReceivedLampState.Preheat preheatData) {
        timerDialView.setBounds(60000, 1);

        preheatTimer = new CountDownTimer(preheatData.getTimeLeft(), 1000) {
            @Override
            public void onTick(long l) {
                setTimerViewTime(60000, l, 1);
            }

            @Override
            public void onFinish() {

            }
        }.start();

    }

    public void stopPreheat() {
        if (preheatTimer != null) {
            preheatTimer.cancel();
        }

        timerDialView.setBounds(0, 1);
        setTimerViewTime(0, 0, 0);
    }

    public void startTimer(ReceivedLampState.Timer timerData) {
        if (preheatTimer != null) {
            preheatTimer.cancel();
        }

        if (mainTimer != null) {
            mainTimer.cancel();
        }

        int generalCycles = timerData.getGeneralCycles();
        int generalCycleTime = timerData.getGeneralCycleTime();
        int remainedTime = timerData.getTimeLeft();
        int generalTime = generalCycles * generalCycleTime;
        int remainedCycleTime = remainedTime % generalCycleTime;
        int remainedCycles = remainedTime / generalCycleTime;

        timerDialView.setBounds(generalTime, generalCycles);
        setTimerViewTime(generalCycleTime, remainedCycleTime, remainedCycles + 1);

        mainTimer = new CountDownTimer(remainedCycleTime, 1000) {
            @Override
            public void onTick(long l) {
                setTimerViewTime(generalCycleTime, l, remainedCycles + 1);
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }

    public void stopTimer() {
        if (mainTimer != null) {
            mainTimer.cancel();
        }
        timerDialView.setBounds(0, 1);
        setTimerViewTime(0, 0, 0);
    }

    public void pauseTimer(ReceivedLampState.Timer timerData) {
        if (preheatTimer != null) {
            preheatTimer.cancel();
        }

        if (mainTimer != null) {
            mainTimer.cancel();
        }

        int generalCycles = timerData.getGeneralCycles();
        int generalCycleTime = timerData.getGeneralCycleTime();
        int remainedTime = timerData.getTimeLeft();
        int generalTime = generalCycles * generalCycleTime;
        int remainedCycleTime = remainedTime % generalCycleTime;
        int remainedCycles = remainedTime / generalCycleTime;

        timerDialView.setBounds(generalTime, generalCycles);
        setTimerViewTime(generalCycleTime, remainedCycleTime, remainedCycles + 1);
    }

    private void setTimerViewTime(long iterationTimeMillis, long millis, int iterations) {
        int sec = (int) ((millis + 50) / 1000);
        int min = sec / 60;
        sec = sec % 60;
        timerTextView.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));
        timerDialView.setCurrentTime(iterationTimeMillis * (iterations - 1) + millis);
    }
}
