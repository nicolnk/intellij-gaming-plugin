package com.codequest.listeners

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

// Ce fichier est conservé pour la déclaration dans plugin.xml
// Le vrai listener est enregistré dans CodeQuestStartupActivity
class CodeDocumentListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {}
}
