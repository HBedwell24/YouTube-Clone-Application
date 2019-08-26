package com.example.youtubeapiintegration

import android.content.Context
import android.content.SharedPreferences

class SharedPref(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("com.example.youtubeapiintegration", Context.MODE_PRIVATE)

    fun setNightModeState(state: Boolean?) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("nightMode", state!!)
        editor.apply()
    }

    fun loadNightModeState(): Boolean? {
        return sharedPreferences.getBoolean("nightMode", false)
    }
}
