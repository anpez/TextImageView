package com.antonionicolaspina.textimageview.sample

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antonionicolaspina.textimageview.Text
import com.antonionicolaspina.textimageview.TextImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    text_image.listener = object : TextImageView.Listener {
      override fun textsChanged(texts: List<Text>) {
        position_textview.text = texts.joinToString("\n")
      }

      override fun textTapped(text: Text) {
        Toast.makeText(this@MainActivity, "Tapped ${text.text}", Toast.LENGTH_SHORT).show()
      }
    }

    add_more_button.setOnClickListener {
      text_image.addText("Text ${Date()}")
      text_image.setTypeface(Typeface.DEFAULT_BOLD)
      text_image.setTextColor(Color.RED)
      text_image.addDropShadow()
    }
  }
}
