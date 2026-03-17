package com.codequest.ui

import com.codequest.services.PlayerStateService
import com.codequest.services.XpService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class CodeQuestStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId() = "CodeQuestWidget"
    override fun getDisplayName() = "CodeQuest"
    override fun isAvailable(project: Project) = true
    override fun createWidget(project: Project) = CodeQuestStatusBarWidget(project)
    override fun disposeWidget(widget: StatusBarWidget) = widget.dispose()
    override fun canBeEnabledOn(statusBar: StatusBar) = true
}

class CodeQuestStatusBarWidget(private val project: Project) : StatusBarWidget,
    StatusBarWidget.TextPresentation {

    private var statusBar: StatusBar? = null

    init {
        XpService.getInstance().addListener { updateWidget() }
    }

    override fun ID() = "CodeQuestWidget"

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {}

    override fun getText(): String {
        val state = PlayerStateService.getInstance().state
        val player = PlayerStateService.getInstance()
        val xpIn = player.xpInCurrentLevel()
        val xpNext = player.xpForNextLevel()
        val pct = if (xpNext > 0) (xpIn * 100 / xpNext) else 0
        return "⚡ Niv.${state.level} · ${state.rank} · $pct% XP · 🔥${state.currentStreak}j"
    }

    override fun getTooltipText(): String {
        val state = PlayerStateService.getInstance().state
        val player = PlayerStateService.getInstance()
        val xpIn = player.xpInCurrentLevel()
        val xpNext = player.xpForNextLevel()
        return """
            CodeQuest — ${state.rank}
            Niveau ${state.level} · $xpIn/$xpNext XP
            Total : ${state.totalXp} XP
            Streak : ${state.currentStreak} jour(s)
        """.trimIndent()
    }

    override fun getAlignment(): Float = 0.5f

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        // Ouvre le Tool Window CodeQuest au clic
        val toolWindow = com.intellij.openapi.wm.ToolWindowManager
            .getInstance(project)
            .getToolWindow("CodeQuest")
        toolWindow?.show()
    }

    private fun updateWidget() {
        statusBar?.updateWidget(ID())
    }
}
