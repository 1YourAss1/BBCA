package ru.mtuci.bbca.core

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class TrackingFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var onTouchEventListener: ((MotionEvent?) -> Unit)? = null

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        onTouchEventListener?.invoke(ev)
        return super.onInterceptTouchEvent(ev)
    }

    fun setOnTouchEventListener(listener: ((MotionEvent?) -> Unit)?) {
        onTouchEventListener = listener
    }
}