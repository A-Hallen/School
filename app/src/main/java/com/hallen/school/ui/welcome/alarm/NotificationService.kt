package com.hallen.school.ui.welcome.alarm

import android.annotation.TargetApi
import android.app.*
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.hallen.school.R
import com.hallen.school.ResultActivity
import java.util.*

class NotificationService : IntentService {
    private var notificationManager: NotificationManager? = null
    private var pendingIntent: PendingIntent? = null
    private var notification: Notification? = null

    constructor(name: String?) : super(name)
    constructor() : super("SERVICE")

    @TargetApi(Build.VERSION_CODES.O)
    override fun onHandleIntent(intent2: Intent?) {
        if (intent2 == null) return

        val time    = intent2.getLongExtra("date", 0L)
        val title   = intent2.getStringExtra("title")
        val details = intent2.getStringExtra("details")

        Toast.makeText(applicationContext, "Date: ${Date(time)}\n Title: $title \n Details: $details", Toast.LENGTH_SHORT).show()

        val NOTIFICATION_CHANNEL_ID = applicationContext.getString(R.string.app_name)
        val context = this.applicationContext
        notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val mIntent = Intent(this, ResultActivity::class.java)
        val res = this.resources
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFY_ID = 0 // ID of notification
            val notifManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel = notifManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (mChannel == null) {
                mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_ID,
                    importance)
                mChannel.enableVibration(true)
                mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                notifManager.createNotificationChannel(mChannel)
            }
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setContentTitle(title)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.ic_notification) // required
                .setContentText(details)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_notification))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            val notification = builder.build()
            notifManager.notify(NOTIFY_ID, notification)
            startForeground(1, notification)
        } else {
            pendingIntent =
                PendingIntent.getActivity(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            notification = NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_notification))
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentText(details).build()
            notificationManager!!.notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}