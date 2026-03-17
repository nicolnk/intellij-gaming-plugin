package com.codequest.services

import com.codequest.model.Achievements
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import java.time.LocalTime

@Service(Service.Level.APP)
class AchievementService {

    companion object {
        fun getInstance(): AchievementService =
            ApplicationManager.getApplication().getService(AchievementService::class.java)
    }

    private val listeners = mutableListOf<(String) -> Unit>()
    fun addUnlockListener(l: (String) -> Unit) = listeners.add(l)
    fun removeUnlockListener(l: (String) -> Unit) = listeners.remove(l)

    private fun tryUnlock(id: String) {
        val player = PlayerStateService.getInstance()
        if (!player.isAchievementUnlocked(id)) {
            val achievement = Achievements.findById(id) ?: return
            player.unlockAchievement(id)
            player.addXp(achievement.xpReward)
            listeners.forEach { it(id) }
        }
    }

    fun checkCodeAchievements() {
        val state = PlayerStateService.getInstance().state
        val lines = state.totalCharsTyped / 40
        if (lines >= 1) tryUnlock("first_line")
        if (lines >= 100) tryUnlock("lines_100")
        if (lines >= 1000) tryUnlock("lines_1000")
        if (lines >= 10000) tryUnlock("lines_10000")
        checkTimeAchievements()
    }

    fun checkBuildAchievements() {
        val state = PlayerStateService.getInstance().state
        val builds = state.totalSuccessfulBuilds
        val combo = state.consecutiveSuccessBuilds
        if (builds >= 1) tryUnlock("first_build")
        if (builds >= 10) tryUnlock("builds_10")
        if (builds >= 50) tryUnlock("builds_50")
        if (combo >= 3) tryUnlock("combo_3")
        if (combo >= 10) tryUnlock("combo_10")
    }

    fun checkTestAchievements() {
        val state = PlayerStateService.getInstance().state
        val tests = state.totalTestsPassed
        if (tests >= 1) tryUnlock("first_test")
        if (tests >= 10) tryUnlock("tests_10")
        if (tests >= 100) tryUnlock("tests_100")
    }

    fun checkCommitAchievements() {
        val state = PlayerStateService.getInstance().state
        val commits = state.totalCommits
        if (commits >= 1) tryUnlock("first_commit")
        if (commits >= 10) tryUnlock("commits_10")
        if (commits >= 50) tryUnlock("commits_50")
        if (commits >= 100) tryUnlock("commits_100")
    }

    fun checkStreakAchievements() {
        val state = PlayerStateService.getInstance().state
        val streak = state.currentStreak
        if (streak >= 3) tryUnlock("streak_3")
        if (streak >= 7) tryUnlock("streak_7")
        if (streak >= 30) tryUnlock("streak_30")
    }

    fun checkLevelAchievements() {
        val state = PlayerStateService.getInstance().state
        val level = state.level
        if (level >= 5) tryUnlock("level_5")
        if (level >= 10) tryUnlock("level_10")
        if (level >= 20) tryUnlock("level_20")
        if (level >= 50) tryUnlock("level_50")
    }

    private fun checkTimeAchievements() {
        val now = LocalTime.now()
        if (now.hour >= 0 && now.hour < 5) tryUnlock("night_owl")
        if (now.hour < 7) tryUnlock("early_bird")
        val dayOfWeek = java.time.LocalDate.now().dayOfWeek
        if (dayOfWeek == java.time.DayOfWeek.MONDAY && now.hour in 6..10) tryUnlock("monday_warrior")
    }
}
