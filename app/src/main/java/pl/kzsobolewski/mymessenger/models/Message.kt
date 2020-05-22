package pl.kzsobolewski.mymessenger.models

data class Message(
        val id: String = "",
        val text: String = "",
        val fromId: String = "",
        val toId: String = "",
        val timestamp: Long = -1
)