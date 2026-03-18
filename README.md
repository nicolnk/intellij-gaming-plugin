# 🎮 CodeQuest — Plugin IntelliJ de Gamification

Gagnez de l'XP en codant, montez en niveau, débloquez des succès et accomplissez des missions journalières.

---

## ✨ Fonctionnalités

### XP & Niveaux
- **+5 XP** tous les 50 caractères de code écrits
- **+20 XP** par compilation réussie (×2 si combo ×3)
- **+30 XP** par test passé
- **+50 XP** par commit / push Git
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
- Taper N caractères de code
- Compiler N fois avec succès
- Faire passer N tests
- Faire N commits

### Succès (23 au total)
Répartis en 4 rarités : Commun, Rare, Épique, Légendaire.
Catégories : Code, Builds, Tests, Commits, Streaks, Niveaux, Easter Eggs.

---

## 🚀 Installation

### Prérequis
- **IntelliJ IDEA** (Community ou Ultimate) — version 2025.1+
- **JDK 21+**
- **Kotlin** (inclus avec IntelliJ)

### Installer le plugin
1. Télécharger le `.zip` depuis les releases
2. `Settings > Plugins > ⚙️ > Install Plugin from Disk...` → sélectionner le `.zip`
3. Redémarrer IntelliJ

### Configuration requise (macOS / Linux)

Pour la détection des builds et commits depuis le terminal, ajouter ces hooks dans `~/.zshrc` (zsh) ou `~/.bashrc` (bash) :
```zsh
# CodeQuest plugin hook
preexec() {
    echo "EXEC:$1" >> /tmp/codequest.log
}
precmd() {
    echo "EXIT:$?" >> /tmp/codequest.log
}
```

Puis recharger la config : `source ~/.zshrc`

> ⚠️ **Windows** : la détection via terminal n'est pas encore supportée. Les builds lancés via le bouton ▶️ d'IntelliJ sont détectés nativement.

---

## 🔧 Développement

### Compiler le plugin
```bash
gradle wrapper
./gradlew buildPlugin
```
Le fichier `.zip` sera dans `build/distributions/`.

### Lancer en mode sandbox
```bash
./gradlew runIde
```

### Mise à jour
Recompiler avec `./gradlew buildPlugin` et réinstaller via `Install Plugin from Disk...`.

---

## 📁 Structure du projet
```
src/main/kotlin/com/codequest/
├── listeners/
│   ├── CodeQuestStartupActivity.kt   # Démarrage + listener de frappe
│   ├── TerminalOutputListener.kt     # Détecte builds/commits via terminal
│   ├── BuildResultListener.kt        # Détecte les builds UI IntelliJ
│   ├── RunConfigurationListener.kt   # Détecte les run configs (Flutter, etc.)
│   ├── VcsCommitListener.kt          # Détecte les commits via UI Git
│   └── CodeDocumentListener.kt
├── services/
│   ├── PlayerStateService.kt         # État persistant (XP, niveau, stats)
│   ├── XpService.kt                  # Calcul et attribution de l'XP
│   ├── MissionService.kt             # Missions journalières
│   └── AchievementService.kt         # Gestion des succès
├── model/
│   ├── DailyMission.kt
│   └── Achievement.kt                # Tous les succès définis ici
└── ui/
    ├── CodeQuestToolWindow.kt         # Panneau latéral détail
    └── CodeQuestStatusBarWidget.kt    # Widget barre de statut
```

---

## 🔧 Personnalisation

Pour ajouter des missions : éditez `MissionService.MISSION_POOL`
Pour ajouter des succès : éditez `Achievements.ALL` dans `Achievement.kt`
Pour modifier les gains XP : éditez les constantes dans `XpService`

---

## 🗺️ Roadmap

- [ ] Support Windows (hook PowerShell)
- [ ] Résolution de bug build flutter non détecté dans les stats