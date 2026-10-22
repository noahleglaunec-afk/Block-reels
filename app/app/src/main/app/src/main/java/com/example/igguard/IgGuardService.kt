package com.example.igguard

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class IgGuardService : AccessibilityService() {
    private val ig = "com.instagram.android"
    private val blockedTabs = listOf("$ig:id/feed_tab", "$ig:id/clips_tab")
    private val dmTargets = listOf("$ig:id/direct_tab", "$ig:id/action_bar_inbox_button")
    private var lastClick = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName != ig) return
        val now = System.currentTimeMillis()
        if (now - lastClick < 800) return
        val root = rootInActiveWindow ?: return

        val onBlocked = blockedTabs.any { id ->
            root.findAccessibilityNodeInfosByViewId(id).any { it.isSelected }
        }
        if (!onBlocked) return

        for (id in dmTargets) {
            val node = root.findAccessibilityNodeInfosByViewId(id).firstOrNull() ?: continue
            var n: AccessibilityNodeInfo? = node
            while (n != null && !n.isClickable) n = n.parent
            if (n?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
                lastClick = now
                return
            }
        }
    }

    override fun onInterrupt() {}
}
