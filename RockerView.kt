package com.example.studyview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.DITHER_FLAG
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import android.graphics.Shader

import android.graphics.LinearGradient


/**
 * @author 战神族灬小火
 * @date 2021/9/28 16:31
 * @description
 */
class RockerView : View {

    //onMeasure  决定了view本身大小多少
    //onLayout   决定了View在ViewGroup中的位置如何
    //onDraw     决定了如何绘制这个View
    /**********************************************************/
    //requestLayout View重新调用一次layout过程 重新定位
    //invalidate    View重新调用一次draw过程
    //forceLayout   标识View在下一次重绘，需要重新调用layout过程
    /**********************************************************/
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    companion object {
        private const val TAG = "===RockerView==="
    }

    private lateinit var rockerCallBack: RockerCallBack

    //默认宽高
    private val defaultWHSize = 600

    //centrePoint 圆心位置
    private var centrePointX = 0F
    private var centrePointY = 0F

    //大圆半径
    private var bigCircleC = 0F

    //小圆半径 原来的4分之一
    private var smallCircleC = 0F

    //移动的位置
    private var moveX = 0f
    private var moveY = 0f

    //标签大小 大圆半径的16分之一
    private var labelSize = 20F

    /**
     * 测量大小
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //宽
        val widthModel = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        //高
        val heightModel = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        //模式区分
        //宽模式
        val width = when (widthModel) {
            MeasureSpec.EXACTLY -> widthSize
            else -> defaultWHSize
        }

        //高模式
        val height = when (heightModel) {
            MeasureSpec.EXACTLY -> heightSize
            else -> defaultWHSize
        }

        setMeasuredDimension(width, height)


        centrePointX = width / 2.toFloat()
        centrePointY = height / 2.toFloat()
        labelSize = (width / 16.toFloat())
        bigCircleC = (width / 2.toFloat()) - (labelSize * 2)
        smallCircleC = bigCircleC / 4
        moveX = centrePointX
        moveY = centrePointY

    }

    //大圆画笔
    private val mBigBgPaint = Paint(ANTI_ALIAS_FLAG and DITHER_FLAG)

    //小圆画笔
    private val mSmallBgPaint = Paint(ANTI_ALIAS_FLAG and DITHER_FLAG)

    //小圆画笔 内边
    private val mSmallBorderPaint = Paint(ANTI_ALIAS_FLAG and DITHER_FLAG)

    //标签画笔
    private val mIndicateBgPaint = Paint(ANTI_ALIAS_FLAG and DITHER_FLAG)

    //标签边画笔
    private val mLabelBorderPaint = Paint(ANTI_ALIAS_FLAG and DITHER_FLAG)

    //箭头画笔
    private val mArrowPaint = Paint(ANTI_ALIAS_FLAG and DITHER_FLAG)

    //是否绘制三角形标签
    private var isDrawLabel = false

    //角度 0-360
    private var angle: Float = 0F

    //弧度
    private var radian: Float = 0F

    //当前的斜边
    private var c: Float = 0F

    //是否循环
    private var isLoop = false


    private var smallCircleColor = "#803A83C8"

    /**
     * 触摸事件
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isLoop = true
                loopCallBack()

            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> isLoop = false
        }



        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
//                Log.e(
//                    TAG,
//                    "onTouchEvent:=== ${event.x}=== ${event.y}=== ${event.rawX}=== ${event.rawY}",
//                )
                val x = event.x
                val y = event.y

                //|c-x|=a  |c-y|=b  a^+b^=c^
                val a = x - centrePointX
                val b = -(y - centrePointY)

                //先得到角度 c*sin@=b c*cos@=a
                //弧度
                radian = atan2((y - centrePointY), ((x - centrePointX)))
                val androidAngle = radian * (180 / Math.PI).toFloat()
                //角度
                angle = if (androidAngle <= 0) {
                    abs(androidAngle)
                } else {
                    180 + (180 - androidAngle)
                }
                Log.e(TAG, "onTouchEvent: ===角度=======$angle")
                //判断是否在圆形内
                c = (sqrt(a.pow(2) + b.pow(2)))
                Log.e(TAG, "onTouchEvent: ===a=$a===b=$b===c=$c")
                if (c <= bigCircleC - smallCircleC) {
                    Log.e(TAG, "onTouchEvent: 内")
                    moveX = x
                    moveY = y
                } else {
                    Log.e(TAG, "onTouchEvent: 外")
                    moveX = ((cos(radian) * (bigCircleC - smallCircleC))) + centrePointX
                    moveY = ((sin(radian) * (bigCircleC - smallCircleC))) + centrePointY
                }
                isDrawLabel = true
                isLoop = true
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP,
            -> {
                moveX = centrePointX
                moveY = centrePointY
                isDrawLabel = false
                isLoop = false
            }
        }
        invalidate()
        return true
    }


    /**
     * 回调
     */
    fun getRockerCallBack(rockerCallBack: RockerCallBack) {
        this.rockerCallBack = rockerCallBack
    }

    /**
     * 循环定时回调
     */
    private fun loopCallBack() {
        Thread {
            while (isLoop) {
                if (c > bigCircleC) {
                    c = bigCircleC
                }
                rockerCallBack.onCallBack(angle = angle, loadMeasuring = c / bigCircleC * 100)
                Thread.sleep(100)
            }
        }.start()
    }

    /**
     * 初始化画笔参数
     */
    private fun initPaint() {
        Log.e(TAG, "initPaint: Big==R===$bigCircleC", )
        Log.e(TAG, "initPaint: Label=S===$labelSize", )
        val colors = intArrayOf(Color.parseColor("#ffffff"), Color.parseColor("#000000"), Color.parseColor("#000000"), Color.parseColor("#ffffff"))
        val position = floatArrayOf(0.0f,0.2f,0.8f, 1.0f)
        val linearGradient: Shader = LinearGradient(0f, 0f, (Math.PI*(bigCircleC+(labelSize/4))/2).toFloat(),0f,colors,null, Shader.TileMode.CLAMP)

        //判断背景是不是颜色
        if (background is ColorDrawable) {
            mBigBgPaint.color = (background as ColorDrawable).color

            background = null
        } else {
            (background as BitmapDrawable).bitmap
            background = null
        }

        mSmallBgPaint.color = Color.parseColor(smallCircleColor)
        mSmallBorderPaint.color = Color.parseColor("#0297F5")
        mSmallBorderPaint.style = Paint.Style.STROKE
        mSmallBorderPaint.strokeWidth = 5f
        mIndicateBgPaint.color = Color.parseColor("#0059c4")



        mLabelBorderPaint.style = Paint.Style.STROKE
        mLabelBorderPaint.strokeWidth = labelSize / 4


        mArrowPaint.color = Color.parseColor("#4DFFFFFF")


    }


    /**
     *  绘制
     */
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(centrePointX, centrePointY, bigCircleC, mBigBgPaint)
        arrowDraw(canvas)
        canvas?.drawCircle(moveX, moveY, smallCircleC, mSmallBgPaint)
        canvas?.drawCircle(moveX, moveY, smallCircleC - (smallCircleC / 5), mSmallBorderPaint)
//        actionDraw(canvas)
        actionDraw2(canvas)
    }

    /**
     * 四个小箭头
     */
    private fun arrowDraw(canvas: Canvas?) {
        val path = Path()
        //箭头高度
        val arrowH = bigCircleC / 5
        path.moveTo(centrePointX, centrePointY - (bigCircleC / 5 * 4))//1
        path.rLineTo(-arrowH, arrowH)//2
        path.rLineTo(arrowH / 2, 0f)//3
        path.rLineTo(arrowH / 2, -arrowH / 2)//4
        path.rLineTo(arrowH / 2, arrowH / 2)//5
        path.rLineTo(arrowH / 2, 0f)//6
        path.close()

        val matrix = Matrix()

        for (i in 0..3) {
            matrix.reset()
            matrix.setRotate(90f * i, centrePointX, centrePointY)
//            canvas?.rotate(90f * i, centrePointX, centrePointY)
            path.transform(matrix)
            canvas?.drawPath(path, mArrowPaint)

        }
//        canvas?.rotate(180f , centrePointX, centrePointY)
        canvas?.save()

    }

    /**
     * 动态绘制
     */
    private fun actionDraw(canvas: Canvas?) {
        if (isDrawLabel) {
            /****************************************************/
            //边缘移动扇形
            //三角边缘移动箭头
            val matrix = Matrix()
            matrix.reset()
            matrix.setSinCos(
                sin(radian + (Math.PI / 2)).toFloat(),
                cos(radian + (Math.PI / 2)).toFloat(),
                centrePointX,
                centrePointY
            )
            //三角形路径
            val path = Path()
            //此点为多边形的起点
            path.rMoveTo(centrePointX, centrePointY - bigCircleC)
            path.rLineTo(0f, 0f)
            path.rLineTo(-labelSize / 2, 0f)
            path.rLineTo(labelSize / 2, -labelSize)
            path.rLineTo(labelSize / 2, labelSize)
            // 使这些点构成封闭的多边形
            path.close()
            path.transform(matrix)
            canvas?.drawPath(path, mIndicateBgPaint)
            /****************************************************/
            val positions = floatArrayOf(0.25f,0.3f,0.7f,0.75f)
            val colors = intArrayOf( Color.TRANSPARENT,Color.parseColor("#0059c4"),Color.parseColor("#0059c4"), Color.TRANSPARENT)
            val linearGradient: Shader = LinearGradient(0f, 0f,0f,height.toFloat(),colors,positions, Shader.TileMode.CLAMP)
            val matrix2= Matrix()
            matrix2.reset()
            matrix2.setRotate( - angle,centrePointX,centrePointY)
            linearGradient.setLocalMatrix(matrix2)
            mLabelBorderPaint.shader=linearGradient
            canvas?.drawArc(
                centrePointX - bigCircleC-(labelSize/8),
                centrePointY - bigCircleC-(labelSize/8),
                centrePointX + bigCircleC+(labelSize/8),
                centrePointY + bigCircleC+(labelSize/8),
                60F - angle,
                -120F,
                false,
                mLabelBorderPaint
            )
            /****************************************************/

        }
    }
    /**
     * 动态绘制 第二个方法
     */
    private fun actionDraw2(canvas: Canvas?){
        if (isDrawLabel) {
            /****************************************************/
            //边缘移动扇形
            val positions = floatArrayOf(0.25f,0.3f,0.7f,0.75f)
            val colors = intArrayOf( Color.TRANSPARENT,Color.parseColor("#0059c4"),Color.parseColor("#0059c4"), Color.TRANSPARENT)
            val linearGradient: Shader = LinearGradient(0f, 0f,0f,height.toFloat(),colors,positions, Shader.TileMode.CLAMP)
            val matrix= Matrix()
            matrix.setRotate( - angle,centrePointX,centrePointY)
            linearGradient.setLocalMatrix(matrix)
            mLabelBorderPaint.shader=linearGradient
            canvas?.drawArc(
                centrePointX - bigCircleC-(labelSize/8),
                centrePointY - bigCircleC-(labelSize/8),
                centrePointX + bigCircleC+(labelSize/8),
                centrePointY + bigCircleC+(labelSize/8),
                60F - angle,
                -120F,
                false,
                mLabelBorderPaint
            )
            canvas?.save()

            /****************************************************/
            //三角边缘移动箭头
            //三角形路径
            val path = Path()
            //此点为多边形的起点
            path.rMoveTo(centrePointX+bigCircleC, centrePointY )
            path.rLineTo(0f,-labelSize / 2 )
            path.rLineTo(labelSize,labelSize / 2)
            path.rLineTo(-labelSize,labelSize / 2 )
            // 使这些点构成封闭的多边形
            path.close()
            canvas?.rotate(-angle,centrePointX,centrePointY)
            canvas?.drawPath(path,mIndicateBgPaint)
            canvas?.save()
            /****************************************************/
        }
    }

    /**
     * 回调接口
     */
    interface RockerCallBack {
        fun onCallBack(angle: Float, loadMeasuring: Float)
    }

    /**
     * 初始化准备
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
       // initPaint()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        initPaint()
    }

    /**
     * 销毁时  注销
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isDrawLabel = false
        isLoop = false
    }


}