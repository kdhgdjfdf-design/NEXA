package com.example.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class FocusGuardAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var activeService: FocusGuardAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeService = this
        FocusGuardManager.instance?.onAccessibilityConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val type = event?.eventType ?: return
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            type != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }
        val pkg = event.packageName?.toString()
            ?: rootInActiveWindow?.packageName?.toString()
            ?: return
        if (pkg.isBlank() || pkg == "android" || pkg.startsWith("com.android.systemui")) return
        FocusGuardManager.instance?.onForegroundPackageDetected(pkg)
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        activeService = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        activeService = null
        super.onDestroy()
    }
}
