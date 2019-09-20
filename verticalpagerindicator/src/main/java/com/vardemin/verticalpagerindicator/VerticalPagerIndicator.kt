package com.vardemin.verticalpagerindicator

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import java.lang.Exception

class VerticalPagerIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //CONST VALUES
    private val paintSelected: Paint
    private val paintSecondary: Paint
    private val paintText: Paint
    private val paddingHorizontal: Float
    private val duration: Int
    private val spacing: Float
    private val gravity: Int

    //DIFFERS
    private var numTextSize: Float
    private var baseDotSize = 0f
    private var cx = 0f
    private var center = 0f
    private var baseOffset = 0f

    private var animator: ValueAnimator? = null
    private var animationOffset: Float = 0f

    private var currentPosition: Int = 0
    private var targetPosition: Int = 0
    private var indicators: ArrayList<Indicator> = arrayListOf()

    //PUBLIC
    public var numPages: Int
        get() = indicators.size
        set(value) {
            indicators.clear()
            indicators.addAll(List(value) { Indicator() })
            indicators.trimToSize()
        }

    public var indicatorClickListener: ((position: Int) -> Unit)? = null
    public var indicatorAnimationListener: ((position: Int) -> Unit)? = null

    init {
        val colorSelected: Int
        val colorSecondary: Int
        val colorText: Int
        val textFont: Typeface
        if (attrs != null) {
            val a =
                context.theme.obtainStyledAttributes(attrs, R.styleable.VerticalPagerIndicator, defStyleAttr, 0)
            try {
                colorSelected = a.getColor(R.styleable.VerticalPagerIndicator_vpi_colorSelected,
                    ContextCompat.getColor(context, R.color.colorPrimary))
                colorSecondary = a.getColor(R.styleable.VerticalPagerIndicator_vpi_colorSecondary,
                    ContextCompat.getColor(context, R.color.colorSecondary))
                colorText = a.getColor(R.styleable.VerticalPagerIndicator_vpi_colorText,
                    ContextCompat.getColor(context, android.R.color.white))
                numTextSize = a.getDimension(R.styleable.VerticalPagerIndicator_vpi_sizeText, spToPixel(14f, context))
                textFont = try {
                    ResourcesCompat.getFont(context, a.getResourceId(R.styleable.VerticalPagerIndicator_vpi_fontText, 0)) ?: Typeface.DEFAULT
                } catch (ex: Exception) {
                    Typeface.DEFAULT
                }

                paddingHorizontal =  a.getDimension(R.styleable.VerticalPagerIndicator_vpi_paddingHorizontal, dpToPixel(16f, context))
                duration = a.getInt(R.styleable.VerticalPagerIndicator_vpi_duration, 300)
                spacing = a.getDimension(R.styleable.VerticalPagerIndicator_vpi_spacing, dpToPixel(24f, context))
                gravity = a.getInt(R.styleable.VerticalPagerIndicator_vpi_gravity, 0) //fill
            } finally {
                a.recycle()
            }
        }
        else {
            colorSelected = ContextCompat.getColor(context, R.color.colorPrimary)
            colorText = ContextCompat.getColor(context, android.R.color.white)
            numTextSize = spToPixel(14f, context)
            textFont = Typeface.DEFAULT

            paddingHorizontal = dpToPixel(16f, context)
            duration = 300
            spacing = dpToPixel(8f, context)
            gravity = 0
            colorSecondary = ContextCompat.getColor(context, R.color.colorSecondary)
        }
        paintSelected = Paint().apply{
            color = colorSelected
            isAntiAlias = true
        }
        paintSecondary = Paint().apply {
            color = colorSecondary
            isAntiAlias = true
            //xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        paintText = Paint().apply {
            color = colorText
            textSize = numTextSize
            typeface = textFont
            isAntiAlias = true
            isSubpixelText = true
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        animationOffset = currentPosition.toFloat()
        baseDotSize = (width - (paddingHorizontal*2))
        cx = width/2f
        center = height/2f
        baseOffset = baseDotSize/2+spacing
        calculateIndicators()
        /*var space = 0f
        while (space + spacing < height) {
            space += (baseSecondaryDotSize + spacing)
            if (space <= height) {
                maxIntems ++
            }
        }*/

    }

    override fun onDraw(canvas: Canvas?) {
        for (i in 0 until indicators.size) {
            val primary = indicators[i].y >= center - baseDotSize/2 && indicators[i].y <= center + baseDotSize/2
            canvas?.drawCircle(cx, indicators[i].y, (baseDotSize/2f)*indicators[i].scale, if (primary) paintSelected else paintSecondary)
            if (indicators[i].y == center) {
                val text = (i+1).toString()

                canvas?.drawText(text, cx - paintText.measureText(text)/2f, center - (paintText.descent() + paintText.ascent()) / 2, paintText)
            }
        }
    }

    public fun moveTo(page: Int = 0) {
        val _page = clampInt(page, 0, indicators.size-1)
        if (currentPosition == _page && animationOffset == currentPosition.toFloat()) return
        targetPosition = _page
        setupAnimations()
    }

    private fun setupAnimations() {
        animator?.cancel()
        animator?.removeAllUpdateListeners()
        animator = null
        animator = ValueAnimator().apply {
            setFloatValues(animationOffset, targetPosition.toFloat())
            addUpdateListener(updateListener)
        }
        animator?.start()
    }

    private fun calculateIndicators() {
        val offset = animationOffset * baseOffset
        for (i in 0 until indicators.size) {
            val dotPos = center + (i * baseOffset) - offset
            indicators[i].y = dotPos
            indicators[i].scale = calculateDotScale(dotPos, center, center-baseOffset, 1f)
        }
    }

    private val updateListener = ValueAnimator.AnimatorUpdateListener {
        animationOffset = it.animatedValue as Float
        if (animationOffset == targetPosition.toFloat()) {
            currentPosition = targetPosition
            indicatorAnimationListener?.invoke(currentPosition)
        }
        calculateIndicators()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_UP){
            for (i in 0 until indicators.size) {
                if (isInRectangle(cx, indicators[i].y, baseDotSize/2, event.x, event.y)) {
                    indicatorClickListener?.invoke(i)
                    break
                }
            }
            return performClick()
        }
        return true
    }

    public fun initWith(pager: ViewPager) {
        numPages = pager.adapter?.count ?: 0
        pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                moveTo(position)
            }

        })
    }

    public fun initWith(pager: ViewPager2) {
        numPages = pager.adapter?.itemCount ?: 0
        pager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                moveTo(position)
            }
        })
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState._currentPosition = currentPosition
        savedState._numberOfIndicators = numPages
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            currentPosition = state._currentPosition
            numPages = state._numberOfIndicators
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private inner class SavedState: BaseSavedState {
        var _currentPosition = 0
        var _numberOfIndicators = 0

        constructor(source: Parcel) : super(source) {
            _currentPosition = source.readInt()
            _numberOfIndicators = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(_numberOfIndicators)
            out.writeInt(_currentPosition)
        }
        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}