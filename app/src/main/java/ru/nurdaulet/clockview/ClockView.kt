package ru.nurdaulet.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.cos
import kotlin.math.sin

class ClockView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.clockViewStyle,
    defStyleRs: Int = R.style.ClockViewStyle
) : View(context, attrs, defStyleAttr, defStyleRs) {

    private var hourValue = 0
    private var minuteValue = 0
    private var secondsValue = 0
    private var clockRadius = 0f
    private var arrowTruncation = 0f
    private var minuteArrowTruncation = 0f
    private var hourArrowTruncation = 0f
    private var hourArrowRadius = 0f
    private var minuteArrowRadius = 0f
    private var secondsArrowRadius = 0f
    private var fontSize = 0f
    private val numberRect = Rect()

    private val clockNumberTextColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            strokeWidth = context.toDp(CLOCK_NUMERALS_STROKE_WIDTH)
        }
    }
    private val clockStrokeColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(CLOCK_STROKE_WIDTH)
        }
    }
    private val clockHourArrowColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(HOUR_ARROW_STROKE_WIDTH)
        }
    }
    private val clockMinuteArrowColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(MINUTE_ARROW_STROKE_WIDTH)
        }
    }
    private val clockSecondsArrowColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(SECOND_ARROW_STROKE_WIDTH)
        }
    }
    private val clockBackgroundColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            strokeWidth = context.toDp(DEFAULT_STROKE_WIDTH)
        }
    }

    init {
        attrs?.let { initAttrs(it, defStyleAttr, defStyleRs) }
        initClock()
    }

    private fun initAttrs(attrs: AttributeSet, defStyleAttr: Int, defStyleRs: Int) {
        context.theme
            .obtainStyledAttributes(attrs, R.styleable.ClockView, defStyleAttr, defStyleRs)
            .apply {
                try {
                    hourValue = getInteger(
                        R.styleable.ClockView_hourValue,
                        CLOCK_DEFAULT_HOUR_VALUE
                    )
                    minuteValue = getInteger(
                        R.styleable.ClockView_minuteValue,
                        CLOCK_DEFAULT_MINUTE_VALUE
                    )
                    secondsValue = getInteger(
                        R.styleable.ClockView_secondsValue,
                        CLOCK_DEFAULT_SECONDS_VALUE
                    )
                    clockRadius =
                        getDimension(R.styleable.ClockView_clockRadius, CLOCK_DEFAULT_RADIUS)

                    clockNumberTextColor.color = getColor(
                        R.styleable.ClockView_clockNumberTextColor,
                        Color.BLACK
                    )
                    clockStrokeColor.color = getColor(
                        R.styleable.ClockView_clockStrokeColor,
                        Color.BLACK
                    )
                    clockHourArrowColor.color = getColor(
                        R.styleable.ClockView_clockHourArrowColor,
                        Color.BLACK
                    )
                    clockMinuteArrowColor.color = getColor(
                        R.styleable.ClockView_clockMinuteArrowColor,
                        Color.BLACK
                    )
                    clockSecondsArrowColor.color = getColor(
                        R.styleable.ClockView_clockSecondsArrowColor,
                        ContextCompat.getColor(context, R.color.orange)
                    )
                    clockBackgroundColor.color = getColor(
                        R.styleable.ClockView_clockBackgroundColor,
                        Color.WHITE
                    )
                } finally {
                    recycle()
                }
            }
    }

    private fun initClock() {
        fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20f,
            resources.displayMetrics
        )
        //Different truncation for arrows
        arrowTruncation = clockRadius / 7
        minuteArrowTruncation = clockRadius / 7
        hourArrowTruncation = clockRadius / 3

        //Different radius for each arrow
        hourArrowRadius = clockRadius - arrowTruncation - hourArrowTruncation
        minuteArrowRadius = clockRadius - arrowTruncation - minuteArrowTruncation
        secondsArrowRadius = clockRadius - arrowTruncation
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = (clockRadius * 2).toInt()
        val desiredHeight = (clockRadius * 2).toInt()
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec) + paddingLeft + paddingRight,
            resolveSize(desiredHeight, heightMeasureSpec) + paddingTop + paddingBottom
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

        drawClockCircle(canvas)
        drawNumbers(canvas)
        drawClockArrows(canvas)
    }

    private fun drawClockCircle(canvas: Canvas?) {
        canvas?.drawCircle(
            clockRadius,
            clockRadius,
            clockRadius - clockStrokeColor.strokeWidth / 2,
            clockStrokeColor
        )
        //Inner circle for background
        canvas?.drawCircle(
            clockRadius,
            clockRadius,
            clockRadius - clockBackgroundColor.strokeWidth,
            clockBackgroundColor
        )
    }

    private fun drawNumbers(canvas: Canvas?) {
        clockNumberTextColor.textSize = fontSize
        for (number in numberList) {
            clockNumberTextColor.getTextBounds(number, 0, number.length, numberRect)
            // some mathematics
            val angle = Math.PI / 6 * (number.toInt() - 3)
            val x =
                (clockRadius + cos(angle) * (clockRadius - NUMERAL_PADDING) - numberRect.width() / 2).toFloat()
            val y =
                (clockRadius + sin(angle) * (clockRadius - NUMERAL_PADDING) + numberRect.width() / 2).toFloat()
            canvas?.drawText(number, x, y, clockNumberTextColor)
        }
    }

    private fun drawClockArrows(canvas: Canvas?) {
        drawArrow(canvas, hourValue * 5f, ClockArrowType.Hour)
        drawArrow(canvas, minuteValue.toFloat(), ClockArrowType.Minute)
        drawArrow(canvas, secondsValue.toFloat(), ClockArrowType.Second)
    }

    private fun drawArrow(canvas: Canvas?, timeMoment: Float, arrowType: ClockArrowType) {
        var arrowRadius = 0f
        var angle = 0f
        var arrowColor = Paint()

        when (arrowType) {
            ClockArrowType.Hour -> {
                // Math formulae for each angle
                // Also considered value of minutes for hours & seconds for minutes
                angle =
                    (Math.PI * timeMoment / 30 - Math.PI / 2 + Math.PI / 180 * minuteValue / 2).toFloat()
                arrowRadius = hourArrowRadius
                arrowColor = clockHourArrowColor
            }
            ClockArrowType.Minute -> {
                angle =
                    (Math.PI * timeMoment / 30 - Math.PI / 2 + Math.PI / 180 * secondsValue / 10).toFloat()
                arrowRadius = minuteArrowRadius
                arrowColor = clockMinuteArrowColor
            }
            ClockArrowType.Second -> {
                angle = (Math.PI * timeMoment / 30 - Math.PI / 2).toFloat()
                arrowRadius = secondsArrowRadius
                arrowColor = clockSecondsArrowColor
            }
        }

        canvas?.drawLine(
            clockRadius,
            clockRadius,
            (clockRadius + cos(angle) * arrowRadius),
            (clockRadius + sin(angle) * arrowRadius),
            arrowColor
        )
    }

    fun setClockTime(hour: Int, minutes: Int, seconds: Int) {
        hourValue = if (hour > 12) hour - 12 else hour
        minuteValue = minutes
        secondsValue = seconds

        initClock()
        requestLayout()
        invalidate()
    }

    fun setClockRadius(radius: Float) {
        clockRadius = context.toDp(radius)

        initClock()
        requestLayout()
        invalidate()
    }

    private fun Context.toDp(value: Float): Float {
        return resources.displayMetrics.density * value
    }

    override fun onSaveInstanceState(): Parcelable {
        val state = super.onSaveInstanceState()
        return SavedState(hourValue, minuteValue, secondsValue, state)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state as SavedState
        super.onRestoreInstanceState(state.superState)
        setClockTime(state.hourValue, state.minuteValue, state.secondsValue)
    }

    @Parcelize
    class SavedState(
        val hourValue: Int,
        val minuteValue: Int,
        val secondsValue: Int,
        @IgnoredOnParcel val source: Parcelable? = null
    ) : BaseSavedState(source)

    companion object {
        private const val CLOCK_DEFAULT_HOUR_VALUE = 0
        private const val CLOCK_DEFAULT_MINUTE_VALUE = 30
        private const val CLOCK_DEFAULT_SECONDS_VALUE = 45
        private const val DEFAULT_STROKE_WIDTH = 3f
        private const val HOUR_ARROW_STROKE_WIDTH = 6f
        private const val MINUTE_ARROW_STROKE_WIDTH = 3f
        private const val SECOND_ARROW_STROKE_WIDTH = 1f
        private const val CLOCK_NUMERALS_STROKE_WIDTH = 1f
        private const val CLOCK_STROKE_WIDTH = 6f
        private const val CLOCK_DEFAULT_RADIUS = 100f
        private const val NUMERAL_PADDING = 60
        private val numberList =
            mutableListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
    }
}