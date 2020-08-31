package com.antonionicolaspina.textimageview

import android.graphics.PointF

data class Text(
    val text: String,
    val position: PointF = PointF(),
    var scaleFactor: Float = 1f,
    var rotationDegress: Float = 0f
)
