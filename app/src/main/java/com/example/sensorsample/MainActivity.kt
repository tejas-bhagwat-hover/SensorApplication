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
import java.util.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var mainActivityBinding: ActivityMainBinding

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

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
                lowPassFilter(sensorEvent.values.clone(), accelerometerReading)
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
                lowPassFilter(sensorEvent.values.clone(), magnetometerReading)
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
        updateHeading()
    }

    private var heading = 0f
    private fun updateHeading() {
        heading = calculateHeading(accelerometerReading, magnetometerReading)
        heading = convertRadtoDeg(heading)
        heading = map180to360(heading)
        mainActivityBinding.tvHeading.text = heading.toString()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == TYPE_MAGNETIC_FIELD) {
            //   3 - android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH
            mainActivityBinding.tvMagnetAccuracy.text =
                "calibratedMagneticFieldAccuracy:$accuracy"
        }
    }

    private val alpha = 0.15f

    private fun lowPassFilter(input: FloatArray, output: FloatArray): FloatArray {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    fun convertRadtoDeg(rad: Float): Float {
        return (rad / Math.PI).toFloat() * 180
    }

    //map angle from [-180,180] range to [0,360] range
    fun map180to360(angle: Float): Float {
        return (angle + 360) % 360
    }

    private fun calculateHeading(
        accelerometerReading: FloatArray,
        magnetometerReading: FloatArray
    ): Float {
        var Ax = accelerometerReading[0]
        var Ay = accelerometerReading[1]
        var Az = accelerometerReading[2]
        val Ex = magnetometerReading[0]
        val Ey = magnetometerReading[1]
        val Ez = magnetometerReading[2]

        //cross product of the magnetic field vector and the gravity vector
        var Hx = Ey * Az - Ez * Ay
        var Hy = Ez * Ax - Ex * Az
        var Hz = Ex * Ay - Ey * Ax

        //normalize the values of resulting vector
        val invH = 1.0f / Math.sqrt((Hx * Hx + Hy * Hy + Hz * Hz).toDouble()).toFloat()
        Hx *= invH
        Hy *= invH
        Hz *= invH

        //normalize the values of gravity vector
        val invA = 1.0f / Math.sqrt((Ax * Ax + Ay * Ay + Az * Az).toDouble()).toFloat()
        Ax *= invA
        Ay *= invA
        Az *= invA

        //cross product of the gravity vector and the new vector H
        val Mx = Ay * Hz - Az * Hy
        val My = Az * Hx - Ax * Hz
        val Mz = Ax * Hy - Ay * Hx

        //arctangent to obtain heading in radians
        return Math.atan2(Hy.toDouble(), My.toDouble()).toFloat()
    }
}