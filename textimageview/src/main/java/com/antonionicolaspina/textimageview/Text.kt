package com.antonionicolaspina.textimageview

import android.graphics.PointF
import android.graphics.Typeface
import androidx.annotation.ColorInt

data class Text(
    val text: String,
    @ColorInt val textColor: Int,
    val textSize: Float,
    val typeface: Typeface?,
    val position: PointF = PointF(),
    var scaleFactor: Float = 1f,
    var rotationDegress: Float = 0f
)
