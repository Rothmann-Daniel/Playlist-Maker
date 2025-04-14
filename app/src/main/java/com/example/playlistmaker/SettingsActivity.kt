package com.example.playlistmaker

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings) // Установка разметки

        val themeSwitch = findViewById<SwitchMaterial>(R.id.switch_theme)
        val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Установка текущего состояния
        themeSwitch.isChecked = isDarkThemeEnabled()

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("dark_theme", isChecked).apply()
            applyTheme(isChecked)
        }

        //Обработчик нажатия на кнопку навигаци: Назад
        val navBack = findViewById<MaterialToolbar>(R.id.tool_bar)
        navBack.setNavigationOnClickListener {
            finish()
        }

        // Обработчик кнопки "Поделиться"
        val shareButton = findViewById<MaterialTextView>(R.id.share)
        shareButton.setOnClickListener {
            shareApp()
        }

        // Обработчик кнопки "Написать в поддержку"
        val supportButton = findViewById<MaterialTextView>(R.id.support)
        supportButton.setOnClickListener {
            sendSupportEmail()
        }

        // Обработчик кнопки "Пользовательское соглашение"
        val userAgreementButton = findViewById<MaterialTextView>(R.id.user_agreement)
        userAgreementButton.setOnClickListener {
            openUserAgreement()
        }

    }

    // Пересоздаем активити для применения темы

    private fun applyTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                // Если режим темы установлен в "системный" проверяем системные настройки
                resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    // <!-- Кнопка "Поделиться" -->
    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT
            }

            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getString(R.string.share_title)
                )
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "${getString(R.string.share_error)} ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // <!-- Кнопка "Написать в поддержку / Support" -->

    private fun sendSupportEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822" // для любых приложений, работающих с email
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_body))
        }

        when {
            packageManager.queryIntentActivities(emailIntent, 0).isEmpty() -> {
                Toast.makeText(this, R.string.no_email_app, Toast.LENGTH_LONG).show()
            }
            else -> try {
                startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_app)))
            } catch (e: Exception) {
                Toast.makeText(this, R.string.email_send_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    // <!-- Кнопка "Пользовательское соглашение / User agreement" -->

    private fun openUserAgreement() {
        val context = this
        val url = getString(R.string.user_agreement_url)

        try {
            // Пытаемся открыть через Custom Tabs
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (e: Exception) {
            // Fallback: если Custom Tabs не сработал, используем обычный Intent
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                showNoBrowserError()
            }
        }
    }

    private fun showNoBrowserError() {
        Toast.makeText(this, R.string.no_browser_error, Toast.LENGTH_LONG).show()
    }

}



