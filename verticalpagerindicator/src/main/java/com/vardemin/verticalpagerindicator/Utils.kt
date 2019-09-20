package com.vardemin.verticalpagerindicator

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import android.util.TypedValue




/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 *
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun dpToPixel(dp: Float, context: Context): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

/**
 * This method converts device specific pixels to density independent pixels.
 *
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @param context Context to get resources and device specific display metrics
 * @return A float value to represent dp equivalent to px value
 */
fun pixelsToDp(px: Float, context: Context): Float {
    return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun spToPixel(sp: Float, context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        sp,
        context.resources.displayMetrics
    )
}

fun clampInt(target: Int, minVal: Int, maxVal: Int): Int {
    return min(max(target, minVal), maxVal)
}

fun calculateDotScale(dotPos: Float, centerPos: Float, maxOffset: Float, maxScale: Float): Float {
    if (abs(dotPos) > centerPos + maxOffset || abs(dotPos) < centerPos - maxOffset) return 0f
    //return 0.1f + ((maxOffset - (abs(dotPos)-centerPos))/maxOffset) * (maxScale - 0.1f)
    val coordinateOffset = centerPos - maxOffset
    val _dotPos = if (dotPos > centerPos) centerPos - min((dotPos - centerPos), centerPos) else dotPos

    return 0.2f + (abs(_dotPos) - coordinateOffset)*(maxScale - 0.2f)/(centerPos - coordinateOffset)
}

fun isInRectangle(
    centerX: Float, centerY: Float, radius: Float,
    x: Float, y: Float
): Boolean {
    return x >= centerX - radius && x <= centerX + radius &&
            y >= centerY - radius && y <= centerY + radius
}

fun isPointInCircle(
    centerX: Float, centerY: Float,
    radius: Float, x: Float, y: Float
): Boolean {
    if (isInRectangle(centerX, centerY, radius, x, y)) {
        var dx = centerX - x
        var dy = centerY - y
        dx *= dx
        dy *= dy
        val distanceSquared = dx + dy
        val radiusSquared = radius * radius
        return distanceSquared <= radiusSquared
    }
    return false
}