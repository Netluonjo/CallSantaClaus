package com.festive.santacall.model

enum class CallType {
    VIDEO,
    VOICE
}

data class ChildProfile(
    val name: String = "Bé Yêu",
    val behaviorStatus: String = "Bé rất ngoan và chăm học",
    val wishlist: String = "Một món đồ chơi bất ngờ",
    val age: Int = 5
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val isFromSanta: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
