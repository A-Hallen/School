package com.hallen.school

import android.content.Context

class Prefs(context: Context){
    private val database = "School_db"

    private val storage = context.getSharedPreferences(database, 0)!!

    fun saveUserName(name: String){
        storage.edit().putString("userName", name).apply()
    }
    fun getUserName() = storage.getString("userName", "")!!

}