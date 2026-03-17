# 🎮 CodeQuest — Plugin IntelliJ de Gamification

Gagnez de l'XP en codant, montez en niveau, débloquez des succès et accomplissez des missions journalières.

---

## ✨ Fonctionnalités

### XP & Niveaux
- **+5 XP** toutes les 10 lignes de code écrites
- **+20 XP** par compilation réussie (×2 si combo ×3)
- **+30 XP** par test passé
- **+50 XP** par commit Git
- **+10 XP** par fichier créé
- **+25 XP** × streak en bonus journalier

### Rangs
| XP Total | Rang |
|---|---|
| 0 | Stagiaire |
| 500 | Junior |
| 2 000 | Mid |
| 6 000 | Senior |
| 15 000 | Lead |
| 35 000 | Architecte |
| 80 000 | Légende |

### Missions Journalières (3 par jour, aléatoires)
- Écrire N lignes de code
- Compiler N fois avec succès
- Faire passer N tests
- Faire N commits

### Succès (23 au total)
Répartis en 4 rarités : Commun, Rare, Épique, Légendaire.
Catégories : Code, Builds, Tests, Commits, Streaks, Niveaux, Easter Eggs.

---

## 🚀 Installation & Développement

### Prérequis
- **IntelliJ IDEA** (Community ou Ultimate) — version 2024.3+
- **JDK 17+**
- **Kotlin** (inclus avec IntelliJ)

### Ouvrir le projet
1. Cloner / extraire ce dossier
2. Ouvrir dans IntelliJ IDEA : `File > Open > codequest-plugin/`
3. Laisser Gradle synchroniser les dépendances (peut prendre quelques minutes)

### Lancer en mode sandbox
```bash
./gradlew runIde
```
Cela ouvre une instance sandbox d'IntelliJ avec le plugin installé.

### Construire le fichier .jar installable
```bash
./gradlew buildPlugin
```
Le fichier `.zip` sera dans `build/distributions/`.

### Installer manuellement
`Settings > Plugins > ⚙️ > Install Plugin from Disk...` → sélectionner le `.zip`

---

## 📁 Structure du projet

```
src/main/kotlin/com/codequest/
├── listeners/
│   ├── CodeDocumentListener.kt   # Détecte l'écriture de code
│   ├── BuildResultListener.kt    # Détecte les compilations
│   ├── VcsCommitListener.kt      # Détecte les commits Git
│   └── CodeQuestStartupActivity.kt
├── services/
│   ├── PlayerStateService.kt     # État persistant (XP, niveau, stats)
│   ├── XpService.kt              # Calcul et attribution de l'XP
│   ├── MissionService.kt         # Missions journalières
│   └── AchievementService.kt     # Gestion des succès
├── model/
│   ├── DailyMission.kt
│   └── Achievement.kt            # Tous les succès définis ici
└── ui/
    └── CodeQuestToolWindow.kt    # Interface du panneau latéral
```

---

## 🔧 Personnalisation

Pour ajouter des missions : éditez `MissionService.MISSION_POOL`
Pour ajouter des succès : éditez `Achievements.ALL` dans `Achievement.kt`
Pour modifier les gains XP : éditez les constantes dans `XpService`
