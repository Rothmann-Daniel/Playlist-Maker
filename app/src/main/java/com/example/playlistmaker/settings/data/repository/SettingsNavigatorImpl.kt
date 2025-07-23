package com.example.playlistmaker.settings.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.example.playlistmaker.settings.domain.repository.NavigationEvent
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator

class SettingsNavigatorImpl(private val context: Context) : SettingsNavigator {

    override fun navigate(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.ShareApp -> shareApp(event.message)
            is NavigationEvent.ContactSupport -> contactSupport(
                event.email,
                event.subject,
                event.body
            )
            is NavigationEvent.OpenUserAgreement -> openUserAgreement(event.url)
        }
    }

    private fun shareApp(message: String) {
        try {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.let { intent ->
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(intent, null))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun contactSupport(email: String, subject: String, body: String) {
        try {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.let { intent ->
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    handleFallbackEmail(email, subject, body)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleFallbackEmail(email: String, subject: String, body: String) {
        try {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }.let { intent ->
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(intent, null))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openUserAgreement(url: String) {
        try {
            Uri.parse(url).let { uri ->
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .apply {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        launchUrl(context, uri)
                    }
            }
        } catch (e: Exception) {
            try {
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let { intent ->
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}