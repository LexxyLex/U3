package com.example.detectorpostura

import android.app.Activity
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    private lateinit var menu: LinearLayout
    private lateinit var pantalla: LinearLayout
    private lateinit var texto: TextView
    private lateinit var valores: TextView
    private lateinit var imagen: ImageView

    private var tipoSensor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menu = findViewById(R.id.menu)
        pantalla = findViewById(R.id.pantalla)
        texto = findViewById(R.id.texto)
        valores = findViewById(R.id.valores)
        imagen = findViewById(R.id.imagen)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        findViewById<Button>(R.id.btnGravedad).setOnClickListener {
            iniciarSensor(Sensor.TYPE_GRAVITY, 1)
        }

        findViewById<Button>(R.id.btnMagnetico).setOnClickListener {
            iniciarSensor(Sensor.TYPE_MAGNETIC_FIELD, 2)
        }

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            sensorManager.unregisterListener(this)
            pantalla.visibility = View.GONE
            menu.visibility = View.VISIBLE
        }
    }

    private fun iniciarSensor(tipo: Int, opcion: Int) {
        sensorManager.unregisterListener(this)
        tipoSensor = opcion
        sensor = sensorManager.getDefaultSensor(tipo)

        menu.visibility = View.GONE
        pantalla.visibility = View.VISIBLE

        if (sensor == null) {
            texto.text = "Sensor no disponible"
            valores.text = ""
            imagen.visibility = View.GONE
            return
        }

        if (opcion == 1) {
            texto.text = "Detector de postura"
            imagen.visibility = View.GONE
        } else {
            texto.text = "Detector de metales"
            imagen.visibility = View.VISIBLE
            cargarImagen("otra_imagen.jpeg")
        }

        sensorManager.registerListener(
            this, sensor, SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        valores.text = String.format(
            Locale.getDefault(),
            "X: %.2f\nY: %.2f\nZ: %.2f",
            x, y, z
        )

        if (tipoSensor == 1) {
            detectarPostura(x, y, z)
        } else if (tipoSensor == 2) {
            detectarMetales(x, y, z)
        }
    }

    private fun detectarPostura(x: Float, y: Float, z: Float) {
        imagen.visibility = View.GONE

        when {
            abs(z) > 7 -> {
                texto.text = "Celular acostado o superficie plana"
                pantalla.setBackgroundColor(Color.GREEN)
                texto.setTextColor(Color.BLACK)
            }
            abs(x) > 7 -> {
                texto.text = "Celular inclinado hacia un lado"
                pantalla.setBackgroundColor(Color.rgb(255, 165, 0))
                texto.setTextColor(Color.BLACK)
            }
            abs(y) > 7 -> {
                texto.text = "Celular vertical o inclinado"
                pantalla.setBackgroundColor(Color.BLACK)
                texto.setTextColor(Color.WHITE)
            }
            else -> {
                texto.text = "Orientación indefinida"
                pantalla.setBackgroundColor(Color.WHITE)
                texto.setTextColor(Color.BLACK)
            }
        }
    }

    private fun detectarMetales(x: Float, y: Float, z: Float) {
        val magnitud = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        imagen.visibility = View.VISIBLE

        when {
            magnitud > 100 -> {
                texto.text = "Campo magnético fuerte detectado"
                pantalla.setBackgroundColor(Color.BLACK)
                texto.setTextColor(Color.WHITE)
                cargarImagen("iman.jpeg")
            }
            magnitud >= 60 -> {
                texto.text = "Campo magnético débil"
                pantalla.setBackgroundColor(Color.BLUE)
                texto.setTextColor(Color.WHITE)
                cargarImagen("iman.jpeg")
            }
            else -> {
                texto.text = "Campo magnético normal"
                pantalla.setBackgroundColor(Color.GREEN)
                texto.setTextColor(Color.BLACK)
                cargarImagen("otra_imagen.jpeg")
            }
        }
    }

    private fun cargarImagen(nombreArchivo: String) {
        val resId = resources.getIdentifier(nombreArchivo, "drawable", packageName)
        if (resId != 0) {
            imagen.setImageResource(resId)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}