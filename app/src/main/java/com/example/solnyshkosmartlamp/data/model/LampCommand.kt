package com.example.solnyshkosmartlamp.data.model

import com.google.gson.annotations.SerializedName

data class LampCommand(
    @SerializedName("relay")
    val relay: Int? = null,

    @SerializedName("timer")
    val timer: Timer? = null
) {
    constructor(relayParam: Int) : this(relay = relayParam)
    constructor(actionParam: String, time: Long, cycles: Int) : this(timer = Timer(actionParam, time, cycles))
    constructor(actionParam: String) : this(timer = Timer(action = actionParam))

    data class Timer(
        val action: String,
        val time: Long? = null,
        val cycles: Int? = null
    )
}
