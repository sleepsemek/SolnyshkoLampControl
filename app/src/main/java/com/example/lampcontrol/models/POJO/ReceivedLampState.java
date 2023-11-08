package com.example.lampcontrol.models.POJO;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ReceivedLampState {

    public enum RelayState {
        OFF, ON, PREHEATING, ACTIVE, PAUSED
    }

    @SerializedName("state")
    @JsonAdapter(RelayStateAdapter.class)
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

        @SerializedName("cycles")
        private int generalCycles;

        @SerializedName("cycle_time")
        private int cycleTime;

        public int getTimeLeft() {
            return timeLeft;
        }

        public int getGeneralCycles() {
            return generalCycles;
        }

        public int getGeneralCycleTime() {
            return cycleTime;
        }
    }

    public static class Preheat {
        @SerializedName("time_left")
        private int timeLeft;

        public int getTimeLeft() {
            return timeLeft;
        }
    }

    public static class RelayStateAdapter extends TypeAdapter<RelayState> {
        @Override
        public void write(JsonWriter out, RelayState value) throws IOException {
            out.value(value.name());
        }

        @Override
        public RelayState read(JsonReader in) throws IOException {
            int numericValue = in.nextInt();
            return RelayState.values()[numericValue];
        }
    }
}