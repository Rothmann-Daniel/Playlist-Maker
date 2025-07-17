package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.settings.domain.usecase.UpdateThemeSettingsUseCase
import com.example.playlistmaker.util.App


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            GetThemeSettingsUseCase(SettingsRepositoryImpl((application as App).sharedPrefs)),
            UpdateThemeSettingsUseCase(SettingsRepositoryImpl((application as App).sharedPrefs))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.toolBar.setNavigationOnClickListener { finish() }

        // Настройка переключателя темы
        binding.switchTheme.isChecked = viewModel.getThemeSettings()
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateThemeSettings(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }


        binding.share.setOnClickListener { shareApp() }
        binding.support.setOnClickListener { sendSupportEmail() }
        binding.userAgreement.setOnClickListener { openUserAgreement() }
    }

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
            showToast("${getString(R.string.share_error)} ${e.localizedMessage}")
        }
    }

    private fun sendSupportEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_body))
        }

        when {
            packageManager.queryIntentActivities(emailIntent, 0).isEmpty() -> {
                showToast(getString(R.string.no_email_app))
            }
            else -> try {
                startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_app)))
            } catch (e: Exception) {
                showToast(getString(R.string.email_send_error))
            }
        }
    }

    // Открытие пользовательского соглашения
    private fun openUserAgreement() {
        val url = getString(R.string.user_agreement_url)

        try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(this, Uri.parse(url))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                showToast(getString(R.string.no_browser_error))
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}



