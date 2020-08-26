package com.antonionicolaspina.textimageview.sample

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.antonionicolaspina.textimageview.TextImageView

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val textImageView = findViewById<View>(R.id.text_image) as TextImageView
    textImageView.setTypeface(Typeface.DEFAULT_BOLD)
    val textView = findViewById<View>(R.id.text) as TextView
    textImageView.setOnTextMovedListener { position ->
      textView.text =
        String.format("Position: [%.2f%%, %.2f%%]", position.x * 100f, position.y * 100f)
    }
  }
}
