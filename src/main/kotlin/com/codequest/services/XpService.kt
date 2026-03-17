package com.codequest.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.APP)
class XpService {

    companion object {
        fun getInstance(): XpService =
            ApplicationManager.getApplication().getService(XpService::class.java)

        const val CHARS_PER_XP_TICK = 50
        const val XP_PER_TICK = 5L
        const val XP_SUCCESSFUL_BUILD = 20L
        const val XP_TESTS_PASSED = 30L
        const val XP_COMMIT = 50L
        const val XP_STREAK_BONUS = 25L
        const val BUILD_COMBO_THRESHOLD = 3
        const val BUILD_COMBO_MULTIPLIER = 2.0
    }

    private val listeners = mutableListOf<() -> Unit>()
    fun addListener(listener: () -> Unit) { listeners.add(listener) }
    fun removeListener(listener: () -> Unit) { listeners.remove(listener) }
    private fun notifyListeners() = listeners.forEach { it() }

    fun onCharsTyped(chars: Int, project: Project?) {
        val state = PlayerStateService.getInstance().state
        val previousChars = state.totalCharsTyped
        state.totalCharsTyped += chars

        val previousTicks = previousChars / CHARS_PER_XP_TICK
        val newTicks = state.totalCharsTyped / CHARS_PER_XP_TICK
        val completedTicks = newTicks - previousTicks

        if (completedTicks > 0) {
            val xp = completedTicks * XP_PER_TICK
            grantXp(xp, "📝 +${xp} XP (code écrit)", project)
        }

        MissionService.getInstance().onProgress(MissionService.MissionType.TYPE_CHARS, chars, project)
        AchievementService.getInstance().checkCodeAchievements()
        notifyListeners()
    }

    fun onSuccessfulBuild(project: Project?) {
        val state = PlayerStateService.getInstance().state
        state.totalSuccessfulBuilds++
        state.consecutiveSuccessBuilds++
        var xp = XP_SUCCESSFUL_BUILD
        var label = "🔨 +$xp XP (compilation réussie)"
        if (state.consecutiveSuccessBuilds >= BUILD_COMBO_THRESHOLD) {
            xp = (xp * BUILD_COMBO_MULTIPLIER).toLong()
            label = "🔥 COMBO x${state.consecutiveSuccessBuilds} ! +$xp XP"
        }
        grantXp(xp, label, project)
        MissionService.getInstance().onProgress(MissionService.MissionType.SUCCESSFUL_BUILD, 1, project)
        AchievementService.getInstance().checkBuildAchievements()
        notifyListeners()
    }

    fun onFailedBuild() {
        PlayerStateService.getInstance().state.consecutiveSuccessBuilds = 0
        notifyListeners()
    }

    fun onTestsPassed(count: Int, project: Project?) {
        val state = PlayerStateService.getInstance().state
        state.totalTestsPassed += count
        val xp = XP_TESTS_PASSED * count
        grantXp(xp, "✅ +$xp XP ($count test(s) passé(s))", project)
        MissionService.getInstance().onProgress(MissionService.MissionType.PASS_TESTS, count, project)
        AchievementService.getInstance().checkTestAchievements()
        notifyListeners()
    }

    fun onCommit(project: Project?) {
        val state = PlayerStateService.getInstance().state
        state.totalCommits++
        grantXp(XP_COMMIT, "💾 +$XP_COMMIT XP (commit Git)", project)
        MissionService.getInstance().onProgress(MissionService.MissionType.COMMIT, 1, project)
        AchievementService.getInstance().checkCommitAchievements()
        notifyListeners()
    }

    fun applyStreakBonus(streak: Int, project: Project?) {
        if (streak > 1) {
            val xp = XP_STREAK_BONUS * streak
            grantXp(xp, "🔥 Streak $streak jours ! +$xp XP bonus", project)
            notifyListeners()
        }
    }

    private fun grantXp(amount: Long, message: String, project: Project?) {
        val playerState = PlayerStateService.getInstance()
        val oldLevel = playerState.state.level
        playerState.addXp(amount)
        val newLevel = playerState.state.level
        if (project != null) {
            showNotification(message, project)
            if (newLevel > oldLevel) {
                showNotification(
                    "🎉 LEVEL UP ! Niveau $newLevel — Rang : ${playerState.state.rank}",
                    project,
                    NotificationType.INFORMATION
                )
            }
        }
        AchievementService.getInstance().checkLevelAchievements()
    }

    private fun showNotification(message: String, project: Project, type: NotificationType = NotificationType.INFORMATION) {
        try {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("CodeQuest Notifications")
                ?.createNotification(message, type)
                ?.notify(project)
        } catch (_: Exception) {}
    }
}
