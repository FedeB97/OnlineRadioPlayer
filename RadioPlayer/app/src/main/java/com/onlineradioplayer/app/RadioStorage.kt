package com.onlineradioplayer.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RadioStorage {
    private const val PREFS = "radios_prefs"
    private const val KEY = "radios"

    fun saveRadios(ctx: Context, radios: List<Radio>) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = Gson().toJson(radios)
        prefs.edit().putString(KEY, json).apply()
    }

    fun loadRadios(ctx: Context): MutableList<Radio> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Radio>>() {}.type
        return Gson().fromJson(json, type)
    }
}
