package ru.mtuci.bbca.scale.overlay_image_view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import ru.mtuci.bbca.BuildConfig
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

typealias OverlayItemsOnClickListener = (List<OverlayItem>) -> Unit

class OverlayImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : SubsamplingScaleImageView(context, attrs),
    GestureDetector.OnGestureListener by GestureDetector.SimpleOnGestureListener(),
    ScaleGestureDetector.OnScaleGestureListener by ScaleGestureDetector.SimpleOnScaleGestureListener() {

    var items = emptyList<OverlayItem>()
        set(value) {
            field = value
            invalidate()
        }

    var debugHighlightedItemId: String? = null
        set(value) {
            field = value
            invalidate()
        }

    var isDebugModeEnabled = true
        set(value) {
            field = value
            invalidate()
        }

    private val debugItemsPaint = Paint().apply {
        color = Color.GREEN
        alpha = DEBUG_ITEM_ALPHA
        style = Paint.Style.FILL
    }

    private val ripplePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    private val topLeftCoordinates = PointF(0f, 0f)
    private val translate = PointF()

    private val gestureDetector = GestureDetector(context, this)
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    private val rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener { animator ->
            rippleAnimationProgress = animator.animatedValue as Float

            if (rippleAnimationProgress == 1f) {
                rippleAnimationProgress = 0f
            }
        }

        duration = RIPPLE_DURATION
    }

    private var isScrolling = false
    private var previousScrollingPointersDistance = 0f

    private var rippleCenter = PointF()

    private var rippleAnimationProgress = 0f
        set(value) {
            field = value
            ripplePaint.alpha = (255 * (1 - value)).roundToInt()
            invalidate()
        }

    private var overlayItemsOnClickListener: OverlayItemsOnClickListener? = null

    fun setOverlayItemsOnClickListener(listener: OverlayItemsOnClickListener?) {
        overlayItemsOnClickListener = listener
    }

    fun triggerRipple() {
        rippleAnimator.start()
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (isScrolling) {
            return false
        }

        val newScale = (scale * detector.scaleFactor).coerceIn(minScale, maxScale)

        setScaleAndCenter(newScale, center)

        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        val x = ((e.x - translate.x) / scale).roundToInt()
        val y = ((e.y - translate.y) / scale).roundToInt()

        rippleCenter.x = x.toFloat()
        rippleCenter.y = y.toFloat()

        val clickedItems = mutableListOf<OverlayItem>()

        items.forEach { item ->
            if (x in item.x..(item.x + item.width) &&
                y in item.y..(item.y + item.height)) {
                clickedItems.add(item)
            }
        }

        overlayItemsOnClickListener?.invoke(clickedItems)

        return clickedItems.isNotEmpty()
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (e2.pointerCount != 2) {
            isScrolling = false
            return false
        }

        if (!isScrolling && abs(distanceX) < SCROLL_THRESHOLD && abs(distanceY) < SCROLL_THRESHOLD) {
            return false
        }

        val pointersDistance =
            getScrollingPointersDistance(e2)

        val pointersDistanceDifference =
            abs(pointersDistance - previousScrollingPointersDistance)

        previousScrollingPointersDistance = pointersDistance

        if (!isScrolling && pointersDistanceDifference > SCROLLING_POINTERS_DISTANCE_THRESHOLD) {
            return false
        }

        isScrolling = true

        val center = center ?: return false

        val newCenter = PointF(center.x + distanceX / scale, center.y + distanceY / scale)

        setScaleAndCenter(scale, newCenter)

        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_POINTER_UP ||
            event.actionMasked == MotionEvent.ACTION_UP) {
            isScrolling = false
        }

        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isReady) {
            return
        }

        sourceToViewCoord(topLeftCoordinates, translate)

        drawRipple(canvas)

        if (!(BuildConfig.DEBUG && isDebugModeEnabled)) {
            return
        }

        drawDebugItems(canvas)
    }

    private fun drawRipple(canvas: Canvas) {
        val x = translate.x + rippleCenter.x * scale
        val y = translate.y + rippleCenter.y * scale

        canvas.drawCircle(
            x,
            y,
            RIPPLE_MAX_RADIUS * rippleAnimationProgress * scale,
            ripplePaint
        )
    }

    private fun drawDebugItems(canvas: Canvas) {
        items.forEach { item ->
            if (item.id == debugHighlightedItemId) {
                val left = translate.x + item.x * scale
                val top = translate.y + item.y * scale
                val right = translate.x + (item.x + item.width) * scale
                val bottom = translate.y + (item.y + item.height) * scale

                canvas.drawRect(
                    left,
                    top,
                    right,
                    bottom,
                    debugItemsPaint
                )
            }
        }
    }

    private fun getScrollingPointersDistance(event: MotionEvent): Float {
        if (event.pointerCount != 2) {
            return 0f
        }

        val x1 = event.getX(0)
        val y1 = event.getY(0)

        val x2 = event.getX(1)
        val y2 = event.getY(1)

        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    companion object {
        private const val DEBUG_ITEM_ALPHA = 180

        private const val SCROLL_THRESHOLD = 20f
        private const val SCROLLING_POINTERS_DISTANCE_THRESHOLD = 15f

        private const val RIPPLE_MAX_RADIUS = 300f
        private const val RIPPLE_DURATION = 700L
    }
}