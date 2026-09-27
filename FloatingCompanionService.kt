package com.petmorph.ai.overlay

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.petmorph.ai.MainActivity
import com.petmorph.ai.PetMorphApp
import com.petmorph.ai.R
import kotlinx.coroutines.runBlocking
import kotlin.math.abs

/**
 * Foreground service hosting the floating companion via WindowManager.
 * Drag to move, tap to play click reaction + sound, double tap to open app,
 * long press then pinch-zone buttons handled via control panel (size/opacity).
 */
class FloatingCompanionService : Service() {

    private lateinit var windowManager: WindowManager
    private var avatarView: ImageView? = null
    private var params: WindowManager.LayoutParams? = null
    private var companionId: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_HIDE -> avatarView?.let { runCatching { windowManager.removeView(it) } }
            ACTION_SHOW -> {
                val id = intent.getStringExtra(EXTRA_COMPANION_ID) ?: companionId
                if (id != null) showCompanion(id)
            }
            ACTION_STOP -> stopSelf()
        }
        intent?.getStringExtra(EXTRA_COMPANION_ID)?.let {
            companionId = it
            if (avatarView == null) showCompanion(it)
        }
        return START_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showCompanion(id: String) {
        val app = application as PetMorphApp
        val entity = kotlinx.coroutines.runBlocking {
            app.container.repository.get(id)
        } ?: return

        // Remove previous
        avatarView?.let { runCatching { windowManager.removeView(it) } }

        val size = (96 * app.container.settings.overlaySizeFlow.let {
            kotlinx.coroutines.runBlocking { it }
        }).toInt().coerceIn(48, 320)

        val lp = WindowManager.LayoutParams(
            size, size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40; y = 200
            alpha = runBlocking { app.container.settings.overlayOpacityFlow }
        }
        params = lp

        val view = ImageView(this).apply {
            setImageBitmap(BitmapFactory.decodeFile(entity.processedImagePath))
            val pad = size / 6
            setPadding(pad / 2, 0, pad / 2, 0)
            setOnTouchListener(DragTapListener(lp, app, entity.soundEnabled))
        }
        avatarView = view
        runCatching { windowManager.addView(view, lp) }

        // Idle bobbing
        view.post(object : Runnable {
            var t = 0f
            override fun run() {
                t += 0.05f
                kotlin.math.sin(t.toDouble()).toFloat().let { s ->
                    view.translationY = s * 6f
                    view.scaleY = 1f + s * 0.03f
                }
                if (avatarView === view) view.postDelayed(this, 50)
            }
        })
    }

    private inner class DragTapListener(
        private val lp: WindowManager.LayoutParams,
        private val app: PetMorphApp,
        private val soundsOn: Boolean,
    ) : View.OnTouchListener {
        private var downX = 0f; private var downY = 0f
        private var startX = 0; private var startY = 0
        private var dragging = false
        private var lastTap = 0L

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    startX = lp.x; startY = lp.y
                    dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (abs(dx) > 12 || abs(dy) > 12) dragging = true
                    if (dragging) {
                        lp.x = (startX + dx).toInt()
                        lp.y = (startY + dy).toInt()
                        runCatching { windowManager.updateViewLayout(v, lp) }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!dragging) {
                        val now = System.currentTimeMillis()
                        if (now - lastTap < 350) {
                            // double tap -> open app
                            startActivity(
                                Intent(this@FloatingCompanionService, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } else {
                            // single tap -> cute reaction + sound
                            v.animate().scaleX(0.82f).scaleY(0.82f).setDuration(80)
                                .withEndAction {
                                    v.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
                                }.start()
                            if (soundsOn) {
                                app.container.sounds.enabled = true
                                app.container.sounds.play(com.petmorph.ai.audio.SoundEffect.HAPPY)
                            }
                        }
                        lastTap = now
                    }
                }
            }
            return true
        }
    }

    private fun startForegroundWithNotification() {
        val channelId = "floating_companion"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Floating companion", NotificationManager.IMPORTANCE_LOW)
        )
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, FloatingCompanionService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_pet)
            .setContentTitle("PetMorph companion is floating")
            .setContentText("Tap to open the app")
            .setContentIntent(openApp)
            .addAction(0, "Close companion", stop)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        avatarView?.let { runCatching { windowManager.removeView(it) } }
        avatarView = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_SHOW = "com.petmorph.ai.action.SHOW"
        const val ACTION_HIDE = "com.petmorph.ai.action.HIDE"
        const val ACTION_STOP = "com.petmorph.ai.action.STOP"
        const val EXTRA_COMPANION_ID = "companion_id"

        fun start(context: android.content.Context, companionId: String) {
            val i = Intent(context, FloatingCompanionService::class.java)
                .setAction(ACTION_SHOW)
                .putExtra(EXTRA_COMPANION_ID, companionId)
            context.startForegroundService(i)
        }

        fun stop(context: android.content.Context) {
            context.startService(Intent(context, FloatingCompanionService::class.java).setAction(ACTION_STOP))
        }
    }
}
