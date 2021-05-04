package kyklab.clockring.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kyklab.clockring.App

object Prefs {
    private val pref = PreferenceManager.getDefaultSharedPreferences(App.context)
    private val editor = pref.edit()

    init {
        editor.apply()
    }

    var ringSize: Int
        get() = pref.getInt(Key.RING_SIZE, Key.RING_SIZE_DFEAULT)
        set(value) = editor.putInt(Key.RING_SIZE, value).apply()

    var ringPaddingTop: Int
        get() = pref.getInt(Key.RING_PADDING_TOP, Key.RING_PADDING_TOP_DEFAULT)
        set(value) = editor.putInt(Key.RING_PADDING_TOP, value).apply()

    fun registerPrefChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        pref.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterPrefChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        pref.unregisterOnSharedPreferenceChangeListener(listener)
    }

    object Key {
        const val RING_SIZE = "ring_size"
        const val RING_SIZE_DFEAULT = 64

        const val RING_PADDING_TOP = "ring_padding_top"
        const val RING_PADDING_TOP_DEFAULT = 8
    }
}