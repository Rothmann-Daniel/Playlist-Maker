package com.example.playlistmaker.settings.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator

class SettingsNavigatorImpl : SettingsNavigator {

    override fun shareApp(
        context: Context,
        message: String,
        shareTitle: String,
        noAppMessage: String,
        errorMessage: String
    ): SettingsNavigator.NavigationResult {
        return try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (shareIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(shareIntent, shareTitle))
                SettingsNavigator.NavigationResult.Success
            } else {
                SettingsNavigator.NavigationResult.NoAppFound(noAppMessage)
            }
        } catch (e: Exception) {
            SettingsNavigator.NavigationResult.Error("$errorMessage ${e.localizedMessage}")
        }
    }

    override fun contactSupport(
        context: Context,
        email: String,
        subject: String,
        body: String,
        chooseEmailAppText: String,
        noEmailAppMessage: String,
        errorMessage: String
    ): SettingsNavigator.NavigationResult {
        return try {
            val mailtoIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (mailtoIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mailtoIntent)
                SettingsNavigator.NavigationResult.Success
            } else {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(fallbackIntent, chooseEmailAppText))
                    SettingsNavigator.NavigationResult.Success
                } else {
                    SettingsNavigator.NavigationResult.NoAppFound(noEmailAppMessage)
                }
            }
        } catch (e: Exception) {
            SettingsNavigator.NavigationResult.Error("$errorMessage: ${e.localizedMessage}")
        }
    }

    override fun openUserAgreement(
        context: Context,
        url: String,
        noBrowserMessage: String,
        errorMessage: String
    ): SettingsNavigator.NavigationResult {
        return try {
            val uri = Uri.parse(url)
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                customTabsIntent.launchUrl(context, uri)
                SettingsNavigator.NavigationResult.Success
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (browserIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(browserIntent)
                    SettingsNavigator.NavigationResult.Success
                } else {
                    SettingsNavigator.NavigationResult.NoAppFound(noBrowserMessage)
                }
            }
        } catch (e: Exception) {
            SettingsNavigator.NavigationResult.Error(errorMessage)
        }
    }
}