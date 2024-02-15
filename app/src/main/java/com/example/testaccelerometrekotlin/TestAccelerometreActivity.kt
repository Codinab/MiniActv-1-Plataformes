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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testaccelerometrekotlin.databinding.MainBinding


class TestAccelerometreActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var color = false
    private lateinit var view: TextView
    private var lastUpdate: Long = 0

    private lateinit var binding: MainBinding

    private val greenBackgroundColor by lazy {
        ContextCompat.getColor(this, R.color.view_background_green)
    }

    private val redBackgroundColor by lazy {
        ContextCompat.getColor(this, R.color.view_background_red)
    }


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

        println("Test")
    }

    override fun onSensorChanged(event: SensorEvent) {
        getAccelerometer(event)
    }

    private fun getAccelerometer(event: SensorEvent) {
        val values = event.values
        if (event.values.size < 3) return;

        // Movement
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val accelerationSquareRoot = ((x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))


        val actualTime = System.currentTimeMillis()

        println("Acceleration: $accelerationSquareRoot")

        if (accelerationSquareRoot >= MIN_ACCELERATION) {
            if (actualTime - lastUpdate < ACCELEROMETER_UPDATE_TIME) {
                return
            }
            lastUpdate = actualTime
            Toast.makeText(this, R.string.shuffed, Toast.LENGTH_SHORT).show()
            if (color) {
                view.setBackgroundColor(greenBackgroundColor)
            } else {
                view.setBackgroundColor(redBackgroundColor)
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

    override fun onResume() {
        // register OnResume
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }


    companion object {
        private const val MIN_ACCELERATION = 1.1f
        private const val ACCELEROMETER_UPDATE_TIME = 1000
    }
}
