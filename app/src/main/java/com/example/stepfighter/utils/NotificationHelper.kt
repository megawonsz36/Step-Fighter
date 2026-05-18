package com.example.stepfighter.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.stepfighter.R
import com.example.stepfighter.ui.login.LoginActivity

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "stepfighter_alerts_channel"
        private const val CHANNEL_NAME = "Alerty StepFighter"
        private const val CHANNEL_DESC = "Powiadomienia o energii, krokach i awansach"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    fun notifyEnergyFull() {
        val currentLang = context.getString(R.string.lang_pl)
        val title = if (currentLang == "Polski") "Energia naładowana!" else "Energy full!"
        val msg = if (currentLang == "Polski") "Twoja energia odnowiła się w 100%. Ruszaj do walki!" else "Your energy is at 100%. Time to battle!"
        sendNotification(title, msg, 101)
    }

    fun notifyStepsMilestone() {
        val currentLang = context.getString(R.string.lang_pl)
        val title = if (currentLang == "Polski") "Przeszedłeś już 500 kroków!" else "You walked 500 steps!"
        val msg = if (currentLang == "Polski") "Twoja siła rośnie! Może pora wejść do dungeona?" else "Your strength grows! Time to enter the dungeon?"
        sendNotification(title, msg, 102)
    }

    fun notifyLevelUp(level: Int) {
        val currentLang = context.getString(R.string.lang_pl)
        val title = if (currentLang == "Polski") "Nowy poziom!" else "Level up!"
        val msg = if (currentLang == "Polski") "Gratulacje! Awansowałeś na poziom $level." else "Congratulations! You reached level $level."
        sendNotification(title, msg, 103)
    }

    fun scheduleInactivityReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, InactivityReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 201, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        val triggerTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}

class InactivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val currentLang = it.getString(R.string.lang_pl)
            val title = if (currentLang == "Polski") "Twoja przygoda czeka!" else "Your adventure awaits!"
            val msg = if (currentLang == "Polski") "Wojowniku, lochy same się nie oczyszczą." else "Warrior, the dungeons won't clear themselves."

            val builder = NotificationCompat.Builder(it, "stepfighter_alerts_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (it.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            NotificationManagerCompat.from(it).notify(104, builder.build())
        }
    }
}