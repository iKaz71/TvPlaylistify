package com.kaz.tvplaylistify.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

object PersistentHostManager {
    private const val PREF_NAME = "persistent_hosts"
    private const val KEY_HOSTS = "hosts"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun obtenerAnfitriones(context: Context, onLoaded: (List<String>) -> Unit) {
        val json = getPrefs(context).getString(KEY_HOSTS, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = List(array.length()) { i -> array.getString(i) }
        onLoaded(list)
    }

    fun guardarAnfitriones(context: Context, hosts: List<String>, onSaved: (() -> Unit)? = null) {
        val json = JSONArray(hosts).toString()
        getPrefs(context)
            .edit()
            .putString(KEY_HOSTS, json)
            .apply()
        onSaved?.invoke()
    }
}
