package com.example.playlistmaker.settings.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.InteractorCreator
import com.example.playlistmaker.databinding.ActivitySettingsBinding


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
                message = getString(R.string.share_message),
                shareTitle = getString(R.string.share_title),
                noAppMessage = getString(R.string.no_app_to_share),
                errorMessage = getString(R.string.share_error)
            )
        }

        binding.support.setOnClickListener {
            viewModel.onSupportClicked(
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
                url = getString(R.string.user_agreement_url),
                noBrowserMessage = getString(R.string.no_browser_error),
                errorMessage = getString(R.string.browser_error)
            )
        }
    }

    private fun observeViewModel() {
        viewModel.navigationEvent.observe(this) { event ->
            when (event) {
                is SettingsViewModel.NavigationEvent.ShareApp -> {
                    InteractorCreator.navigateUseCase.shareApp(
                        context = this,
                        message = event.message,
                        shareTitle = event.shareTitle,
                        noAppMessage = event.noAppMessage,
                        errorMessage = event.errorMessage
                    )
                }
                is SettingsViewModel.NavigationEvent.ContactSupport -> {
                    InteractorCreator.navigateUseCase.contactSupport(
                        context = this,
                        email = event.email,
                        subject = event.subject,
                        body = event.body,
                        chooseEmailAppText = event.chooseEmailAppText,
                        noEmailAppMessage = event.noEmailAppMessage,
                        errorMessage = event.errorMessage
                    )
                }
                is SettingsViewModel.NavigationEvent.OpenUserAgreement -> {
                    InteractorCreator.navigateUseCase.openUserAgreement(
                        context = this,
                        url = event.url,
                        noBrowserMessage = event.noBrowserMessage,
                        errorMessage = event.errorMessage
                    )
                }
            }
        }
    }
}