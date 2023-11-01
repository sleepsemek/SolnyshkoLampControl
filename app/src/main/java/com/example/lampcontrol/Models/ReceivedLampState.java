package com.example.lampcontrol.Models;

import com.google.gson.annotations.SerializedName;

public class ReceivedLampState {
    private int state;

    @SerializedName("timer")
    private Timer timer;

    @SerializedName("preheat")
    private Preheat preheat;

    public int getState() {
        return state;
    }

    public Timer getTimer() {
        return timer;
    }

    public Preheat getPreheat() {
        return preheat;
    }

    public static class Timer {
        @SerializedName("time_left")
        private int timeLeft;

        public int getTimeLeft() {
            return timeLeft;
        }
    }

    public static class Preheat {
        @SerializedName("time_left")
        private int timeLeft;

        public int getTimeLeft() {
            return timeLeft;
        }
    }
}