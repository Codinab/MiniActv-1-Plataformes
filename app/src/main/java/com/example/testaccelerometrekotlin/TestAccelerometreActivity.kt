package com.example.testaccelerometrekotlin


import android.app.Activity
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.testaccelerometrekotlin.databinding.MainBinding


class TestAccelerometreActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var color = false
    private lateinit var view: TextView
    private var lastUpdate: Long = 0

    private lateinit var binding: MainBinding

    /** Called when the activity is first created.  */

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        view = binding.textView
        view.setBackgroundColor(Color.GREEN)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        // register this class as a listener for the accelerometer sensor
        lastUpdate = System.currentTimeMillis()
    }

    override fun onSensorChanged(event: SensorEvent) {
        getAccelerometer(event)
    }

    private fun getAccelerometer(event: SensorEvent) {
        val values = event.values
        // Movement
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val accelerationSquareRoot = (x * x + y * y + z * z
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))
        val actualTime = System.currentTimeMillis()
        if (accelerationSquareRoot >= 200) {
            if (actualTime - lastUpdate < 1000) {
                return
            }
            lastUpdate = actualTime
            Toast.makeText(this, R.string.shuffed, Toast.LENGTH_SHORT).show()
            if (color) {
                view.setBackgroundColor(Color.GREEN)
            } else {
                view.setBackgroundColor(Color.RED)
            }
            color = !color
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onPause() {
        // unregister listener
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
