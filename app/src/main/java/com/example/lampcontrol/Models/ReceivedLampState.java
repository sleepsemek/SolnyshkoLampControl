package com.example.lampcontrol.Models;

import com.google.gson.annotations.SerializedName;

public class ReceivedLampState {

    public enum RelayState {
        OFF, ON, PREHEATING, ACTIVE, PAUSED
    }

    @SerializedName("state")
    private RelayState lampState;

    @SerializedName("timer")
    private Timer timer;

    @SerializedName("preheat")
    private Preheat preheat;

    public RelayState getState() {
        return lampState;
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