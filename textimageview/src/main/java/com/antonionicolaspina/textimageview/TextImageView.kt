package com.antonionicolaspina.textimageview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.contains
import androidx.core.graphics.toPoint
import androidx.core.graphics.toRectF
import androidx.core.graphics.withMatrix
import com.mapbox.android.gestures.*
import kotlin.math.*

class TextImageView
@JvmOverloads constructor(
  context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0
): AppCompatImageView(context, attributeSet, defStyleAttr) {
  interface Listener {
    fun textsChanged(texts: List<Text>)
    fun textTapped(text: Text)
  }

  var panEnabled = false
  var scaleEnabled = false
  var rotationEnabled = false
  val deleteEnabled: Boolean

  var listener: Listener? = null
  var initialTextSize = 0f
  var minTextSize = 0f
  var maxTextSize = 0f

  private val texts = mutableListOf<TextProperties>()
  private var selectedText: TextProperties? = null

  private var deleteAreaVisible = false
  private val deleteAreaHeight = context.resources.getDimensionPixelSize(R.dimen.tiv_delete_area_height)
  private val deleteButtonHeight = context.resources.getDimensionPixelSize(R.dimen.tiv_delete_button_height)
  private val deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_trash)!!
  private var deleteButtonScale = 1f

  init {
    context.theme.obtainStyledAttributes(attributeSet, R.styleable.TextImageView, 0, 0).apply {
      try {
        panEnabled = getBoolean(R.styleable.TextImageView_tiv_panEnabled, false)
        scaleEnabled = getBoolean(R.styleable.TextImageView_tiv_scaleEnabled, false)
        rotationEnabled = getBoolean(R.styleable.TextImageView_tiv_rotationEnabled, false)
        deleteEnabled = getBoolean(R.styleable.TextImageView_tiv_deleteEnabled, false)

        initialTextSize = getDimensionPixelSize(R.styleable.TextImageView_tiv_initialTextSize, resources.getDimensionPixelSize(
            R.dimen.tiv_default_text_size
          )).toFloat()
        minTextSize = getDimensionPixelSize(R.styleable.TextImageView_tiv_minTextSize, resources.getDimensionPixelSize(
            R.dimen.tiv_default_min_text_size
          )).toFloat()
        maxTextSize = getDimensionPixelSize(R.styleable.TextImageView_tiv_maxTextSize, resources.getDimensionPixelSize(R.dimen.tiv_default_max_text_size)).toFloat()
      } finally {
        recycle()
      }
    }
    if (deleteEnabled) {
      setPadding(
        paddingLeft,
        paddingTop,
        paddingRight,
        paddingBottom+deleteAreaHeight
      )
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (isInEditMode) {
      if (texts.isEmpty()) {
        setText("sample text")
      }
      deleteAreaVisible = deleteEnabled
    }

    if (deleteEnabled && deleteAreaVisible) {
      val w = measuredWidth
      val h = measuredHeight

      val buttonSize = getDeleteButtonSize()
      deleteDrawable.setBounds(
        (w-buttonSize)/2,
        h-deleteAreaHeight+(deleteAreaHeight-buttonSize)/2,
        (w+buttonSize)/2,
        h-(deleteAreaHeight-buttonSize)/2
      )
      deleteDrawable.draw(canvas)
    }

    texts.forEach { tp ->
      canvas.withMatrix(tp.matrix) {
        canvas.drawText(tp.text, 0f, 0f, tp.paint)
      }
    }
  }

  /**
   * Set text to be drawn over the image.
   * @param text The text.
   */
  fun setText(text: String) {
    texts.clear()
    addText(text)
  }

  /**
   * Change selected text.
   * @param text The text.
   */
  fun changeText(text: String) {
    selectedText?.let {
      it.text = text
      textChanged(it)
    }
  }

  /**
   * Set the typeface to use for the text.
   * @param typeface The typeface to be used.
   */
  fun setTypeface(typeface: Typeface?) {
    selectedText?.let {
      it.paint.typeface = typeface
      textChanged(it)
    }
  }

  fun setPosition(position: PointF) {
    selectedText?.let {
      it.position.x = position.x*measuredWidth
      it.position.y = position.y*measuredHeight
      textChanged(it)
    }
  }

  fun setScaleFactor(scale: Float) {
    selectedText?.let {
      it.scaleFactor = scale
      textChanged(it)
    }
  }

  fun setRotationDegrees(rotation: Float) {
    selectedText?.let {
      it.rotationDegress = rotation
      textChanged(it)
    }
  }

  fun getSelected() = selectedText?.toText(measuredWidth, measuredHeight)

  /**
   * Set the text color.
   * @param color Color in the format of <a href="http://developer.android.com/reference/android/graphics/Color.html">android.graphics.Color</a>.
   *
   * @see <a href="http://developer.android.com/reference/android/graphics/Color.html">android.graphics.Color</a>
   */
  fun setTextColor(@ColorInt color: Int) {
    selectedText?.let {
      it.paint.color = color
      textChanged(it)
    }
  }

  /**
   * Adds a text to be drawn over the image, above existing texts.
   * @param text The text.
   */
  fun addText(text: String) {
    val tp = TextProperties(text)
    tp.paint.textSize = initialTextSize
    tp.paint.getTextBounds(tp.text, 0, tp.text.length, tp.boundingRect)
    tp.position.x = measuredWidth/2f-tp.boundingRect.exactCenterX()
    tp.position.y = measuredHeight/2f-tp.boundingRect.exactCenterY()

    texts.add(tp)
    selectedText = tp
    textChanged(tp)
  }

  /**
   * Adds a drop shadow
   */
  fun addDropShadow(
    angle: Float = 45f, distance: Float = 0.02f, @ColorInt color: Int = Color.argb(
      0x80,
      0,
      0,
      0
    )
  ) {
    selectedText?.let {
      val textSize = it.paint.textSize
      it.paint.setShadowLayer(
        1f,
        textSize * cos(angle) * distance,
        textSize * sin(angle) * distance,
        color
      )
    }
  }

  /**
   * Returns all texts' data
   */
  fun getTexts(): List<Text> {
    val w = measuredWidth
    val h = measuredHeight
    return texts.map { it.toText(w, h) }
  }

  private var valueAnimator: ValueAnimator? = null
  //region Gestures
  private val androidGesturesManager = AndroidGesturesManager(context).apply {
    setMoveGestureListener(object : MoveGestureDetector.SimpleOnMoveGestureListener() {
      override fun onMove(
        detector: MoveGestureDetector,
        distanceX: Float,
        distanceY: Float
      ): Boolean {
        if (panEnabled) {
          selectedText?.let { tp ->
            tp.position.x -= distanceX
            tp.position.y -= distanceY
            textChanged(tp)

            if (deleteEnabled && deleteAreaVisible) {
              if (inDeleteArea(tp)) {
                ValueAnimator.ofFloat(deleteButtonScale, 1.5f)
                  .setDuration(200)
                  .apply {
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                      deleteButtonScale = it.animatedValue as Float
                      invalidate()
                    }
                    start()
                    valueAnimator = this
                  }
              } else {
                ValueAnimator.ofFloat(deleteButtonScale, 1f)
                  .setDuration(200)
                  .apply {
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                      deleteButtonScale = it.animatedValue as Float
                      invalidate()
                    }
                    start()
                    valueAnimator = this
                  }
              }
            }
          }
        }
        return true
      }

      override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
        if (deleteEnabled) {
          if (null != selectedText) {
            deleteButtonScale = 1f
            deleteAreaVisible = true
            invalidate()
          }
        }
        return true
      }

      override fun onMoveEnd(detector: MoveGestureDetector, velocityX: Float, velocityY: Float) {
        if (deleteEnabled && deleteAreaVisible) {
          deleteAreaVisible = false
          if (true == valueAnimator?.isRunning) {
            valueAnimator?.cancel()
            valueAnimator = null
          }
          deleteButtonScale = 1f
          selectedText?.let {
            checkDeleted(it)
          }
          invalidate()
        }
      }
    })

    setStandardScaleGestureListener(object :
      StandardScaleGestureDetector.SimpleStandardOnScaleGestureListener() {
      override fun onScale(detector: StandardScaleGestureDetector): Boolean {
        if (scaleEnabled) {
          selectedText?.let {
            val s = it.scaleFactor * detector.scaleFactor
            val textSize = s * it.paint.textSize
            it.scaleFactor = max(minTextSize, min(textSize, maxTextSize)) / it.paint.textSize
            textChanged(it)
          }
        }
        return true
      }
    })

    setRotateGestureListener(object : RotateGestureDetector.SimpleOnRotateGestureListener() {
      override fun onRotate(
        detector: RotateGestureDetector,
        rotationDegreesSinceLast: Float,
        rotationDegreesSinceFirst: Float
      ): Boolean {
        if (rotationEnabled) {
          selectedText?.let {
            it.rotationDegress -= rotationDegreesSinceLast
            textChanged(it)
          }
        }
        return true
      }
    })

    setStandardGestureListener(object : StandardGestureDetector.SimpleStandardOnGestureListener() {
      override fun onDown(e: MotionEvent): Boolean {
        texts.forEach { tp ->
          val p = tp.inverseMap(e.x, e.y)

          if (tp.boundingRect.contains(p.toPoint())) {
            selectedText = tp
          }
        }
        invalidate()
        return true
      }

      override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val listener = listener
        val selectedText = selectedText
        if ( (null!=selectedText) && (null != listener) ) {
          val p = selectedText.inverseMap(e.x, e.y)

          if (selectedText.boundingRect.contains(p.toPoint())) {
            listener.textTapped(selectedText.toText(measuredWidth, measuredHeight))
          }
        }
        return true
      }
    })
  }

  private fun getDeleteButtonSize() = (deleteButtonHeight*deleteButtonScale).roundToInt()
  private fun inDeleteArea(tp: TextProperties): Boolean {
    val w = measuredWidth
    val h = measuredHeight
    val buttonSize = getDeleteButtonSize()
    val r = RectF(
      (w-buttonSize)/2f,
      h-deleteAreaHeight+(deleteAreaHeight-buttonSize)/2f,
      (w+buttonSize)/2f,
      h-(deleteAreaHeight-buttonSize)/2f
    )

    val boundingRect = tp.boundingRect.toRectF()
    tp.matrix.mapRect(boundingRect)
    return r.intersect(boundingRect)
  }

  private fun checkDeleted(tp: TextProperties) {
    if (inDeleteArea(tp)) {
      texts.remove(tp)
      invalidate()
      listener?.textsChanged(getTexts())
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent?): Boolean {
    androidGesturesManager.onTouchEvent(event)
    return true
  }
  //endregion

  private fun textChanged(tp: TextProperties) {
    invalidate()

    tp.paint.getTextBounds(tp.text, 0, tp.text.length, tp.boundingRect)

    with(tp.matrix) {
      reset()
      setScale(tp.scaleFactor, tp.scaleFactor)
      preTranslate(tp.position.x / tp.scaleFactor, tp.position.y / tp.scaleFactor)
      preRotate(
        tp.rotationDegress,
        tp.boundingRect.exactCenterX() / tp.scaleFactor,
        tp.boundingRect.exactCenterY() / tp.scaleFactor
      )
    }

    listener?.textsChanged(getTexts())
  }
}
