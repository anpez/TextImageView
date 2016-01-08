package com.antonionicolaspina.textimageview.sample;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.antonionicolaspina.textimageview.TextImageView;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    TextImageView textImageView = (TextImageView) findViewById(R.id.text_image);
    textImageView.setTypeface(Typeface.DEFAULT_BOLD);
  }
}
