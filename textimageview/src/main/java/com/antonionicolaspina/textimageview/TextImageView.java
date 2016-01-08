package com.antonionicolaspina.textimageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class TextImageView extends ImageView {
  public enum ClampMode {UNLIMITED, ORIGIN_INSIDE, TEXT_INSIDE}

  private String text;
  private Paint paint;
  private RectF imageRect;
  private Rect textRect;
  private PointF textPosition;

  private PointF focalPoint;

  private boolean panEnabled;

  private ClampMode clampTextMode;

  public TextImageView(Context context) {
    super(context);
    init(context, null);
  }

  public TextImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public TextImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public TextImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }

  protected void init(Context context, AttributeSet attributeSet) {
    paint        = new Paint(Paint.ANTI_ALIAS_FLAG);
    imageRect    = new RectF();
    textRect     = new Rect();
    textPosition = new PointF();
    focalPoint   = new PointF();

    if (null != attributeSet) {
      TypedArray attrs    = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TextImageView, 0, 0);
      Resources resources = context.getResources();
      paint.setTextSize(attrs.getDimensionPixelSize(R.styleable.TextImageView_textSize, resources.getDimensionPixelSize(R.dimen.default_text_size)));
      paint.setColor(attrs.getColor(R.styleable.TextImageView_textColor, Color.BLACK));
      panEnabled = attrs.getBoolean(R.styleable.TextImageView_panEnabled, false);
      clampTextMode = ClampMode.values()[attrs.getInt(R.styleable.TextImageView_clampTextMode, 0)];
      setText(attrs.getString(R.styleable.TextImageView_text));
      attrs.recycle();
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if ( (null == text) && isInEditMode()) {
      text = "sample text";
    }

    if (null == text) {
      return;
    }

    // Get rectangle of the drawable
    imageRect.top    = 0;
    imageRect.left   = 0;
    imageRect.right  = getDrawable().getIntrinsicWidth();
    imageRect.bottom = getDrawable().getIntrinsicHeight();

    // Translate and scale the rectangle
    getImageMatrix().mapRect(imageRect);

    // Draw text
    canvas.drawText(text, textPosition.x+imageRect.left, textPosition.y+imageRect.top+textRect.height(), paint);
  }

  protected void recalculateFocalPoint(MotionEvent event) {
    final int pointerCount = event.getPointerCount();
    if (pointerCount <= 0) {
      return;
    }

    focalPoint.x = 0f;
    focalPoint.y = 0f;
    for(int i=0; i<pointerCount; i++) {
      focalPoint.x += event.getX(i);
      focalPoint.y += event.getY(i);
    }
    focalPoint.x /= pointerCount;
    focalPoint.y /= pointerCount;
  }

  protected static float between(float value, float min, float max) {
    return Math.max(Math.min(value, max), min);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    super.onTouchEvent(event);

    final int action = event.getAction();
    switch(action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        recalculateFocalPoint(event);
        return true;
      case MotionEvent.ACTION_MOVE: {
        final float x = focalPoint.x;
        final float y = focalPoint.y;

        recalculateFocalPoint(event);

        if (panEnabled) {
          textPosition.x += focalPoint.x - x;
          textPosition.y += focalPoint.y - y;

          switch (clampTextMode) {
            case UNLIMITED:
              break;
            case ORIGIN_INSIDE:
              textPosition.x = between(textPosition.x, 0, imageRect.width());
              textPosition.y = between(textPosition.y, 0, imageRect.height());
              break;
            case TEXT_INSIDE:
              textPosition.x = between(textPosition.x, 0, imageRect.width()-textRect.width());
              textPosition.y = between(textPosition.y, 0, imageRect.height()-textRect.height());
              break;
          }

          invalidate();
        }

        return true;
      }
    }
    return false;
  }

  /**************
   *** Public ***
   **************/

  /**
   * Set text to be drawn over the image.
   * @param text The text.
   */
  public void setText(String text) {
    this.text = text;
    if (null != text) {
      paint.getTextBounds(text, 0, text.length(), textRect);
    }
    invalidate();
  }

  /**
   * Set the typeface to use for the text.
   * @param typeface The typeface to be used.
   */
  public void setTypeface(Typeface typeface) {
    paint.setTypeface(typeface);
  }

  /**
   * Return offset position between the text and the image. Considers both top left corners to the the calculation.
   * @return Pointf containing x and y offsets, as a per-one value. Eg. (0,0)=top-left, (1,1)=bottom-right.
   */
  public PointF getTextPosition() {
    return new PointF(textPosition.x / imageRect.width(), textPosition.y / imageRect.height());
  }
}
