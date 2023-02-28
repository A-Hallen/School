package com.hallen.school

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.hallen.school.model.Global
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = Firebase.auth
        val database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
        if (!Global.isPersistentSet){
            database.setPersistenceEnabled(true)
            database.setPersistenceCacheSizeBytes(100000000)
            Global.isPersistentSet = true
        }
        Logger.addLogAdapter(AndroidLogAdapter())
        if (auth.currentUser != null){
            startActivity(Intent(this, ResultActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}