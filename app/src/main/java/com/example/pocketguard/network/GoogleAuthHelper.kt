package com.example.pocketguard.network

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

class GoogleAuthHelper(private val activity: Activity) {

    companion object {
        private const val AUTH_URL = "http://10.0.2.2:3001/api/v1/auth/google"
        const val DEEP_LINK_SCHEME = "pocketguard"
        const val DEEP_LINK_HOST = "auth"
        const val DEEP_LINK_PATH = "/callback"
    }

    fun startGoogleSignIn() {
        val builder = CustomTabsIntent.Builder().apply {
            setShowTitle(true)
            setUrlBarHidingEnabled(true)
            setStartAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
            setExitAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
        }

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(activity, Uri.parse(AUTH_URL))
    }

    fun handleDeepLink(intent: Intent): String? {
        val uri = intent.data ?: return null

        if (uri.scheme != DEEP_LINK_SCHEME || uri.host != DEEP_LINK_HOST) {
            return null
        }

        return when (uri.path) {
            DEEP_LINK_PATH -> uri.getQueryParameter("token")
            else -> null
        }
    }
}

