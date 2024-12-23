package com.example.uts_map

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

fun ScheduleReminder(context: Context, reminderId: String, title: String, description: String, date: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Schedule alarm for the specific reminder time
    val reminderIntent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("description", description)
    }
    val reminderPendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        reminderIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, date, reminderPendingIntent)

    // Schedule alarm for the start of the day
    val calendar = Calendar.getInstance().apply {
        timeInMillis = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val startOfDayIntent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", "Start of the Day Reminder")
        putExtra("description", "You have a reminder today: $title")
    }
    val startOfDayPendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId.hashCode() + 1, // Ensure a unique request code
        startOfDayIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, startOfDayPendingIntent)
}