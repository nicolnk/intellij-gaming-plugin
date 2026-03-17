package com.codequest.model

data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val type: String,          // Correspond à MissionType.name
    val target: Int,
    var progress: Int = 0,
    var completed: Boolean = false,
    val xpReward: Long = 100L,
    val emoji: String = "🎯"
) {
    val progressPercent: Float get() = if (target == 0) 1f else (progress.toFloat() / target).coerceAtMost(1f)
}
