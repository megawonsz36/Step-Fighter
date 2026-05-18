package com.example.stepfighter.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sqrt

class StepTrackerManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var stepsSinceStart by mutableStateOf(0)
        private set

    private var lastMagnitude = 0f
    private var lastStepTime = 0L

    fun startTracking() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt(x * x + y * y + z * z)
            val delta = magnitude - lastMagnitude
            lastMagnitude = magnitude

            if (delta > 4.5f) {
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - lastStepTime

                if (timeDiff in 350..1200) {
                    stepsSinceStart++
                    lastStepTime = currentTime
                } else if (timeDiff > 1200) {
                    lastStepTime = currentTime
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}