package com.example.lampcontrol.models.POJO;

import com.google.gson.annotations.SerializedName;

public class SentCommand {
    @SerializedName("relay")
    private Integer relay;

    @SerializedName("timer")
    private Timer timer;

    public SentCommand(Integer relay) {
        this.relay = relay;
    }

    public SentCommand(String action, long time, int cycles) {
        this.timer = new Timer(action, time, cycles);
    }

    public SentCommand(String action) {
        this.timer = new Timer(action);
    }

    public static class Timer {
        private String action;
        private Long time;
        private Integer cycles;

        public Timer(String action, Long time, int cycles) {
            this.action = action;
            this.time = time;
            this.cycles = cycles;
        }

        public Timer(String action) {
            this.action = action;
        }
    }
}
