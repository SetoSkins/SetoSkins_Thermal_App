package com.setoskins.thermal.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.setoskins.thermal.MainActivity
import com.setoskins.thermal.R
import com.setoskins.thermal.data.BatteryMonitor

class SuperIslandService : Service() {

    private val CHANNEL_ID = "super_island_channel"
    private val NOTIFICATION_ID = 1001
    private val UPDATE_INTERVAL_MS = 2000L
    private var appIconCircular: Icon? = null
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (updateNotification()) {
                updateHandler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                updateNotification()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        appIconCircular = createCircularIconFromResource(R.mipmap.ic_launcher)
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        startForeground(NOTIFICATION_ID, buildNotification(BatteryMonitor.getBatteryState(this)))
        startRealtimeUpdates()
    }

    private fun createCircularIconFromResource(resId: Int): Icon? {
        return try {
            val bitmap = BitmapFactory.decodeResource(resources, resId) ?: return null
            val size = Math.min(bitmap.width, bitmap.height)
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint().apply { isAntiAlias = true }
            val rect = Rect(0, 0, size, size)
            
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            
            val srcRect = if (bitmap.width > bitmap.height) {
                Rect((bitmap.width - size) / 2, 0, (bitmap.width + size) / 2, size)
            } else {
                Rect(0, (bitmap.height - size) / 2, size, (bitmap.height + size) / 2)
            }
            
            canvas.drawBitmap(bitmap, srcRect, rect, paint)
            bitmap.recycle()
            Icon.createWithBitmap(output)
        } catch (e: Exception) {
            null
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateNotification()
        startRealtimeUpdates()
        return START_STICKY
    }

    private fun startRealtimeUpdates() {
        updateHandler.removeCallbacks(updateRunnable)
        updateHandler.post(updateRunnable)
    }

    private fun updateNotification(): Boolean {
        val state = BatteryMonitor.getBatteryState(this)
        if (!state.isCharging) {
            stopSelf()
            return false
        }
        val notification = buildNotification(state)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
        return true
    }

    private fun buildNotification(state: BatteryMonitor.BatteryState): Notification {
        val contentTitle = "超级岛充电监测"
        val contentText = "功率: %.1fW | 温度: %.1f°C | 电量: %d%%".format(state.wattage, state.temperature, state.level)
        
        // For Android 15 Status Bar Chip: Only wattage as requested
        val shortText = "%.1fW".format(state.wattage)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE)

            // Use the circular app icon
            if (appIconCircular != null) {
                builder.setSmallIcon(appIconCircular)
            } else {
                builder.setSmallIcon(R.mipmap.ic_launcher)
            }

            val notification = builder.build()

            // Android 15 Live Update APIs
            if (Build.VERSION.SDK_INT >= 35) {
                notification.extras.putBoolean("android.requestPromotedOngoing", true)
                notification.extras.putCharSequence("android.shortCriticalText", shortText)
            }
            return notification
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "超级岛充电信息",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "在状态栏或通知栏显示实时充电信息"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        updateHandler.removeCallbacks(updateRunnable)
        unregisterReceiver(batteryReceiver)
        super.onDestroy()
    }
}
