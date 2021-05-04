package kyklab.clockring.utils

import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.os.PowerManager
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager

fun dpToPx(context: Context, dp: Int): Int {
    val dm = context.resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), dm).toInt()
}

val Context.isScreenOn: Boolean
    get() = (getSystemService(Service.POWER_SERVICE) as PowerManager).isInteractive

fun Context.getScreenRotation(): Int? {
    /*return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        display?.rotation
    } else {
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.rotation
    }*/
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return manager.defaultDisplay.rotation
}

fun getScreenRotation(config: Configuration): Int? {
    return try {
        config::class.java.declaredFields.forEach { Log.e("REF decfields", "${it.name}") }
        config::class.java.fields.forEach { Log.e("REF fields", "${it.name}") }
        val fieldConfig = config::class.java.getDeclaredField("windowConfiguration")
        fieldConfig.isAccessible = true
        val winConfig = fieldConfig.get(config)

        val getRotation = winConfig::class.java.getDeclaredMethod("getRotation")
        getRotation.isAccessible = true
        getRotation(winConfig) as Int
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}