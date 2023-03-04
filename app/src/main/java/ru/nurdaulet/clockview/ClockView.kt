package ru.nurdaulet.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.cos
import kotlin.math.sin

private const val CLOCK_DEFAULT_HOUR_VALUE = 0
private const val CLOCK_DEFAULT_MINUTE_VALUE = 30
private const val CLOCK_DEFAULT_SECONDS_VALUE = 45
private const val PAINT_BRUSH_STROKE_WIDTH = 3f
private const val MAIN_CONTENT_OFFSET = 4f
private const val CLOCK_DEFAULT_RADIUS = 60f
private val numberList =
    mutableListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")


private const val BATTERY_WIDTH = 200f
private const val BATTERY_HEIGHT = 100f
private const val BATTERY_ZERO_COORDINATE = 0f
private const val BATTERY_WARNING_COLOR = Color.RED
private const val BATTERY_DEFAULT_COLOR = Color.GREEN

class ClockView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.clockViewStyle,
    defStyleRs: Int = R.style.ClockViewStyle
) : View(context, attrs, defStyleAttr, defStyleRs) {
    private var viewHeight = 0
    private var viewWidth = 0
    private var hourValue = 0
    private var minuteValue = 0
    private var secondsValue = 0
    private var clockRadius = 0f
    private var handTruncation = 0f
    private var hourHandTruncation = 0f
    private var fontSize = 0f
    private val numberRect = Rect()

    private val clockNumberTextColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }
    private val clockStrokeColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }
    private val clockArrowColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }
    private val clockBackgroundColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }

    /*private var batteryPercent = 0
    private var batteryCriticalPercent = 0
    private var mainContentOffset = context.toDp(MAIN_CONTENT_OFFSET).toInt()

    private lateinit var backgroundRect: Rect
    private lateinit var batteryLevelRect: Rect

    private val batteryStrokeColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }

    private val batteryPercentColor: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            strokeWidth = context.toDp(PAINT_BRUSH_STROKE_WIDTH)
        }
    }*/

    init {
        attrs?.let { initAttrs(it, defStyleAttr, defStyleRs) }
        initClock()
        //initRectangles()
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
                    clockArrowColor.color = getColor(
                        R.styleable.ClockView_clockArrowColor,
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
        /*height = height
        width = width
        padding = numeralSpacing + 50
        fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 13f,
            resources.displayMetrics
        ).toInt()
        val min = Math.min(height, width)
        radius = min / 2 - padding
        handTruncation = min / 20
        hourHandTruncation = min / 7
        paint = Paint()
        isInit = true*/

        handTruncation = clockRadius / 7
        hourHandTruncation = clockRadius / 5
        fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20f,
            resources.displayMetrics
        )
    }

    /*private fun initRectangles() {
        backgroundRect = Rect(
            context.toDp(BATTERY_ZERO_COORDINATE).toInt(),
            context.toDp(BATTERY_ZERO_COORDINATE).toInt(),
            context.toDp(BATTERY_WIDTH).toInt(),
            context.toDp(BATTERY_HEIGHT).toInt(),
        )

        batteryLevelRect = Rect(
            backgroundRect.left + mainContentOffset,
            backgroundRect.top + mainContentOffset,
            ((backgroundRect.right - mainContentOffset) *
                    (this.batteryPercent.toDouble() / 100.toDouble())).toInt(),
            backgroundRect.bottom - mainContentOffset

        )
    }*/

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

        /*drawBattery(canvas)
        drawBatteryPercent(canvas)*/
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
            clockRadius - clockStrokeColor.strokeWidth / 2 - clockBackgroundColor.strokeWidth / 2,
            clockBackgroundColor
        )
    }

    private fun drawNumbers(canvas: Canvas?) {
        clockNumberTextColor.textSize = fontSize
        for (number in numberList) {
            clockNumberTextColor.getTextBounds(number, 0, number.length, numberRect)
            val angle = Math.PI / 6 * (number.toInt() - 3)
            //TODO Fix magin numbers 8, 24, -60
            val x = (clockRadius + 8 + cos(angle) * (clockRadius - 60) - numberRect.width() / 2).toFloat()
            val y = (clockRadius + 24 + sin(angle) * (clockRadius - 60) - numberRect.width() / 2).toFloat()
            canvas?.drawText(number, x, y, clockNumberTextColor)
        }
    }

    private fun drawClockArrows(canvas: Canvas?) {
        drawArrow(canvas, (hourValue + minuteValue / 60) * 5f, true)
        drawArrow(canvas, minuteValue.toFloat(), false)
        drawArrow(canvas, secondsValue.toFloat(), false)
    }

    private fun drawArrow(canvas: Canvas?, loc: Float, isHour: Boolean) {
        val angle = Math.PI * loc / 30 - Math.PI / 2
        val handRadius = if (isHour) {
            clockRadius - handTruncation - hourHandTruncation
        } else {
            clockRadius - handTruncation
        }
        canvas?.drawLine(
            clockRadius,
            clockRadius,
            (clockRadius + cos(angle) * handRadius).toFloat(),
            (clockRadius + sin(angle) * handRadius).toFloat(),
            clockArrowColor
        )
    }

    /*private fun drawBattery(canvas: Canvas?) {
        canvas?.drawRect(backgroundRect, batteryStrokeColor)
    }

    private fun drawBatteryPercent(canvas: Canvas?) {

        if (batteryPercent == 0) {
            drawEmptyBattery(canvas)
        } else {
            canvas?.drawRect(batteryLevelRect, batteryPercentColor)
        }
    }

    private fun drawEmptyBattery(canvas: Canvas?) {
        // As I should draw fully empty battery, there is no point to convert values .toDp()
        canvas?.drawRect(
            0f,
            0f,
            0f,
            0f,
            batteryPercentColor
        )
    }*/

    /*fun setBatteryPercent(percent: Int) {

        batteryPercent = percent.coerceIn(0, 100)

        if (batteryPercent <= batteryCriticalPercent) {
            batteryPercentColor.color = BATTERY_WARNING_COLOR
        } else {
            batteryPercentColor.color = BATTERY_DEFAULT_COLOR
        }

        initRectangles()
        requestLayout()
        invalidate()
    }*/

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
        //setBatteryPercent(state.batteryPercent)
    }

    @Parcelize
    class SavedState(
        val hourValue: Int,
        val minuteValue: Int,
        val secondsValue: Int,
        @IgnoredOnParcel val source: Parcelable? = null
    ) : BaseSavedState(source)
}