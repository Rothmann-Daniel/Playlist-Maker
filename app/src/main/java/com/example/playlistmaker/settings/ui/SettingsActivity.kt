package com.example.playlistmaker.settings.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.InteractorCreator
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.data.repository.SettingsNavigatorImpl


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            InteractorCreator.getThemeSettingsUseCase,
            InteractorCreator.updateThemeSettingsUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupViews()
    }

    private fun observeViewModel() {
        viewModel.themeState.observe(this) { isDarkTheme ->
            binding.switchTheme.isChecked = isDarkTheme
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        viewModel.navigationEvent.observe(this) { event ->
            when (event) {
                is SettingsViewModel.NavigationEvent.ShareApp ->
                    InteractorCreator.navigateUseCase.shareApp(this, event.message)
                is SettingsViewModel.NavigationEvent.ContactSupport ->
                    InteractorCreator.navigateUseCase.contactSupport(
                        this,
                        event.email,
                        event.subject,
                        event.body
                    )
                is SettingsViewModel.NavigationEvent.OpenUserAgreement ->
                    InteractorCreator.navigateUseCase.openUserAgreement(this, event.url)
            }
        }
    }

    private fun setupViews() {
        binding.toolBar.setNavigationOnClickListener { finish() }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateThemeSettings(isChecked)
        }

        binding.share.setOnClickListener {
            viewModel.onShareAppClicked(getString(R.string.share_message))
        }

        binding.support.setOnClickListener {
            viewModel.onSupportClicked(
                getString(R.string.support_email),
                getString(R.string.support_email_subject),
                getString(R.string.support_email_body)
            )
        }

        binding.userAgreement.setOnClickListener {
            viewModel.onUserAgreementClicked(getString(R.string.user_agreement_url))
        }
    }
}