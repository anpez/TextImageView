package com.antonionicolaspina.textimageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.util.ArrayList;

public class TextImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener, RotationGestureDetector.OnRotationGestureListener {
  public interface OnTextMovedListener {
    void textMoved(PointF position);
  }

  public enum ClampMode {UNLIMITED, ORIGIN_INSIDE, TEXT_INSIDE}

  private ScaleGestureDetector scaleDetector;
  private RotationGestureDetector rotateDetector;

  private float scale = 1f;
  private float minSize;
  private float size;
  private float maxSize;

  private String text;
  private String[] textLines;
  private Paint paint;
  private RectF imageRect;
  private Rect textTotalRect;
  private ArrayList<Rect> textRects;
  private PointF textPosition;
  private PointF rotationCenter;
  private int interline;

  private PointF focalPoint;

  private float rotation = 0f;
  private float previousRotation = 0f;

  private boolean panEnabled;
  private boolean scaleEnabled;
  private boolean rotationEnabled;

  private ClampMode clampTextMode;

  private OnTextMovedListener onTextMovedListener;

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
    paint          = new Paint(Paint.ANTI_ALIAS_FLAG);
    imageRect      = new RectF();
    textTotalRect  = new Rect();
    textRects      = new ArrayList<>();
    textPosition   = new PointF(0f, 0f);
    rotationCenter = new PointF();
    focalPoint     = new PointF();

    if (null != attributeSet) {
      TypedArray attrs    = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TextImageView, 0, 0);
      Resources resources = context.getResources();
      size = attrs.getDimensionPixelSize(R.styleable.TextImageView_android_textSize, resources.getDimensionPixelSize(R.dimen.default_text_size));
      paint.setTextSize(size);
      paint.setColor(attrs.getColor(R.styleable.TextImageView_android_textColor, Color.BLACK));
      panEnabled = attrs.getBoolean(R.styleable.TextImageView_tiv_panEnabled, false);
      scaleEnabled = attrs.getBoolean(R.styleable.TextImageView_tiv_scaleEnabled, false);
      rotationEnabled = attrs.getBoolean(R.styleable.TextImageView_tiv_rotationEnabled, false);
      interline = attrs.getDimensionPixelOffset(R.styleable.TextImageView_tiv_interline, 0);
      clampTextMode = ClampMode.values()[attrs.getInt(R.styleable.TextImageView_tiv_clampTextMode, 0)];
      setText(attrs.getString(R.styleable.TextImageView_android_text));

      minSize = attrs.getDimensionPixelSize(R.styleable.TextImageView_tiv_minTextSize, resources.getDimensionPixelSize(R.dimen.default_min_text_size));
      maxSize = attrs.getDimensionPixelSize(R.styleable.TextImageView_tiv_maxTextSize, resources.getDimensionPixelSize(R.dimen.default_max_text_size));
      attrs.recycle();
    }

    scaleDetector  = new ScaleGestureDetector(context, this);
    rotateDetector = new RotationGestureDetector(this);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if ( (null == text) && isInEditMode()) {
      setText("sample text");
    }

    if (null == text) {
      return;
    }

    // Get rectangle of the drawable
    imageRect.top  = 0;
    imageRect.left = 0;

    Drawable drawable = getDrawable();
    if (null != drawable) {
      imageRect.right = drawable.getIntrinsicWidth();
      imageRect.bottom = drawable.getIntrinsicHeight();
    }
    // Translate and scale the rectangle
    getImageMatrix().mapRect(imageRect);

    canvas.save();
    if (rotationEnabled) {
      canvas.rotate(-rotation, rotationCenter.x, rotationCenter.y);
    }

    // Draw text
    float top = textPosition.y + imageRect.top;
    for(int i=0; i<textLines.length; i++) {
      int h = textRects.get(i).height();
      canvas.save();
      canvas.translate(textPosition.x + imageRect.left, top + h);
      canvas.drawText(textLines[i], 0, 0, paint);
      canvas.restore();
      top += h + interline*scale;
    }
    canvas.restore();
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
    scaleDetector.onTouchEvent(event);
    rotateDetector.onTouchEvent(event);
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

          rotationCenter.x += focalPoint.x - x;
          rotationCenter.y += focalPoint.y - y;

          reclampText();

          invalidate();
        }

        return true;
      }
    }
    return false;
  }

  protected void reclampText() {
    switch (clampTextMode) {
      case UNLIMITED:
        break;
      case ORIGIN_INSIDE: {
        RectF enclosingRect = calculateEnclosingRect();
        enclosingRect.offset(-imageRect.left, -imageRect.top);
        textPosition.x -= enclosingRect.left-between(enclosingRect.left, 0, imageRect.width());
        textPosition.y -= enclosingRect.top-between(enclosingRect.top, 0, imageRect.height());
        break;
      }
      case TEXT_INSIDE: {
        RectF enclosingRect = calculateEnclosingRect();
        enclosingRect.offset(-imageRect.left, -imageRect.top);
        textPosition.x -= enclosingRect.left - between(enclosingRect.left, 0, imageRect.width()-enclosingRect.width());
        textPosition.y -= enclosingRect.top - between(enclosingRect.top, 0, imageRect.height()-enclosingRect.height());
        break;
      }
    }

    if (null != onTextMovedListener) {
      PointF position = getTextPosition();
      if ( (!Float.isNaN(position.x)) && (!Float.isNaN(position.y)) ) {
        onTextMovedListener.textMoved(position);
      }
    }
  }

  protected RectF calculateEnclosingRect() {
    Matrix mat = new Matrix();
    RectF globalRect = new RectF();
    float top = textPosition.y;
    for(int i=0; i<textLines.length; i++) {
      int h = textRects.get(i).height();
      RectF rect = new RectF(0, 0, textRects.get(i).width(), h);
      rect.offset(imageRect.left, imageRect.top);

      mat.reset();
      mat.preRotate(-rotation, rotationCenter.x, rotationCenter.y);
      mat.preTranslate(textPosition.x, top);

      mat.mapRect(rect);

      if (0 == i) {
        globalRect.set(rect);
      } else {
        globalRect.top = Math.min(globalRect.top, rect.top);
        globalRect.left = Math.min(globalRect.left, rect.left);
        globalRect.bottom = Math.max(globalRect.bottom, rect.bottom);
        globalRect.right = Math.max(globalRect.right, rect.right);
      }
      top += h + interline*scale;
    }

    return globalRect;
  }

  @Override
  public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
    if (scaleEnabled) {
      scale *= scaleGestureDetector.getScaleFactor();
      paint.setTextSize(Math.max(minSize, Math.min(scale * size, maxSize)));
      scale = paint.getTextSize() / size;
      setText(text);
    }

    return true;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
    return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
  }

  @Override
  public void OnRotation(RotationGestureDetector rotationDetector) {
    if (rotationEnabled) {
      rotation += rotationDetector.getAngle() - previousRotation;
      previousRotation = rotationDetector.getAngle();

      rotationCenter.x = focalPoint.x;
      rotationCenter.y = focalPoint.y;
      invalidate();
    }
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
    this.textLines = null;
    if (null != text) {
      this.textLines = text.split("\n");
      int height = 0;
      int width  = 0;

      textRects.clear();
      for(int i=0; i<textLines.length; i++) {
        Rect r = new Rect();
        paint.getTextBounds(textLines[i], 0, textLines[i].length(), r);
        textRects.add(i, r);

        height += r.height();
        width   = Math.max(width, r.width());
      }
      height += (textLines.length-1)*interline*scale;
      textTotalRect.set(0, 0, width, height);
    }
    reclampText();
    invalidate();
  }

  /**
   * Set the typeface to use for the text.
   * @param typeface The typeface to be used.
   */
  public void setTypeface(Typeface typeface) {
    paint.setTypeface(typeface);
    setText(text);
  }

  /**
   * Set the text color.
   * @param color Color in the format of <a href="http://developer.android.com/reference/android/graphics/Color.html">android.graphics.Color</a>.
   *
   * @see <a href="http://developer.android.com/reference/android/graphics/Color.html">android.graphics.Color</a>
   */
  public void setTextColor(int color) {
    paint.setColor(color);
    invalidate();
  }

  /**
   * Set the default text size to the given value, interpreted as "scaled pixel" units.
   * This size is adjusted based on the current density and user font size preference.
   * @param textSize The scaled pixel size.
   */
  public void setTextSize(float textSize) {
    scale = 1f;
    size  = textSize;
    paint.setTextSize(textSize);
    setText(text);
  }

  /**
   * Return offset position between the text and the image. Considers both top left corners to the the calculation.
   * @return Pointf containing x and y offsets, as a per-one value. Eg. (0,0)=top-left, (1,1)=bottom-right.
   */
  public PointF getTextPosition() {
    RectF enclosingRect = calculateEnclosingRect();
    enclosingRect.offset(-imageRect.left, -imageRect.top);
    return new PointF(enclosingRect.left / imageRect.width(), enclosingRect.top / imageRect.height());
  }

  /**
   * Set the listener to be fired when the text changes its location.
   * @param listener the listener to be called, or null.
   */
  public void setOnTextMovedListener(OnTextMovedListener listener) {
    this.onTextMovedListener = listener;
  }

  /**
   * Get the relative size between the image and the text.
   * @return Relative size. Eg. 0.5=text half the height of the image.
   */
  public float getTextRelativeSize() {
    return paint.getTextSize() / imageRect.height();
  }
}
