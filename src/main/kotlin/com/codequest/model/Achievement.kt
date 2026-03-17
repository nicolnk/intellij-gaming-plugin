package com.codequest.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val xpReward: Long,
    val rarity: Rarity = Rarity.COMMON
) {
    enum class Rarity(val label: String, val color: String) {
        COMMON("Commun", "#aaaaaa"),
        RARE("Rare", "#4a9eff"),
        EPIC("Épique", "#9b59b6"),
        LEGENDARY("Légendaire", "#f39c12")
    }
}

object Achievements {
    val ALL = listOf(
        // --- Code ---
        Achievement("first_line", "Hello World", "Écrire votre première ligne de code", "✍️", 50L),
        Achievement("lines_100", "Centurion", "Écrire 100 lignes de code", "📝", 100L),
        Achievement("lines_1000", "Mille Mots", "Écrire 1 000 lignes de code", "📖", 300L, Achievement.Rarity.RARE),
        Achievement("lines_10000", "Graphomane", "Écrire 10 000 lignes de code", "📚", 1000L, Achievement.Rarity.EPIC),

        // --- Builds ---
        Achievement("first_build", "Premier Lancement", "Compiler pour la première fois", "🔨", 50L),
        Achievement("builds_10", "Constructeur", "Compiler 10 fois avec succès", "🏗️", 150L),
        Achievement("builds_50", "Architecte du Code", "Compiler 50 fois avec succès", "🏛️", 500L, Achievement.Rarity.RARE),
        Achievement("combo_3", "Triple Combo", "Compiler 3 fois de suite sans erreur", "🔥", 200L, Achievement.Rarity.RARE),
        Achievement("combo_10", "Perfectionniste", "Compiler 10 fois de suite sans erreur", "💎", 750L, Achievement.Rarity.EPIC),

        // --- Tests ---
        Achievement("first_test", "TDD Padawan", "Faire passer votre premier test", "✅", 75L),
        Achievement("tests_10", "QA Junior", "Faire passer 10 tests", "🧪", 150L),
        Achievement("tests_100", "QA Senior", "Faire passer 100 tests", "🔬", 600L, Achievement.Rarity.RARE),

        // --- Commits ---
        Achievement("first_commit", "Première Trace", "Effectuer votre premier commit", "💾", 100L),
        Achievement("commits_10", "Versionneur", "Effectuer 10 commits", "📦", 200L),
        Achievement("commits_50", "Git Master", "Effectuer 50 commits", "🌿", 700L, Achievement.Rarity.RARE),
        Achievement("commits_100", "Open Sourcier", "Effectuer 100 commits", "🌍", 2000L, Achievement.Rarity.EPIC),

        // --- Streaks ---
        Achievement("streak_3", "En Feu", "Coder 3 jours de suite", "🔥", 150L),
        Achievement("streak_7", "Semaine de Feu", "Coder 7 jours de suite", "⚡", 400L, Achievement.Rarity.RARE),
        Achievement("streak_30", "Moine Codeur", "Coder 30 jours de suite", "🧘", 2000L, Achievement.Rarity.LEGENDARY),

        // --- Niveaux ---
        Achievement("level_5", "Survivant", "Atteindre le niveau 5", "⭐", 200L),
        Achievement("level_10", "Confirmé", "Atteindre le niveau 10", "🌟", 500L, Achievement.Rarity.RARE),
        Achievement("level_20", "Expert", "Atteindre le niveau 20", "💫", 1500L, Achievement.Rarity.EPIC),
        Achievement("level_50", "Légende Vivante", "Atteindre le niveau 50", "🏆", 5000L, Achievement.Rarity.LEGENDARY),

        // --- Easter Eggs ---
        Achievement("night_owl", "Chouette Nocturne", "Coder après minuit", "🦉", 100L),
        Achievement("early_bird", "Lève-Tôt", "Coder avant 7h du matin", "🐦", 100L),
        Achievement("monday_warrior", "Guerrier du Lundi", "Commencer à coder un lundi matin", "⚔️", 75L),
    )

    fun findById(id: String) = ALL.find { it.id == id }
}
