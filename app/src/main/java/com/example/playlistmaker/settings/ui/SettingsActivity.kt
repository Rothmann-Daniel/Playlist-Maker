package com.example.playlistmaker.settings.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.InteractorCreator
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator
import com.google.android.material.snackbar.Snackbar


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            InteractorCreator.getThemeSettingsUseCase,
            InteractorCreator.updateThemeSettingsUseCase,
            InteractorCreator.navigateUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupThemeSwitch()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolBar.setNavigationOnClickListener { finish() }
    }

    private fun setupThemeSwitch() {
        viewModel.themeState.observe(this) { isDarkTheme ->
            binding.switchTheme.isChecked = isDarkTheme
        }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateThemeSettings(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setupClickListeners() {
        binding.share.setOnClickListener {
            viewModel.onShareAppClicked(
                context = this,
                message = getString(R.string.share_message),
                shareTitle = getString(R.string.share_title),
                noAppMessage = getString(R.string.no_app_to_share),
                errorMessage = getString(R.string.share_error)
            )
        }

        binding.support.setOnClickListener {
            viewModel.onSupportClicked(
                context = this,
                email = getString(R.string.support_email),
                subject = getString(R.string.support_email_subject),
                body = getString(R.string.support_email_body),
                chooseEmailAppText = getString(R.string.choose_email_app),
                noEmailAppMessage = getString(R.string.no_email_app_installed),
                errorMessage = getString(R.string.email_send_error)
            )
        }

        binding.userAgreement.setOnClickListener {
            viewModel.onUserAgreementClicked(
                context = this,
                url = getString(R.string.user_agreement_url),
                noBrowserMessage = getString(R.string.no_browser_error),
                errorMessage = getString(R.string.browser_error)
            )
        }
    }

    private fun observeViewModel() {
        viewModel.navigationResult.observe(this) { result ->
            when (result) {
                is SettingsNavigator.NavigationResult.Error -> {
                    showMessage(result.message)
                }
                is SettingsNavigator.NavigationResult.NoAppFound -> {
                    showMessage(result.message)
                }
                SettingsNavigator.NavigationResult.Success -> {
                    // Успешная навигация не требует действий
                }
            }
        }

        viewModel.toastMessage.observe(this) { message ->
            showMessage(message)
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}