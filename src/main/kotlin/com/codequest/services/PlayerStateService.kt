package com.codequest.services

import com.codequest.model.Achievement
import com.codequest.model.DailyMission
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service(Service.Level.APP)
@State(name = "CodeQuestPlayerState", storages = [Storage("codequest_player.xml")])
class PlayerStateService : PersistentStateComponent<PlayerStateService.State> {

    data class State(
        var totalXp: Long = 0,
        var level: Int = 1,
        var rank: String = "Stagiaire",

        // Stats globales
        var totalLinesWritten: Int = 0,
        var totalCharsTyped: Int = 0,
        var totalSuccessfulBuilds: Int = 0,
        var totalTestsPassed: Int = 0,
        var totalCommits: Int = 0,
        var totalFilesCreated: Int = 0,
        var totalRefactors: Int = 0,

        // Streak
        var currentStreak: Int = 0,
        var longestStreak: Int = 0,
        var lastActiveDate: String = "",

        // Missions journalières (sérialisées en JSON simple)
        var dailyMissionsJson: String = "",
        var lastMissionResetDate: String = "",

        // Succès débloqués (IDs)
        var unlockedAchievementIds: MutableList<String> = mutableListOf(),

        // Combo builds
        var consecutiveSuccessBuilds: Int = 0
    )

    private var _state = State()

    override fun getState(): State = _state
    override fun loadState(state: State) { _state = state }

    companion object {
        fun getInstance(): PlayerStateService =
            ApplicationManager.getApplication().getService(PlayerStateService::class.java)

        val RANKS = listOf(
            0L to "Stagiaire",
            500L to "Junior",
            2_000L to "Mid",
            6_000L to "Senior",
            15_000L to "Lead",
            35_000L to "Architecte",
            80_000L to "Légende"
        )

        val LEVEL_XP_CURVE: (Int) -> Long = { level ->
            (100L * level * (1 + level * 0.1)).toLong()
        }
    }

    fun addXp(amount: Long) {
        _state.totalXp += amount
        updateLevel()
        updateRank()
        updateStreak()
    }

    fun updateLevel() {
        var xpRemaining = _state.totalXp
        var lvl = 1
        while (xpRemaining >= LEVEL_XP_CURVE(lvl)) {
            xpRemaining -= LEVEL_XP_CURVE(lvl)
            lvl++
        }
        _state.level = lvl
    }

    fun updateRank() {
        val newRank = RANKS.lastOrNull { _state.totalXp >= it.first }?.second ?: "Stagiaire"
        _state.rank = newRank
    }

    fun updateStreak() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val last = _state.lastActiveDate
        if (last.isEmpty()) {
            _state.currentStreak = 1
        } else {
            val lastDate = LocalDate.parse(last, DateTimeFormatter.ISO_DATE)
            val todayDate = LocalDate.now()
            when {
                lastDate == todayDate -> { /* Déjà compté aujourd'hui */ }
                lastDate.plusDays(1) == todayDate -> {
                    _state.currentStreak++
                    if (_state.currentStreak > _state.longestStreak)
                        _state.longestStreak = _state.currentStreak
                }
                else -> _state.currentStreak = 1
            }
        }
        _state.lastActiveDate = today
    }

    /** XP nécessaire pour le prochain niveau */
    fun xpForNextLevel(): Long = LEVEL_XP_CURVE(_state.level)

    /** XP dans le niveau actuel */
    fun xpInCurrentLevel(): Long {
        var xpRemaining = _state.totalXp
        var lvl = 1
        while (lvl < _state.level && xpRemaining >= LEVEL_XP_CURVE(lvl)) {
            xpRemaining -= LEVEL_XP_CURVE(lvl)
            lvl++
        }
        return xpRemaining
    }

    fun isAchievementUnlocked(id: String) = id in _state.unlockedAchievementIds
    fun unlockAchievement(id: String) {
        if (id !in _state.unlockedAchievementIds) _state.unlockedAchievementIds.add(id)
    }
}
