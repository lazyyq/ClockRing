package kyklab.clockring.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import kyklab.clockring.Const
import kyklab.clockring.R
import kyklab.clockring.utils.Prefs
import kyklab.clockring.utils.dpToPx
import kyklab.clockring.utils.getScreenRotation
import kyklab.clockring.utils.isScreenOn
import java.util.*

class OverlayService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = OverlayService::class.java.simpleName
    private val windowManager: WindowManager
        get() = getSystemService(WINDOW_SERVICE) as WindowManager
    private lateinit var ongoingNotification: Notification
    private lateinit var container: ViewGroup
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var animation: Animation

    // Receiver for receiving screen on/off state changes
    private val intentFilter by lazy {
        IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON) {
                Log.e(TAG, "screen on received, starting animation")
                refreshView()
            } else if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                Log.e(TAG, "screen off received, clearing animation")
                container.findViewById<ImageView>(R.id.imageView).clearAnimation()
                if (container.parent != null) {
                    windowManager.removeView(container)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        container = inflater.inflate(R.layout.overlay, null) as ViewGroup
        initViewProperties()
         updateViewProperties()
        setupNotifications()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Const.Intent.ACTION_STOP_SERVICE) {
            stopForeground(true)
            stopSelf()
        } else {
            startForeground(Const.Notification.ID_FOREGROUND_SERVICE, ongoingNotification)
            Prefs.registerPrefChangeListener(this)

            // Add overlay view
            updateViewProperties()
            refreshView()

            // Configure and start animation
            if (isScreenOn) {
                createAnimation()
                container.findViewById<ImageView>(R.id.imageView).startAnimation(animation)
            }

            registerReceiver(receiver, intentFilter)
        }

        return START_STICKY
    }

    private fun setupNotifications() {
        // Create notification channel
        val channel = NotificationChannel(
            Const.Notification.CHANNEL_FOREGROUND_SERVICE,
            "Service Running", NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Build foreground service notification
        ongoingNotification =
            Notification.Builder(this, Const.Notification.CHANNEL_FOREGROUND_SERVICE)
                .setContentTitle("Service is running")
                .build()
    }

    private fun initViewProperties() {
        container.findViewById<ImageView>(R.id.imageView).layoutParams.apply {
            width = dpToPx(this@OverlayService, Prefs.ringSize)
            height = dpToPx(this@OverlayService, Prefs.ringSize)
        }
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun updateViewProperties() {
        val rotation = getScreenRotation()
        when (rotation) {
            Surface.ROTATION_90 -> {
                container.setPadding(Prefs.ringPaddingTop, 0, 0, 0)
                params.gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
            }
            Surface.ROTATION_180 -> {
                container.setPadding(0, 0, 0, Prefs.ringPaddingTop)
                params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            }
            Surface.ROTATION_270 -> {
                container.setPadding(0, 0, Prefs.ringPaddingTop, 0)
                params.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
            }
            else -> {
                container.setPadding(0, Prefs.ringPaddingTop, 0, 0)
                params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            }
        }
    }

    private fun refreshView() {
        if (container.parent != null) {
            windowManager.removeView(container)
        }
        windowManager.addView(container, params)
        if (isScreenOn) {
            createAnimation()
            container.findViewById<ImageView>(R.id.imageView).startAnimation(animation)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        updateViewProperties()
        refreshView()
    }

    override fun onDestroy() {
        if (container.parent != null) {
            windowManager.removeView(container)
        }
        Prefs.unregisterPrefChangeListener(this)
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Prefs.Key.RING_PADDING_TOP -> {
                updateViewProperties()
            }
            Prefs.Key.RING_SIZE -> {
                container.findViewById<ImageView>(R.id.imageView).apply {
                    layoutParams.apply {
                        width = dpToPx(this@OverlayService, Prefs.ringSize)
                        height = dpToPx(this@OverlayService, Prefs.ringSize)
                    }
                    requestLayout()
                    if (isScreenOn) {
                        createAnimation()
                        startAnimation(animation)
                    }
                }
            }
        }
    }

    private fun createAnimation() {
        val c = Calendar.getInstance()
        val s = c.get(Calendar.SECOND)
        val ms = c.get(Calendar.MILLISECOND)
        val startAngle = ((s * 1000 + ms) / 60000f) * 360
        animation = RotateAnimation(
            DEFAULT_ANGLE + startAngle, DEFAULT_ANGLE + startAngle + 360f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 60000
            interpolator = LinearInterpolator()
            repeatMode = Animation.RESTART
            repeatCount = Animation.INFINITE
        }
    }

    companion object {
        private const val DEFAULT_ANGLE = 45f

        fun startService(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            intent.action = Const.Intent.ACTION_START_SERVICE
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            intent.action = Const.Intent.ACTION_STOP_SERVICE
            context.startForegroundService(intent)
        }
    }
}