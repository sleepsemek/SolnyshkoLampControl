package com.example.lampcontrol.Models;

import com.google.gson.annotations.SerializedName;

public class SentCommand {
    @SerializedName("relay")
    private Integer relay;

    @SerializedName("timer")
    private Timer timer;

    public SentCommand(Integer relay) {
        this.relay = relay;
    }

    public SentCommand(String action, long time) {
        this.timer = new Timer(action, time);
    }

    public SentCommand(String action) {
        this.timer = new Timer(action);
    }

    public static class Timer {
        private String action;
        private Long time;

        public Timer(String action, Long time) {
            this.action = action;
            this.time = time;
        }

        public Timer(String action) {
            this.action = action;
        }
    }
}
