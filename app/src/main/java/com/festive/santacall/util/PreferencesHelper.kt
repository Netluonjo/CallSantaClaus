package com.festive.santacall.util

import android.content.Context
import android.content.SharedPreferences
import com.festive.santacall.model.ChatMessage
import com.festive.santacall.model.ChildProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("santa_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getChildProfile(): ChildProfile {
        val json = prefs.getString("child_profile", null) ?: return ChildProfile()
        return try {
            gson.fromJson(json, ChildProfile::class.java)
        } catch (e: Exception) {
            ChildProfile()
        }
    }

    fun saveChildProfile(profile: ChildProfile) {
        prefs.edit().putString("child_profile", gson.toJson(profile)).apply()
    }

    fun getCallTheme(): String {
        return prefs.getString("call_theme", "north_pole") ?: "north_pole"
    }

    fun setCallTheme(theme: String) {
        prefs.edit().putString("call_theme", theme).apply()
    }

    fun getChatMessages(): List<ChatMessage> {
        val json = prefs.getString("chat_history", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson<List<ChatMessage>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveChatMessages(messages: List<ChatMessage>) {
        val json = gson.toJson(messages)
        prefs.edit().putString("chat_history", json).apply()
    }

    fun clearChatHistory() {
        prefs.edit().remove("chat_history").apply()
    }
}

