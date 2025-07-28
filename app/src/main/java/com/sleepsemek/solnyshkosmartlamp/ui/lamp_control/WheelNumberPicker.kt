package com.sleepsemek.solnyshkosmartlamp.ui.lamp_control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.commandiron.wheel_picker_compose.core.SelectorProperties
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.commandiron.wheel_picker_compose.core.WheelTextPicker

@Composable
fun WheelNumberPicker(
    modifier: Modifier = Modifier,
    valueRange: IntRange,
    value: Int,
    size: DpSize = DpSize(100.dp, 100.dp),
    rowCount: Int = 3,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = LocalContentColor.current,
    selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
    onValueChange: (Int) -> Unit
) {
    val values = valueRange.toList()
    val currentIndex = values.indexOf(value).coerceAtLeast(0)

    WheelTextPicker(
        modifier = modifier,
        size = size,
        texts = values.map { it.toString().padStart(2, '0') },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        startIndex = currentIndex,
        selectorProperties = selectorProperties,
        onScrollFinished = { snappedIndex ->
            values.getOrNull(snappedIndex)?.let(onValueChange)
            snappedIndex
        }
    )
}

@Composable
fun TimeWheelPicker(
    modifier: Modifier = Modifier,
    minutes: Int,
    seconds: Int,
    size: DpSize = DpSize(200.dp, 160.dp),
    rowCount: Int = 3,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = LocalContentColor.current,
    selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
    onTimeChange: (minutes: Int, seconds: Int) -> Unit
) {
    val values = (0..59).toList()

    val inactiveSelector = WheelPickerDefaults.selectorProperties(enabled = false)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (selectorProperties.enabled().value) {
            Surface(
                modifier = Modifier
                    .size(size.width, size.height / rowCount),
                shape = selectorProperties.shape().value,
                color = selectorProperties.color().value,
                border = selectorProperties.border().value
            ) {}
        }

        Row(
            modifier = Modifier.width(size.width),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelTextPicker(
                size = DpSize(size.width / 2.5f, size.height),
                texts = values.map { it.toString().padStart(2, '0') },
                rowCount = rowCount,
                style = textStyle,
                color = textColor,
                startIndex = values.indexOf(minutes),
                selectorProperties = inactiveSelector,
                onScrollFinished = { index ->
                    values.getOrNull(index)?.let {
                        onTimeChange(it, seconds)
                    }
                    index
                }
            )

            Text(":", style = textStyle, color = textColor)

            WheelTextPicker(
                size = DpSize(size.width / 2.5f, size.height),
                texts = values.map { it.toString().padStart(2, '0') },
                rowCount = rowCount,
                style = textStyle,
                color = textColor,
                startIndex = values.indexOf(seconds),
                selectorProperties = inactiveSelector,
                onScrollFinished = { index ->
                    values.getOrNull(index)?.let {
                        onTimeChange(minutes, it)
                    }
                    index
                }
            )
        }
    }
}




