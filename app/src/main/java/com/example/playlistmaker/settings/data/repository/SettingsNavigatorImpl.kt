package com.example.playlistmaker.settings.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator

class SettingsNavigatorImpl : SettingsNavigator {
    override fun shareApp(context: Context, message: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (shareIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_title)))
            } else {
                showToast(context, context.getString(R.string.no_app_to_share))
            }
        } catch (e: Exception) {
            showToast(context, "${context.getString(R.string.share_error)} ${e.localizedMessage}")
        }
    }

    override fun contactSupport(context: Context, email: String, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // только email приложения должны обрабатывать это
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Проверяем, есть ли приложения, которые могут обработать этот intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Если нет email приложений, предлагаем альтернативные варианты
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(fallbackIntent,
                        context.getString(R.string.choose_email_app)))
                } else {
                    showToast(context, context.getString(R.string.no_email_app_installed))
                }
            }
        } catch (e: Exception) {
            showToast(context, "${context.getString(R.string.email_send_error)}: ${e.localizedMessage}")
        }
    }

    override fun openUserAgreement(context: Context, url: String) {
        try {
            val uri = Uri.parse(url)
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                customTabsIntent.launchUrl(context, uri)
            } catch (e: Exception) {
                // Fallback to browser
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (browserIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(browserIntent)
                } else {
                    showToast(context, context.getString(R.string.no_browser_error))
                }
            }
        } catch (e: Exception) {
            showToast(context, context.getString(R.string.browser_error))
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}