package com.example.testaccelerometrekotlin


import android.annotation.SuppressLint
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testaccelerometrekotlin.databinding.MainBinding


class TestAccelerometreActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var color = false

    private lateinit var topTextView: TextView
    private lateinit var middleTextView: TextView
    private lateinit var bottomTextView: ScrollView

    private lateinit var bottomTextContainer: LinearLayout


    private var accelerometerFound: Boolean = false
    private var lightSensorFound: Boolean = false

    private var lastAccelerometerUpdate: Long = 0
    private var lastLightSensorUpdate: Long = 0


    private var lightSensorMaxRange: Float = 0f
    private var lastLightSensorValue: Float = -1f


    private lateinit var binding: MainBinding

    private val backgroundColor1 by lazy {
        ContextCompat.getColor(this, R.color.top_view_background_1)
    }

    private val backgroundColor2 by lazy {
        ContextCompat.getColor(this, R.color.top_view_background_2)
    }


    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        topTextView = binding.topTextView
        topTextView.setBackgroundColor(backgroundColor1)

        middleTextView = binding.middleTextView
        middleTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.middle_view_background))

        bottomTextView = binding.bottomTextView
        bottomTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.bottom_view_background))
        bottomTextContainer = binding.bottomTextContainer




        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        sensorsDetection()

        sensorsRegister()

        // register this class as a listener for the accelerometer sensor
        lastAccelerometerUpdate = System.currentTimeMillis()

    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> if(accelerometerFound) getAccelerometer(event)
            Sensor.TYPE_LIGHT -> if(lightSensorFound) getLight(event)
        }
    }

    private fun getLight(event: SensorEvent) {
        val lightValue = event.values[0]
        val currentTime = System.currentTimeMillis()

        // Use thresholds based on lightSensorMaxRange to categorize intensity
        val lowThreshold = lightSensorMaxRange / 3
        val highThreshold = 2 * lightSensorMaxRange / 3
        val intensity = when {
            lightValue < lowThreshold -> getString(R.string.low) // Retrieve string value
            lightValue < highThreshold -> getString(R.string.medium) // Retrieve string value
            else -> getString(R.string.high) // Retrieve string value
        }

        // Update only if significant time has passed or value has changed significantly
        if (currentTime - lastLightSensorUpdate > ACCELEROMETER_UPDATE_TIME || Math.abs(lastLightSensorValue - lightValue) > 200) {
            lastLightSensorUpdate = currentTime
            lastLightSensorValue = lightValue
            bottomViewAddText(
                "${getString(R.string.new_light_value)} = $lightValue lx" +
                    "\n$intensity ${getString(R.string.intensity)}"
            )
        }
    }



    @SuppressLint("SetTextI18n")
    private fun getAccelerometer(event: SensorEvent) {
        val values = event.values
        if (event.values.size < 3) return

        // Movement
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val accelerationSquareRoot = ((x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH))


        val actualTime = System.currentTimeMillis()


        if (accelerationSquareRoot >= MIN_ACCELERATION) {
            if (actualTime - lastAccelerometerUpdate < ACCELEROMETER_UPDATE_TIME) {
                return
            }
            lastAccelerometerUpdate = actualTime
            Toast.makeText(this, R.string.shuffed, Toast.LENGTH_SHORT).show()
            if (color) {
                topTextView.setBackgroundColor(backgroundColor1)
            } else {
                topTextView.setBackgroundColor(backgroundColor2)
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
        sensorsUnregister()
    }

    override fun onResume() {
        // register OnResume
        super.onResume()
        sensorsRegister()
    }

    private fun sensorsRegister() {
        if(accelerometerFound) sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        if(lightSensorFound) {
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

    }

    private fun sensorsUnregister() {
        if(accelerometerFound || lightSensorFound) {
            sensorManager.unregisterListener(this)
        }
    }

    private fun sensorsDetection() {
        // Accelerometer detection
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            Toast.makeText(this, R.string.no_accelerometer, Toast.LENGTH_LONG).show()
            accelerometerFound = false
        } else {
            accelerometerFound = true
        }

        // Light sensor detection
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            lightSensorFound = true
            lightSensorMaxRange = lightSensor!!.maximumRange
            bottomViewAddText("${getString(R.string.light_detected)} $lightSensorMaxRange ${getString(R.string.lx)}")
        } else {
            lightSensorFound = false
            bottomViewAddText(getString(R.string.no_light))
        }
    }

    private fun bottomViewAddText(text: String) {
        // Determine if we are already at the bottom of the list before adding the new TextView
        val wasAtBottom = isAtBottom()

        val tv = TextView(this).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        bottomTextContainer.addView(tv)

        // If we were at the bottom, scroll to the bottom after the view is added
        if (wasAtBottom) {
            scrollToBottom()
        }
    }

    private fun isAtBottom(): Boolean {
        val scrollBounds = Rect()
        bottomTextView.getDrawingRect(scrollBounds)
        val bottom = bottomTextContainer.bottom + bottomTextView.paddingBottom
        val scrollY = bottomTextView.scrollY + bottomTextView.height
        return bottom <= scrollY
    }

    private fun scrollToBottom() {
        bottomTextView.post {
            bottomTextView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }





    companion object {
        private const val MIN_ACCELERATION = 1.1f
        private const val ACCELEROMETER_UPDATE_TIME = 1000
    }
}
