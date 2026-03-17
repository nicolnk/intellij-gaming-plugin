package com.codequest.listeners

import com.codequest.services.XpService
import com.intellij.openapi.project.ProjectManager
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager

class BuildResultListener : ProjectTaskListener {

    override fun finished(result: ProjectTaskManager.Result) {
        val project = ProjectManager.getInstance().openProjects.firstOrNull() ?: return
        if (result.hasErrors() || result.isAborted) {
            XpService.getInstance().onFailedBuild()
        } else {
            XpService.getInstance().onSuccessfulBuild(project)
        }
    }
}
