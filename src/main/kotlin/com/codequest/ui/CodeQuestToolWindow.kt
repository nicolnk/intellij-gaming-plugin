package com.codequest.ui

import com.codequest.model.Achievement
import com.codequest.model.Achievements
import com.codequest.model.DailyMission
import com.codequest.services.AchievementService
import com.codequest.services.MissionService
import com.codequest.services.PlayerStateService
import com.codequest.services.XpService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

class CodeQuestToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = CodeQuestPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class CodeQuestPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val BG_DARK = Color(18, 18, 28)
    private val BG_CARD = Color(28, 28, 42)
    private val BG_CARD2 = Color(35, 35, 55)
    private val ACCENT = Color(99, 102, 241)
    private val ACCENT2 = Color(236, 72, 153)
    private val XP_COLOR = Color(251, 191, 36)
    private val SUCCESS = Color(34, 197, 94)
    private val TEXT_PRIMARY = Color(248, 250, 252)
    private val TEXT_SECONDARY = Color(148, 163, 184)
    private val TEXT_MUTED = Color(71, 85, 105)

    private lateinit var levelLabel: JLabel
    private lateinit var rankLabel: JLabel
    private lateinit var xpLabel: JLabel
    private lateinit var xpBar: JProgressBar
    private lateinit var totalXpLabel: JLabel
    private lateinit var statsPanel: JPanel
    private lateinit var missionsPanel: JPanel
    private lateinit var achievementsPanel: JPanel
    private lateinit var streakLabel: JLabel

    init {
        background = BG_DARK
        buildUI()
        registerListeners()
        refresh()
    }

    private fun buildUI() {
        val scrollContent = JPanel()
        scrollContent.layout = BoxLayout(scrollContent, BoxLayout.Y_AXIS)
        scrollContent.background = BG_DARK
        scrollContent.border = EmptyBorder(12, 12, 12, 12)

        scrollContent.add(buildHeroSection())
        scrollContent.add(Box.createVerticalStrut(12))
        scrollContent.add(buildSectionTitle("📊 Statistiques"))
        scrollContent.add(Box.createVerticalStrut(6))
        statsPanel = JPanel(GridLayout(2, 2, 8, 8))
        statsPanel.background = BG_DARK
        statsPanel.alignmentX = LEFT_ALIGNMENT
        scrollContent.add(statsPanel)
        scrollContent.add(Box.createVerticalStrut(12))
        scrollContent.add(buildSectionTitle("🎯 Missions du Jour"))
        scrollContent.add(Box.createVerticalStrut(6))
        missionsPanel = JPanel()
        missionsPanel.layout = BoxLayout(missionsPanel, BoxLayout.Y_AXIS)
        missionsPanel.background = BG_DARK
        missionsPanel.alignmentX = LEFT_ALIGNMENT
        scrollContent.add(missionsPanel)
        scrollContent.add(Box.createVerticalStrut(12))
        scrollContent.add(buildSectionTitle("🏆 Succès"))
        scrollContent.add(Box.createVerticalStrut(6))
        achievementsPanel = JPanel()
        achievementsPanel.layout = BoxLayout(achievementsPanel, BoxLayout.Y_AXIS)
        achievementsPanel.background = BG_DARK
        achievementsPanel.alignmentX = LEFT_ALIGNMENT
        scrollContent.add(achievementsPanel)
        scrollContent.add(Box.createVerticalStrut(20))

        val scroll = JBScrollPane(scrollContent)
        scroll.border = null
        scroll.background = BG_DARK
        scroll.viewport.background = BG_DARK
        scroll.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scroll.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        add(scroll, BorderLayout.CENTER)
    }

    private fun buildHeroSection(): JPanel {
        val hero = JPanel()
        hero.layout = BoxLayout(hero, BoxLayout.Y_AXIS)
        hero.background = BG_CARD
        hero.border = compoundBorder(LineBorder(ACCENT.darker(), 1, true), EmptyBorder(16, 16, 16, 16))
        hero.alignmentX = LEFT_ALIGNMENT

        val topRow = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
        topRow.background = BG_CARD
        topRow.alignmentX = LEFT_ALIGNMENT

        levelLabel = JLabel("Niv. 1")
        levelLabel.font = Font("Monospaced", Font.BOLD, 22)
        levelLabel.foreground = XP_COLOR
        topRow.add(levelLabel)

        rankLabel = JLabel("Stagiaire")
        rankLabel.font = Font("SansSerif", Font.BOLD, 14)
        rankLabel.foreground = ACCENT
        rankLabel.border = compoundBorder(LineBorder(ACCENT, 1, true), EmptyBorder(2, 8, 2, 8))
        topRow.add(rankLabel)

        streakLabel = JLabel("🔥 0j")
        streakLabel.font = Font("SansSerif", Font.BOLD, 12)
        streakLabel.foreground = Color(251, 146, 60)
        topRow.add(Box.createHorizontalStrut(8))
        topRow.add(streakLabel)
        hero.add(topRow)
        hero.add(Box.createVerticalStrut(10))

        xpLabel = JLabel("0 / 100 XP")
        xpLabel.font = Font("Monospaced", Font.PLAIN, 11)
        xpLabel.foreground = TEXT_SECONDARY
        xpLabel.alignmentX = LEFT_ALIGNMENT
        xpLabel.border = EmptyBorder(0, 2, 4, 0)
        hero.add(xpLabel)

        xpBar = JProgressBar(0, 100)
        xpBar.value = 0
        xpBar.isStringPainted = false
        xpBar.preferredSize = Dimension(Int.MAX_VALUE, 10)
        xpBar.maximumSize = Dimension(Int.MAX_VALUE, 10)
        xpBar.background = BG_DARK
        xpBar.foreground = ACCENT
        xpBar.alignmentX = LEFT_ALIGNMENT
        xpBar.border = LineBorder(TEXT_MUTED, 1, true)
        hero.add(xpBar)
        hero.add(Box.createVerticalStrut(8))

        totalXpLabel = JLabel("Total : 0 XP")
        totalXpLabel.font = Font("Monospaced", Font.PLAIN, 10)
        totalXpLabel.foreground = TEXT_MUTED
        totalXpLabel.alignmentX = LEFT_ALIGNMENT
        hero.add(totalXpLabel)

        return hero
    }

    private fun buildSectionTitle(title: String): JLabel {
        val lbl = JLabel(title)
        lbl.font = Font("SansSerif", Font.BOLD, 12)
        lbl.foreground = TEXT_SECONDARY
        lbl.alignmentX = LEFT_ALIGNMENT
        lbl.border = EmptyBorder(4, 0, 2, 0)
        return lbl
    }

    private fun buildStatCard(label: String, value: String, color: Color): JPanel {
        val card = JPanel(BorderLayout(0, 4))
        card.background = BG_CARD2
        card.border = compoundBorder(LineBorder(color.darker(), 1, true), EmptyBorder(10, 12, 10, 12))
        val valLbl = JLabel(value)
        valLbl.font = Font("Monospaced", Font.BOLD, 18)
        valLbl.foreground = color
        valLbl.horizontalAlignment = SwingConstants.CENTER
        val nameLbl = JLabel(label)
        nameLbl.font = Font("SansSerif", Font.PLAIN, 10)
        nameLbl.foreground = TEXT_SECONDARY
        nameLbl.horizontalAlignment = SwingConstants.CENTER
        card.add(valLbl, BorderLayout.CENTER)
        card.add(nameLbl, BorderLayout.SOUTH)
        return card
    }

    private fun buildMissionCard(mission: DailyMission): JPanel {
        val card = JPanel(BorderLayout(8, 4))
        val borderColor = if (mission.completed) SUCCESS else BG_CARD2
        card.background = BG_CARD
        card.border = compoundBorder(LineBorder(borderColor, 1, true), EmptyBorder(10, 12, 10, 12))
        card.maximumSize = Dimension(Int.MAX_VALUE, 80)
        card.alignmentX = LEFT_ALIGNMENT

        val left = JPanel()
        left.layout = BoxLayout(left, BoxLayout.Y_AXIS)
        left.background = BG_CARD

        val titleRow = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0))
        titleRow.background = BG_CARD
        val emojiLbl = JLabel(mission.emoji)
        emojiLbl.font = Font("SansSerif", Font.PLAIN, 14)
        val titleLbl = JLabel(mission.title)
        titleLbl.font = Font("SansSerif", Font.BOLD, 12)
        titleLbl.foreground = if (mission.completed) SUCCESS else TEXT_PRIMARY
        titleRow.add(emojiLbl)
        titleRow.add(titleLbl)
        left.add(titleRow)

        val descLbl = JLabel(mission.description)
        descLbl.font = Font("SansSerif", Font.PLAIN, 10)
        descLbl.foreground = TEXT_SECONDARY
        descLbl.border = EmptyBorder(2, 4, 4, 0)
        left.add(descLbl)

        val bar = JProgressBar(0, mission.target)
        bar.value = mission.progress
        bar.isStringPainted = false
        bar.preferredSize = Dimension(Int.MAX_VALUE, 6)
        bar.maximumSize = Dimension(Int.MAX_VALUE, 6)
        bar.background = BG_DARK
        bar.foreground = if (mission.completed) SUCCESS else ACCENT2
        bar.border = null
        left.add(bar)
        card.add(left, BorderLayout.CENTER)

        val right = JPanel(BorderLayout())
        right.background = BG_CARD
        val progressLbl = JLabel("${mission.progress}/${mission.target}")
        progressLbl.font = Font("Monospaced", Font.BOLD, 11)
        progressLbl.foreground = if (mission.completed) SUCCESS else XP_COLOR
        progressLbl.horizontalAlignment = SwingConstants.RIGHT
        val rewardLbl = JLabel("+${mission.xpReward}xp")
        rewardLbl.font = Font("Monospaced", Font.PLAIN, 10)
        rewardLbl.foreground = XP_COLOR
        rewardLbl.horizontalAlignment = SwingConstants.RIGHT
        right.add(progressLbl, BorderLayout.CENTER)
        right.add(rewardLbl, BorderLayout.SOUTH)
        card.add(right, BorderLayout.EAST)

        return card
    }

    private fun buildAchievementCard(achievement: Achievement, unlocked: Boolean): JPanel {
        val card = JPanel(BorderLayout(8, 0))
        val bg = if (unlocked) BG_CARD else BG_DARK
        card.background = bg
        val rarityColor = try { Color.decode(achievement.rarity.color) } catch (_: Exception) { TEXT_MUTED }
        card.border = compoundBorder(
            LineBorder(if (unlocked) rarityColor else TEXT_MUTED, 1, true),
            EmptyBorder(8, 10, 8, 10)
        )
        card.maximumSize = Dimension(Int.MAX_VALUE, 60)
        card.alignmentX = LEFT_ALIGNMENT

        val emojiLbl = JLabel(if (unlocked) achievement.emoji else "🔒")
        emojiLbl.font = Font("SansSerif", Font.PLAIN, 18)
        card.add(emojiLbl, BorderLayout.WEST)

        val center = JPanel()
        center.layout = BoxLayout(center, BoxLayout.Y_AXIS)
        center.background = bg
        val titleLbl = JLabel(achievement.title)
        titleLbl.font = Font("SansSerif", Font.BOLD, 11)
        titleLbl.foreground = if (unlocked) TEXT_PRIMARY else TEXT_MUTED
        val descLbl = JLabel(achievement.description)
        descLbl.font = Font("SansSerif", Font.PLAIN, 10)
        descLbl.foreground = TEXT_MUTED
        center.add(titleLbl)
        center.add(descLbl)
        card.add(center, BorderLayout.CENTER)

        if (unlocked) {
            val right = JPanel()
            right.layout = BoxLayout(right, BoxLayout.Y_AXIS)
            right.background = bg
            val rarityLbl = JLabel(achievement.rarity.label)
            rarityLbl.font = Font("SansSerif", Font.BOLD, 9)
            rarityLbl.foreground = rarityColor
            rarityLbl.horizontalAlignment = SwingConstants.RIGHT
            val xpLbl = JLabel("+${achievement.xpReward}xp")
            xpLbl.font = Font("Monospaced", Font.PLAIN, 10)
            xpLbl.foreground = XP_COLOR
            xpLbl.horizontalAlignment = SwingConstants.RIGHT
            right.add(rarityLbl)
            right.add(xpLbl)
            card.add(right, BorderLayout.EAST)
        }
        return card
    }

    private fun registerListeners() {
        XpService.getInstance().addListener { SwingUtilities.invokeLater { refresh() } }
        AchievementService.getInstance().addUnlockListener { SwingUtilities.invokeLater { refresh() } }
    }

    fun refresh() {
        val state = PlayerStateService.getInstance().state
        val player = PlayerStateService.getInstance()

        levelLabel.text = "Niv. ${state.level}"
        rankLabel.text = state.rank
        streakLabel.text = "🔥 ${state.currentStreak}j"

        val xpIn = player.xpInCurrentLevel()
        val xpNext = player.xpForNextLevel()
        xpLabel.text = "$xpIn / $xpNext XP"
        xpBar.maximum = xpNext.toInt().coerceAtLeast(1)
        xpBar.value = xpIn.toInt()
        totalXpLabel.text = "Total : ${state.totalXp} XP"

        statsPanel.removeAll()
        val chars = state.totalCharsTyped
        val linesEq = chars / 40
        statsPanel.add(buildStatCard("📝 ~Lignes", "$linesEq", ACCENT))
        statsPanel.add(buildStatCard("🔨 Builds", "${state.totalSuccessfulBuilds}", Color(34, 197, 94)))
        statsPanel.add(buildStatCard("✅ Tests", "${state.totalTestsPassed}", Color(251, 146, 60)))
        statsPanel.add(buildStatCard("💾 Commits", "${state.totalCommits}", ACCENT2))

        missionsPanel.removeAll()
        MissionService.getInstance().dailyMissions.forEach { mission ->
            missionsPanel.add(buildMissionCard(mission))
            missionsPanel.add(Box.createVerticalStrut(6))
        }

        achievementsPanel.removeAll()
        val unlocked = Achievements.ALL.filter { player.isAchievementUnlocked(it.id) }
        val locked = Achievements.ALL.filter { !player.isAchievementUnlocked(it.id) }
        unlocked.forEach { a ->
            achievementsPanel.add(buildAchievementCard(a, true))
            achievementsPanel.add(Box.createVerticalStrut(4))
        }
        val lockedTitle = JLabel("— ${locked.size} succès verrouillés —")
        lockedTitle.font = Font("SansSerif", Font.ITALIC, 10)
        lockedTitle.foreground = TEXT_MUTED
        lockedTitle.alignmentX = LEFT_ALIGNMENT
        achievementsPanel.add(lockedTitle)
        achievementsPanel.add(Box.createVerticalStrut(4))
        locked.take(5).forEach { a ->
            achievementsPanel.add(buildAchievementCard(a, false))
            achievementsPanel.add(Box.createVerticalStrut(4))
        }

        revalidate()
        repaint()
    }

    private fun compoundBorder(outside: javax.swing.border.Border, inside: javax.swing.border.Border) =
        javax.swing.BorderFactory.createCompoundBorder(outside, inside)
}
