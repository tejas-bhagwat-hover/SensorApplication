package com.example.sensorsample

data class SensorData(
    var timestamp: Int,
    var attitudeQuaternionX: Double,
    var attitudeQuaternionY: Double,
    var attitudeQuaternionZ: Double,
    var attitudeQuaternionW: Double,
    var rotationRateX: Double,
    var rotationRateY: Double,
    var rotationRateZ: Double,
    var gravityX: Double,
    var gravityY: Double,
    var gravityZ: Double,
    var userAccelerationX: Double,
    var userAccelerationY: Double,
    var userAccelerationZ: Double,
    var heading: Double,
    var calibratedMagneticFieldX: Double,
    var calibratedMagneticFieldY: Double,
    var calibratedMagneticFieldZ: Double,
    var calibratedMagneticFieldAccuracy: Int
)