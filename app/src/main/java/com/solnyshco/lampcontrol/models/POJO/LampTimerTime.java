package com.solnyshco.lampcontrol.models.POJO;

public class LampTimerTime {

    private long minutes;
    private long seconds;

    public LampTimerTime(long minutes, long seconds) {
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }
}
