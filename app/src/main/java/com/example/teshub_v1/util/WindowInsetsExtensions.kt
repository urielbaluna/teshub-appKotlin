package com.example.teshub_v1.util

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Configura la actividad para que use edge-to-edge display respetando los insets del sistema.
 * Esto previene que el contenido se sobreponga con la barra de estado y botones de navegación.
 */
fun Activity.setupEdgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

/**
 * Aplica padding a una vista basado en los insets del sistema (barra de estado, navegación, etc.)
 * @param applyTop Si debe aplicar padding superior (barra de estado)
 * @param applyBottom Si debe aplicar padding inferior (barra de navegación)
 * @param applyLeft Si debe aplicar padding izquierdo
 * @param applyRight Si debe aplicar padding derecho
 */
fun View.applySystemBarInsets(
    applyTop: Boolean = true,
    applyBottom: Boolean = true,
    applyLeft: Boolean = true,
    applyRight: Boolean = true
) {
    val initialPadding = Rect(paddingLeft, paddingTop, paddingRight, paddingBottom)
    
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        
        view.setPadding(
            if (applyLeft) initialPadding.left + insets.left else initialPadding.left,
            if (applyTop) initialPadding.top + insets.top else initialPadding.top,
            if (applyRight) initialPadding.right + insets.right else initialPadding.right,
            if (applyBottom) initialPadding.bottom + insets.bottom else initialPadding.bottom
        )
        
        windowInsets
    }
}

private data class Rect(val left: Int, val top: Int, val right: Int, val bottom: Int)
