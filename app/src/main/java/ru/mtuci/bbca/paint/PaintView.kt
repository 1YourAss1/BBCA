package ru.mtuci.bbca.paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.Serializable
import kotlin.math.min
import kotlin.math.roundToInt


class PaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 20f
    }

    private val path: Path = Path()

    fun clear() {
        path.reset()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val minDim = min(width, height)

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(minDim, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(minDim, MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> Unit
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("super_state", super.onSaveInstanceState())
            putSerializable("path", PathSavedStateWrapper(path))
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val state = state as? Bundle ?: return
        path.set((state.getSerializable("path") as PathSavedStateWrapper).path)
        super.onRestoreInstanceState(state.getParcelable("super_state"))
    }

    private data class PathSavedStateWrapper(
        val path: Path
    ) : Serializable
}