package com.codequest.listeners

import com.codequest.services.XpService
import com.intellij.openapi.project.Project
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TerminalOutputListener(private val project: Project) {

    private val logFile = File("/tmp/codequest.log")
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var lastSize = 0L
    private var lastCommand = ""

    fun start() {
        if (!logFile.exists()) logFile.createNewFile()
        lastSize = logFile.length()

        executor.scheduleAtFixedRate({
            try {
                checkLog()
            } catch (_: Exception) {}
        }, 1000, 500, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        executor.shutdownNow()
    }

    private fun checkLog() {
        val currentSize = logFile.length()
        if (currentSize <= lastSize) return

        val newContent = logFile.readText()
        val lines = newContent.lines().filter { it.isNotBlank() }
        lastSize = currentSize

        // Vider le fichier pour éviter qu'il grossisse
        logFile.writeText("")
        lastSize = 0

        for (line in lines) {
            when {
                line.startsWith("EXEC:") -> {
                    lastCommand = line.removePrefix("EXEC:").trim().lowercase()
                }
                line.startsWith("EXIT:") -> {
                    val exitCode = line.removePrefix("EXIT:").trim().toIntOrNull() ?: 1
                    processCommand(lastCommand, exitCode)
                }
            }
        }
    }

    private fun processCommand(cmd: String, exitCode: Int) {
        if (cmd.isBlank()) return
        val success = exitCode == 0
        val xp = XpService.getInstance()

        when {
            // Commits & Push
            cmd.startsWith("git commit") && success -> xp.onCommit(project)
            cmd.startsWith("git push") && success   -> xp.onCommit(project)

            // Flutter / Dart — run interactif : succès si exit 0 ou Ctrl+C (exit 130, app lancée puis arrêtée)
            (cmd.startsWith("flutter run") || cmd.startsWith("dart run")) &&
                    (exitCode == 0 || exitCode == 130) -> xp.onSuccessfulBuild(project)
            // Flutter / Dart — build et compile : succès uniquement si exit 0
            (cmd.startsWith("flutter build") || cmd.startsWith("dart compile")) && success -> xp.onSuccessfulBuild(project)
            // Flutter — build échoué (Ctrl+C exclu car ce n'est pas une erreur de build)
            (cmd.startsWith("flutter run") || cmd.startsWith("flutter build")) &&
                    !success && exitCode != 130 -> xp.onFailedBuild()

            // Flutter test
            cmd.startsWith("flutter test") && success -> xp.onTestsPassed(1, project)
            cmd.startsWith("dart test") && success    -> xp.onTestsPassed(1, project)

            // Gradle
            (cmd.startsWith("./gradlew build") ||
                    cmd.startsWith("gradle build") ||
                    cmd.startsWith("./gradlew assembledebug") ||
                    cmd.startsWith("./gradlew assemblerelease")) && success -> xp.onSuccessfulBuild(project)
            (cmd.startsWith("./gradlew build") ||
                    cmd.startsWith("gradle build")) && !success -> xp.onFailedBuild()
            (cmd.startsWith("./gradlew test") ||
                    cmd.startsWith("gradle test")) && success -> xp.onTestsPassed(1, project)

            // Laravel
            cmd.startsWith("php artisan serve") && success -> xp.onSuccessfulBuild(project)
            cmd.startsWith("php artisan test") && success  -> xp.onTestsPassed(1, project)
            cmd.startsWith("composer install") && success  -> xp.onSuccessfulBuild(project)
            cmd.startsWith("composer update") && success   -> xp.onSuccessfulBuild(project)
            cmd.startsWith("php artisan migrate") && success -> xp.onCommit(project)

            // Symfony
            (cmd.startsWith("symfony serve") ||
                    cmd.startsWith("bin/console") ||
                    cmd.startsWith("php bin/console")) && success -> xp.onSuccessfulBuild(project)
            cmd.contains("phpunit") && success             -> xp.onTestsPassed(1, project)
            cmd.contains("pest") && success                -> xp.onTestsPassed(1, project)

            // Node / JS
            (cmd.startsWith("npm run build") ||
                    cmd.startsWith("npm install") ||
                    cmd.startsWith("yarn build") ||
                    cmd.startsWith("yarn install") ||
                    cmd.startsWith("pnpm build") ||
                    cmd.startsWith("pnpm install") ||
                    cmd.startsWith("vite build") ||
                    cmd.startsWith("next build") ||
                    cmd.startsWith("nuxt build")) && success -> xp.onSuccessfulBuild(project)
            (cmd.startsWith("npm test") ||
                    cmd.startsWith("yarn test") ||
                    cmd.startsWith("pnpm test") ||
                    cmd.startsWith("jest") ||
                    cmd.startsWith("vitest") ||
                    cmd.startsWith("mocha")) && success -> xp.onTestsPassed(1, project)

            // Python
            (cmd.startsWith("python manage.py runserver") ||
                    cmd.startsWith("uvicorn") ||
                    cmd.startsWith("gunicorn") ||
                    cmd.startsWith("flask run") ||
                    cmd.startsWith("pip install")) && success -> xp.onSuccessfulBuild(project)
            (cmd.startsWith("pytest") ||
                    cmd.startsWith("python -m pytest") ||
                    cmd.startsWith("python manage.py test")) && success -> xp.onTestsPassed(1, project)

            // Rust
            cmd.startsWith("cargo build") && success -> xp.onSuccessfulBuild(project)
            cmd.startsWith("cargo build") && !success -> xp.onFailedBuild()
            cmd.startsWith("cargo run") && success   -> xp.onSuccessfulBuild(project)
            cmd.startsWith("cargo test") && success  -> xp.onTestsPassed(1, project)

            // Go
            cmd.startsWith("go build") && success -> xp.onSuccessfulBuild(project)
            cmd.startsWith("go run") && success   -> xp.onSuccessfulBuild(project)
            cmd.startsWith("go test") && success  -> xp.onTestsPassed(1, project)

            // .NET
            (cmd.startsWith("dotnet build") ||
                    cmd.startsWith("dotnet run") ||
                    cmd.startsWith("dotnet publish")) && success -> xp.onSuccessfulBuild(project)
            cmd.startsWith("dotnet test") && success -> xp.onTestsPassed(1, project)

            // Maven
            (cmd.startsWith("mvn package") ||
                    cmd.startsWith("mvn install") ||
                    cmd.startsWith("mvn compile")) && success -> xp.onSuccessfulBuild(project)
            (cmd.startsWith("mvn test") ||
                    cmd.startsWith("mvn verify")) && success  -> xp.onTestsPassed(1, project)

            // Docker
            (cmd.startsWith("docker build") ||
                    cmd.startsWith("docker-compose up") ||
                    cmd.startsWith("docker compose up")) && success -> xp.onSuccessfulBuild(project)

            // Déploiements
            (cmd.startsWith("vercel") ||
                    cmd.startsWith("netlify deploy") ||
                    cmd.startsWith("firebase deploy") ||
                    cmd.startsWith("fly deploy") ||
                    cmd.startsWith("heroku push") ||
                    cmd.startsWith("docker push") ||
                    cmd.startsWith("kubectl apply")) && success -> xp.onCommit(project)
        }
    }
}