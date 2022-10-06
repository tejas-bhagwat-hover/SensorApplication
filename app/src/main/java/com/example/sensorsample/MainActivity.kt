package com.example.sensorsample

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.Sensor.*
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sensorsample.databinding.ActivityMainBinding

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mainActivityBinding: ActivityMainBinding

    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val alpha = 0.97f

    private var azimuth = 0f
    private val azimuthFix = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        val gravitySensor = sensorManager.getDefaultSensor(TYPE_GRAVITY)
        val gyroScopeSensor = sensorManager.getDefaultSensor(TYPE_GYROSCOPE)
        val linearAccelerometer = sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION)
        val magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
        val rotationVector = sensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)

        val accelerometerSupported =
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        val gravitySensorSupported =
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        val gyroScopeSensorSupported =
            sensorManager.registerListener(this, gyroScopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        val linearAccelerometerSupported = sensorManager.registerListener(
            this,
            linearAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        val magnetometerSupported =
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
        val rotationVectorSupported =
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL)

        mainActivityBinding.tvSensorsSupported.text =
            "accelerometerSupported: $accelerometerSupported\n" +
                    "gravitySensorSupported: $gravitySensorSupported\n" +
                    "gyroScopeSensorSupported: $gyroScopeSensorSupported\n" +
                    "linearAccelerometerSupported: $linearAccelerometerSupported\n" +
                    "magnetometerSupported: $magnetometerSupported\n" +
                    "rotationVectorSupported: $rotationVectorSupported"
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        when (sensorEvent?.sensor?.type) {
            TYPE_ACCELEROMETER -> {
                mainActivityBinding.tvAccelerometer.text =
                    "TYPE_ACCELEROMETER: \nTimestamp: ${sensorEvent.timestamp}\n" +
                            "X:${sensorEvent.values[0]}\n" +
                            "Y:${sensorEvent.values[1]}\n" +
                            "Z:${sensorEvent.values[2]}"
            }
            TYPE_GRAVITY -> {
                mainActivityBinding.tvGravity.text =
                    "TYPE_GRAVITY: \nTimestamp: ${sensorEvent.timestamp}\n" +
                            "gravityX:${sensorEvent.values[0]}\n" +
                            "gravityY:${sensorEvent.values[1]}\n" +
                            "gravityZ:${sensorEvent.values[2]}"
            }
            TYPE_GYROSCOPE -> {
                mainActivityBinding.tvGyroscope.text =
                    "TYPE_GYROSCOPE: \nTimestamp: ${sensorEvent.timestamp}\n" +
                            "rateOfRotationX:${sensorEvent.values[0]}\n" +
                            "rateOfRotationY:${sensorEvent.values[1]}\n" +
                            "rateOfRotationZ:${sensorEvent.values[2]}"
            }
            TYPE_LINEAR_ACCELERATION -> {
                mainActivityBinding.tvLinearAcceleration.text =
                    "TYPE_LINEAR_ACCELERATION: \nTimestamp: ${sensorEvent.timestamp}\n" +
                            "X:${sensorEvent.values[0]}\n" +
                            "Y:${sensorEvent.values[1]}\n" +
                            "Z:${sensorEvent.values[2]}"
            }
            TYPE_MAGNETIC_FIELD -> {
                mainActivityBinding.tvMagnet.text =
                    "TYPE_MAGNETIC_FIELD: \nTimestamp: ${sensorEvent.timestamp}\n" +
                            "X:${sensorEvent.values[0]}\n" +
                            "Y:${sensorEvent.values[1]}\n" +
                            "Z:${sensorEvent.values[2]}"
            }
            TYPE_ROTATION_VECTOR -> {
                mainActivityBinding.tvQuaternion.text =
                    "TYPE_ROTATION_VECTOR: \nTimestamp: ${sensorEvent.timestamp}\n" +
                            "X:${sensorEvent.values[0]}\n" +
                            "Y:${sensorEvent.values[1]}\n" +
                            "Y:${sensorEvent.values[2]}\n" +
                            "w:${sensorEvent.values[3]}"
            }
        }


        synchronized(this) {
            if (sensorEvent?.sensor?.type == TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * sensorEvent.values[0]
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * sensorEvent.values[1]
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * sensorEvent.values[2]
            }
            if (sensorEvent?.sensor?.type == TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * sensorEvent.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * sensorEvent.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * sensorEvent.values[2]
            }
            val success = SensorManager.getRotationMatrix(
                rotationMatrix, inclinationMatrix, mGravity,
                mGeomagnetic
            )
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                // Log.d(TAG, "azimuth (rad): " + azimuth);
                azimuth =
                    Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + azimuthFix + 360) % 360
                mainActivityBinding.tvHeading.text = "Heading:$azimuth"
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

}