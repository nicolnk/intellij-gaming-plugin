package com.codequest.services

import com.codequest.model.DailyMission
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service(Service.Level.APP)
class MissionService {

    enum class MissionType {
        TYPE_CHARS,       // Taper N caractères significatifs
        SUCCESSFUL_BUILD, // Compiler N fois avec succès
        PASS_TESTS,       // Faire passer N tests
        COMMIT            // Faire N commits
    }

    companion object {
        fun getInstance(): MissionService =
            ApplicationManager.getApplication().getService(MissionService::class.java)

        // Basé sur les caractères non-whitespace (≈ 40 chars = 1 ligne de code)
        private val MISSION_POOL = listOf(
            DailyMission("chars_200",  "Apprenti",       "Taper 200 caractères de code",   MissionType.TYPE_CHARS.name,       200,  xpReward = 75L,  emoji = "✍️"),
            DailyMission("chars_500",  "Plume Agile",    "Taper 500 caractères de code",   MissionType.TYPE_CHARS.name,       500,  xpReward = 100L, emoji = "📝"),
            DailyMission("chars_1000", "Machine à Code", "Taper 1 000 caractères de code", MissionType.TYPE_CHARS.name,       1000, xpReward = 150L, emoji = "⚡"),
            DailyMission("chars_2000", "Graphomane",     "Taper 2 000 caractères de code", MissionType.TYPE_CHARS.name,       2000, xpReward = 250L, emoji = "🔥"),
            DailyMission("build_2",    "Double Build",   "Compiler 2 fois avec succès",    MissionType.SUCCESSFUL_BUILD.name, 2,    xpReward = 80L,  emoji = "🔨"),
            DailyMission("build_5",    "Cinq sur Cinq",  "Compiler 5 fois avec succès",    MissionType.SUCCESSFUL_BUILD.name, 5,    xpReward = 150L, emoji = "🏗️"),
            DailyMission("build_10",   "Forgeron",       "Compiler 10 fois avec succès",   MissionType.SUCCESSFUL_BUILD.name, 10,   xpReward = 250L, emoji = "⚒️"),
            DailyMission("test_3",     "QA Débutant",    "Faire passer 3 tests",           MissionType.PASS_TESTS.name,       3,    xpReward = 80L,  emoji = "🧪"),
            DailyMission("test_10",    "Testeur du Jour","Faire passer 10 tests",          MissionType.PASS_TESTS.name,       10,   xpReward = 150L, emoji = "✅"),
            DailyMission("test_30",    "QA Acharné",     "Faire passer 30 tests",          MissionType.PASS_TESTS.name,       30,   xpReward = 300L, emoji = "🔬"),
            DailyMission("commit_1",   "Trace du Jour",  "Effectuer 1 commit",             MissionType.COMMIT.name,           1,    xpReward = 75L,  emoji = "💾"),
            DailyMission("commit_3",   "Triple Commit",  "Effectuer 3 commits",            MissionType.COMMIT.name,           3,    xpReward = 175L, emoji = "📦"),
        )
    }

    private var _dailyMissions: MutableList<DailyMission> = mutableListOf()
    val dailyMissions: List<DailyMission> get() = _dailyMissions

    // Compteurs de session (delta depuis le début de la journée)
    private var sessionCharsStart = 0

    init {
        loadOrResetMissions()
        sessionCharsStart = PlayerStateService.getInstance().state.totalCharsTyped
    }

    private fun loadOrResetMissions() {
        val state = PlayerStateService.getInstance().state
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        if (state.lastMissionResetDate != today) {
            resetMissions(today)
        } else {
            _dailyMissions = deserializeMissions(state.dailyMissionsJson).toMutableList()
            if (_dailyMissions.isEmpty()) resetMissions(today)
        }
    }

    private fun resetMissions(today: String) {
        val state = PlayerStateService.getInstance().state
        // 1 mission de chaque catégorie principale + 1 bonus
        val charMission = MISSION_POOL.filter { it.type == MissionType.TYPE_CHARS.name }.random()
        val buildMission = MISSION_POOL.filter { it.type == MissionType.SUCCESSFUL_BUILD.name }.random()
        val bonusMission = MISSION_POOL.filter {
            it.type == MissionType.PASS_TESTS.name || it.type == MissionType.COMMIT.name
        }.random()
        _dailyMissions = mutableListOf(
            charMission.copy(progress = 0, completed = false),
            buildMission.copy(progress = 0, completed = false),
            bonusMission.copy(progress = 0, completed = false)
        )
        state.lastMissionResetDate = today
        serializeMissions()
    }

    fun onProgress(type: MissionType, amount: Int, project: Project?) {
        var changed = false
        _dailyMissions.forEach { mission ->
            if (!mission.completed && mission.type == type.name) {
                mission.progress = (mission.progress + amount).coerceAtMost(mission.target)
                if (mission.progress >= mission.target) {
                    mission.completed = true
                    PlayerStateService.getInstance().addXp(mission.xpReward)
                }
                changed = true
            }
        }
        if (changed) serializeMissions()
    }

    fun refreshDailyMissions() = loadOrResetMissions()

    private fun serializeMissions() {
        PlayerStateService.getInstance().state.dailyMissionsJson =
            _dailyMissions.joinToString("|") { "${it.id},${it.progress},${it.completed}" }
    }

    private fun deserializeMissions(json: String): List<DailyMission> {
        if (json.isBlank()) return emptyList()
        return try {
            json.split("|").mapNotNull { entry ->
                val parts = entry.split(",")
                if (parts.size < 3) return@mapNotNull null
                val base = MISSION_POOL.find { it.id == parts[0] } ?: return@mapNotNull null
                base.copy(progress = parts[1].toInt(), completed = parts[2].toBoolean())
            }
        } catch (_: Exception) { emptyList() }
    }
}
