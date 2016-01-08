package com.antonionicolaspina.textimageview.sample;

import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.antonionicolaspina.textimageview.TextImageView;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final TextImageView textImageView = (TextImageView) findViewById(R.id.text_image);
    textImageView.setTypeface(Typeface.DEFAULT_BOLD);

    final TextView textView = (TextView) findViewById(R.id.text);

    final Handler h = new Handler();
    h.postDelayed(new Runnable() {
      @Override
      public void run() {
        PointF position = textImageView.getTextPosition();
        textView.setText(String.format("Position: [%.2f%%, %.2f%%]", position.x*100f, position.y*100f));
        h.postDelayed(this, 100);
      }
    }, 100);
  }
}
