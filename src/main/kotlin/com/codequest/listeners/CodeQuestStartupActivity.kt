package com.codequest.listeners

import com.codequest.services.AchievementService
import com.codequest.services.MissionService
import com.codequest.services.PlayerStateService
import com.codequest.services.XpService
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class CodeQuestStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // Listener de frappe dans l'éditeur
        val multicaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addDocumentListener(GlobalDocumentListener(project), project)

        // Listener de sortie terminal
        val terminalListener = TerminalOutputListener(project)
        terminalListener.start()

        // Missions journalières
        MissionService.getInstance().refreshDailyMissions()

        // Bonus streak
        val state = PlayerStateService.getInstance().state
        AchievementService.getInstance().checkStreakAchievements()
        if (state.currentStreak > 1) {
            XpService.getInstance().applyStreakBonus(state.currentStreak, project)
        }
        AchievementService.getInstance().checkLevelAchievements()
    }
}

class GlobalDocumentListener(private val project: Project) : DocumentListener {

    override fun documentChanged(event: DocumentEvent) {
        val added = event.newFragment.toString()
        if (added.length > 200) return
        val meaningfulChars = added.count { !it.isWhitespace() }
        if (meaningfulChars > 0) {
            XpService.getInstance().onCharsTyped(meaningfulChars, project)
        }
    }
}
