package com.example.solnyshkosmartlamp.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

class CommandSender<T : Any>(
    private val scope: CoroutineScope,
    private val serializer: (T) -> DataByteArray,
    private val writer: suspend (DataByteArray) -> Unit,
    private val maxRetries: Int = 3,
    private val delayBetween: Long = 150L
) {
    private val commandChannel = Channel<T>(Channel.UNLIMITED)
    private val _errors = MutableSharedFlow<Throwable>()
    val errors: SharedFlow<Throwable> = _errors.asSharedFlow()

    fun init() {
        scope.launch {
            try {
                for (command in commandChannel) {
                    var attempt = 0
                    while (attempt < maxRetries) {
                        try {
                            val data = serializer(command)
                            writer(data)
                            delay(delayBetween)
                            break
                        } catch (e: Throwable) {
                            if (attempt == maxRetries - 1) {
                                _errors.emit(e)
                            } else {
                                delay(100L)
                            }
                        }
                        attempt++
                    }
                }
            } catch (e: ClosedReceiveChannelException) {

            } catch (e: Throwable) {
                _errors.emit(e)
            }

        }
    }

    fun submit(command: T) {
        commandChannel.trySend(command)
    }

    fun clear() {
        commandChannel.close()
    }
}
