package com.example.mermaidmaker.data.local.prefs

import android.content.Context

class EditorPreferences(
    private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLastOpenedDiagramId(): String? {
        return prefs.getString(KEY_LAST_OPENED_ID, null)
    }

    fun setLastOpenedDiagramId(id: String) {
        prefs.edit().putString(KEY_LAST_OPENED_ID, id).apply()
    }

    fun clearLastOpenedDiagramId() {
        prefs.edit().remove(KEY_LAST_OPENED_ID).apply()
    }

    companion object {
        private const val PREFS_NAME = "editor_prefs"
        private const val KEY_LAST_OPENED_ID = "last_opened_diagram_id"
    }
}


