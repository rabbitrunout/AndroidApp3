package com.example.childtracker

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationVector: Sensor? = null

    private lateinit var needle: ImageView
    private lateinit var headingText: TextView
    private lateinit var debugText: TextView

    private var currentDegree = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compass)

        // Toolbar с кнопкой "назад"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // UI
        needle = findViewById(R.id.needle)
        headingText = findViewById(R.id.headingText)
        debugText = findViewById(R.id.debugText)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationVector == null) {
            debugText.text = "Sensor: NOT AVAILABLE"
        } else {
            debugText.text = "Sensor: ready"
        }
    }

    override fun onResume() {
        super.onResume()
        rotationVector?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)

        // Азимут в градусах (0–360)
        val azimuthRad = orientation[0].toDouble()
        val azimuthDeg = Math.toDegrees(azimuthRad).toFloat()

        // Анимация поворота стрелки
        val rotate = RotateAnimation(
            currentDegree,
            -azimuthDeg,                        // минус — чтобы стрелка указывала "вперёд"
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 210
        rotate.fillAfter = true

        needle.startAnimation(rotate)
        currentDegree = -azimuthDeg

        headingText.text = "Heading: ${azimuthDeg.toInt()}°"
        debugText.text = "Sensor: ${"%.1f".format(azimuthDeg)}°"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
