package com.sleepsemek.solnyshkosmartlamp.data.model

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

data class LampState(
    @SerializedName("state")
    @JsonAdapter(RelayStateAdapter::class)
    val lampState: RelayState = RelayState.NONE,

    @SerializedName("timer")
    val timer: Timer? = null,

    @SerializedName("preheat")
    val preheat: Preheat? = null,

    @SerializedName("version")
    val version: String? = null
) {
    enum class RelayState {
        OFF, ON, PREHEATING, ACTIVE, PAUSED, NONE
    }

    data class Timer(
        @SerializedName("time_left")
        val timeLeft: Int = 0,

        @SerializedName("cycles")
        val generalCycles: Int = 0,

        @SerializedName("cycle_time")
        val cycleTime: Int = 0
    )

    data class Preheat(
        @SerializedName("time_left")
        val timeLeft: Int = 0
    )
}

class RelayStateAdapter : TypeAdapter<LampState.RelayState>() {
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LampState.RelayState) {
        out.value(value.ordinal)
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): LampState.RelayState {
        val ordinal = reader.nextInt()
        return LampState.RelayState.entries.toTypedArray().getOrElse(ordinal) { LampState.RelayState.NONE }
    }
}
