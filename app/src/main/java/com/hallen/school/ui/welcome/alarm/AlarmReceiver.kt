package com.hallen.school.ui.welcome.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val service1 = Intent(context, NotificationService::class.java)

        val date        = intent.getLongExtra("date", 0L)
        val titulo      = intent.getStringExtra("title")
        val descripcion = intent.getStringExtra("details")

        service1.putExtra("date", date)
        service1.putExtra("title", titulo)
        service1.putExtra("details", descripcion)
        Toast.makeText(context, "2 - Title: $titulo, Details: $descripcion", Toast.LENGTH_SHORT).show()

        ContextCompat.startForegroundService(context, service1)

        Log.d("HALLEN", " ALARM RECEIVED!!!")
    }
}

