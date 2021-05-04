package kyklab.clockring

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import kyklab.clockring.databinding.ActivityMainBinding
import kyklab.clockring.service.OverlayService
import kyklab.clockring.utils.Prefs

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        val view = b.root
        setContentView(view)

        b.btnSetPerm.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        b.btnStartService.setOnClickListener {
            OverlayService.startService(this)
        }

        b.btnStopService.setOnClickListener {
            OverlayService.stopService(this)
        }

        b.btnPaddingMinus.setOnClickListener {
            if (Prefs.ringPaddingTop > 0)
            Prefs.ringPaddingTop = Prefs.ringPaddingTop - 1
            Log.e(TAG, "ringPaddingTop: ${Prefs.ringPaddingTop}")
        }

        b.btnPaddingPlus.setOnClickListener {
            Prefs.ringPaddingTop = Prefs.ringPaddingTop + 1
            Log.e(TAG, "ringPaddingTop: ${Prefs.ringPaddingTop}")
        }

        b.btnSizeMinus.setOnClickListener {
            if (Prefs.ringSize > 0)
            Prefs.ringSize = Prefs.ringSize - 1
            Log.e(TAG, "ringSize: ${Prefs.ringSize}")
        }

        b.btnSizePlus.setOnClickListener {
            Prefs.ringSize = Prefs.ringSize + 1
            Log.e(TAG, "ringSize: ${Prefs.ringSize}")
        }
    }
}