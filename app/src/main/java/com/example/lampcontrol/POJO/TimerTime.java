package com.example.lampcontrol.POJO;

public class TimerTime {

    private long minutes;
    private long seconds;

    public TimerTime(long minutes, long seconds) {
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
