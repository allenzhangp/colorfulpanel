package com.zp.panelviewdemo

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.BounceInterpolator

/*
*   Created by zhangping on 2018/11/6
*
*/
class ColorfulPanelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var startColor1: Int = 0
    private var endColor1: Int = 0
    private var startColor2: Int = 0
    private var endColor2: Int = 0
    private var startColor3: Int = 0
    private var endColor3: Int = 0
    private var shaderPaint1: Paint
    private var shaderPaint2: Paint
    private var shaderPaint3: Paint

    private var shaderGrayPaint: Paint//内部无状态的灰色圆
    private val VIEW_WIDTH_DEFAULT: Int = 200//空间默认的宽度
    private val SHADER_WIDTH: Int = 14//外圈三个圆弧的宽度

    private val SMALL_SHADER_WIDTH: Int = 2
    private val DRAW_PADING: Int = 20
    private val OFFSET = 20
    private val angle = 135f
    private var paintPointer: Paint//中间指针画笔
    private var paintPinterCircle: Paint//中间圆的画笔

    private var paintProgress: Paint
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var rectF2: RectF
    private lateinit var rectF1: RectF
    private var mMinCircleRadius: Int = 0
    val smalCirleOffset = 10

    private var valueAnimator: ValueAnimator? = null
    private var animatorDuration: Long = 2500
    var mInterpolator: TimeInterpolator
    var mPercent: Float = 0f

    init {

        startColor1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.redEnd, context.theme)
        } else {
            resources.getColor(R.color.redEnd)
        }
        endColor1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.redStart, context.theme)
        } else {
            resources.getColor(R.color.redStart)
        }

        startColor2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.yellowStart, context.theme)
        } else {
            resources.getColor(R.color.yellowStart)
        }
        endColor2 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.yellowEnd, context.theme)
        } else {
            resources.getColor(R.color.yellowEnd)
        }

        startColor3 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.greenStart, context.theme)
        } else {
            resources.getColor(R.color.greenStart)
        }
        endColor3 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.greenEnd, context.theme)
        } else {
            resources.getColor(R.color.greenEnd)
        }

        shaderPaint1 = Paint().apply {
            isAntiAlias = true
            strokeWidth = dpToPx(SHADER_WIDTH).toFloat()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isDither = true
        }


        shaderPaint2 = Paint().apply {
            isAntiAlias = true
            strokeWidth = dpToPx(SHADER_WIDTH).toFloat()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isDither = true
        }


        shaderPaint3 = Paint().apply {
            isAntiAlias = true
            strokeWidth = dpToPx(SHADER_WIDTH).toFloat()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isDither = true
        }


        shaderGrayPaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = dpToPx(SMALL_SHADER_WIDTH).toFloat()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isDither = true
            color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(R.color.shaderGray, context.theme)
            } else {
                resources.getColor(R.color.shaderGray)
            }
        }


        paintPointer = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
            isDither = true
            color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(R.color.shaderGrayCC, context.theme)
            } else {
                resources.getColor(R.color.shaderGrayCC)
            }
        }


        paintPinterCircle = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            isDither = true
            color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(R.color.shaderGrayCC, context.theme)
            } else {
                resources.getColor(R.color.shaderGrayCC)
            }
            //要在setcolor之后，否者无效
            alpha = 75
        }


        paintProgress = Paint().apply {
            isAntiAlias = true
            strokeWidth = dpToPx(SMALL_SHADER_WIDTH).toFloat()
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isDither = true
        }


        mInterpolator = BounceInterpolator()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        mWidth = if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.UNSPECIFIED) {
            MeasureSpec.getSize(widthMeasureSpec)
        } else {
            //默认200的宽度
            paddingLeft + dpToPx(VIEW_WIDTH_DEFAULT) + paddingRight
        }

        mHeight = if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.UNSPECIFIED) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            paddingTop + paddingBottom + dpToPx(VIEW_WIDTH_DEFAULT)
        }

        //保证是个矩形
        if (mWidth > mHeight) {
            mWidth = mHeight
        } else {
            mHeight = mWidth
        }

        setMeasuredDimension(mWidth, mHeight)
    }

    //渐变色初始化，在width和height计算出来之后
    private fun initShader() {
        val shader1 = SweepGradient(mWidth / 2f, mHeight / 2f, intArrayOf(startColor1, endColor1), floatArrayOf(0f, 0.25f))
        val shader2 = SweepGradient(mWidth / 2f, mHeight / 2f, intArrayOf(startColor2, endColor2), floatArrayOf(0f, 0.25f))
        val shader3 = SweepGradient(mWidth / 2f, mHeight / 2f, intArrayOf(startColor3, endColor3), floatArrayOf(0f, 0.25f))

        val gradientMatrix = Matrix()
        gradientMatrix.setRotate(angle - 10, mWidth / 2f, mHeight / 2f)
        shader1.setLocalMatrix(gradientMatrix)

        val gradientMatrix2 = Matrix()
        gradientMatrix2.setRotate(angle + 90 - 10, mWidth / 2f, mHeight / 2f)
        shader2.setLocalMatrix(gradientMatrix2)

        val gradientMatrix3 = Matrix()
        gradientMatrix3.setRotate(angle + DRAW_PADING + 180 - 10, mWidth / 2f, mHeight / 2f)
        shader3.setLocalMatrix(gradientMatrix3)

        shaderPaint1.shader = shader1
        shaderPaint2.shader = shader2
        shaderPaint3.shader = shader3


        val shader4 = SweepGradient(mWidth / 2f, mHeight / 2f, intArrayOf(startColor1, endColor2, endColor3), floatArrayOf(0f, 1 / 2f, 3 / 4f))
        val gradientMatrix4 = Matrix()
        gradientMatrix4.setRotate(angle - 10, mWidth / 2f, mHeight / 2f)
        shader4.setLocalMatrix(gradientMatrix4)
        paintProgress.shader = shader4//进度的圆弧

        updateOval()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制圆弧
        drawProgress(canvas)

        //绘制指针
        drawerPointer(canvas, mPercent)
    }

    private fun drawerPointer(canvas: Canvas, percent: Float) {
        val realAngle0: Double
        val realAngle1: Double
        val realAngle2: Double
        val realAngle3: Double

        if (percent <= 0f) {
            realAngle0 = 90.0 - 1
            realAngle1 = 90.0 - 25
            realAngle2 = 90.0 + 25
            realAngle3 = 90.0 + 1
        } else {
            realAngle0 = 270.0 * percent + angle - 1
            realAngle1 = 270.0 * percent + angle - 25
            realAngle2 = 270.0 * percent + angle + 25
            realAngle3 = 270.0 * percent + angle + 1
        }
        val angleRadians: Double = realAngle0 * Math.PI / 180 //换算成弧度
        val x0 = mMinCircleRadius * 4f * Math.cos(angleRadians).toFloat() + mWidth / 2f
        val y0 = mMinCircleRadius * 4f * Math.sin(angleRadians).toFloat() + mWidth / 2f

        val x1 = mMinCircleRadius * 0.7f * Math.cos(realAngle1 * Math.PI / 180).toFloat() + mWidth / 2f
        val y1 = mMinCircleRadius * 0.7f * Math.sin(realAngle1 * Math.PI / 180).toFloat() + mWidth / 2f

        val x2 = mMinCircleRadius * 0.7f * Math.cos(realAngle2 * Math.PI / 180).toFloat() + mWidth / 2f
        val y2 = mMinCircleRadius * 0.7f * Math.sin(realAngle2 * Math.PI / 180).toFloat() + mWidth / 2f

        val x3 = mMinCircleRadius * 4f * Math.cos(realAngle3 * Math.PI / 180).toFloat() + mWidth / 2f
        val y3 = mMinCircleRadius * 4f * Math.sin(realAngle3 * Math.PI / 180).toFloat() + mWidth / 2f


        val pathPointer = Path()
        pathPointer.moveTo(x1, y1)
        pathPointer.lineTo(x0, y0)
        pathPointer.lineTo(x3, y3)//让箭头看起来比较圆润
        pathPointer.lineTo(x2, y2)
        pathPointer.close()

        val pathCircle1 = Path()
        pathCircle1.addCircle(mWidth / 2f, mWidth / 2f, mMinCircleRadius * 0.7f, Path.Direction.CW)

        val pathCircle = Path()//有透明度的圆
        pathCircle.addCircle(mWidth / 2f, mWidth / 2f, mMinCircleRadius * 1.3f, Path.Direction.CW)


        canvas.drawPath(pathCircle1, paintPointer)//指针，根部的实心圆
        canvas.drawPath(pathPointer, paintPointer)//指针

        canvas.drawPath(pathCircle, paintPinterCircle)//有透明度的圆

        drawerProgress(canvas, percent)
    }

    /**
     * 绘制当前进度
     *
     */
    private fun drawerProgress(canvas: Canvas, percent: Float) {
        val realAngle = 270 * percent + angle
        val angleRadians = realAngle * Math.PI / 180 //换算成弧度


        paintProgress.style = Paint.Style.STROKE
        //进度圆弧
        canvas.drawArc(rectF1, angle, realAngle - angle, false, paintProgress)

        if (percent > 0f) {
            val radius = (mWidth - OFFSET * 2 - dpToPx(SHADER_WIDTH) * 2 - smalCirleOffset * 2) / 2
            val x0 = radius * Math.cos(angleRadians).toFloat() + mWidth / 2f
            val y0 = radius * Math.sin(angleRadians).toFloat() + mWidth / 2f
            paintProgress.style = Paint.Style.FILL
            canvas.drawCircle(x0, y0, 10f, paintProgress)//进度头上的小圆
        }
    }

    private fun updateOval() {
        //外部大圆矩形
        rectF2 = RectF(0f + OFFSET,
                0f + OFFSET,
                mWidth.toFloat() - OFFSET,
                mHeight.toFloat() - OFFSET)


        //内部小圆的矩形
        rectF1 = RectF(0f + OFFSET + dpToPx(SHADER_WIDTH) + smalCirleOffset,
                0f + OFFSET + dpToPx(SHADER_WIDTH) + smalCirleOffset,
                mWidth.toFloat() - OFFSET - dpToPx(SHADER_WIDTH) - smalCirleOffset,
                mHeight.toFloat() - OFFSET - dpToPx(SHADER_WIDTH) - smalCirleOffset)

        mMinCircleRadius = mWidth / 15
    }

    //画最外层的刻度盘
    private fun drawProgress(canvas: Canvas) {
        //三短彩色圆环
        canvas.drawArc(rectF2, angle, 90f - DRAW_PADING, false, shaderPaint1)
        canvas.drawArc(rectF2, angle + 90, 90f, false, shaderPaint2)
        canvas.drawArc(rectF2, angle + 180 + DRAW_PADING, 90f - DRAW_PADING, false, shaderPaint3)

        //内部270度的灰色圆环
        canvas.drawArc(rectF1, angle, 90f * 3, false, shaderGrayPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initShader()
    }

    private var oldPercent: Float = 0f
    private var lastAnimatorTimeLong: Long = 0
    fun startAnimator(cPercent: Float) {
        if (cPercent >= 1.0) {
            mPercent = 1.0f
        } else if (cPercent >= 0f) {
            mPercent = cPercent
        } else {
            mPercent = 0f
        }

        if (oldPercent != mPercent || System.currentTimeMillis() - lastAnimatorTimeLong >= 9000L) {
            oldPercent = mPercent

            if (valueAnimator != null && valueAnimator!!.isRunning) {
                valueAnimator?.cancel()
            }
            valueAnimator = ValueAnimator.ofFloat(0f, mPercent)
            valueAnimator?.apply {
                duration = animatorDuration
                interpolator = mInterpolator
                addUpdateListener {
                    mPercent = it.animatedValue as Float
                    invalidate()
                }
            }
            valueAnimator?.start()
        }

        lastAnimatorTimeLong = System.currentTimeMillis()


    }

    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
    }

    fun spToPx(sp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), resources.displayMetrics).toInt()
    }
}