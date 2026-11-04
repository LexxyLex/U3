
package com.example.detectorpostura

import android.app.Activity
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private lateinit var texto: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        texto = TextView(this)
        texto.textSize = 22f
        texto.gravity = Gravity.CENTER
        texto.setPadding(20, 20, 20, 20)

        setContentView(texto)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        if (sensor == null) {
            texto.text = "Sensor de gravedad no disponible"
        }
    }

    override fun onResume() {
        super.onResume()

        sensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val valores = String.format(
            Locale.getDefault(),
            "\n\nX: %.2f\nY: %.2f\nZ: %.2f",
            x, y, z
        )

        when {
            z > 7 -> {
                texto.text = "Celular acostado o superficie plana$valores"
                texto.setBackgroundColor(Color.GREEN)
                texto.setTextColor(Color.BLACK)
            }

            x > 7 -> {
                texto.text = "Celular inclinado hacia un lado$valores"
                texto.setBackgroundColor(Color.rgb(255, 165, 0))
                texto.setTextColor(Color.BLACK)
            }

            y > 7 -> {
                texto.text = "Celular vertical o inclinado$valores"
                texto.setBackgroundColor(Color.BLACK)
                texto.setTextColor(Color.WHITE)
            }

            else -> {
                texto.text = "Orientación indefinida$valores"
                texto.setBackgroundColor(Color.WHITE)
                texto.setTextColor(Color.BLACK)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}