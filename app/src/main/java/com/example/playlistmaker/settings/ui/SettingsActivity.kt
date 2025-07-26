package com.example.playlistmaker.settings.ui

import com.example.playlistmaker.settings.data.repository.SettingsNavigatorImpl
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.domain.repository.NavigationEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModel()
    private val navigator by lazy { SettingsNavigatorImpl(this) }

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
            viewModel.onShareAppClicked(getString(R.string.share_message))
        }

        binding.support.setOnClickListener {
            viewModel.onSupportClicked(
                email = getString(R.string.support_email),
                subject = getString(R.string.support_email_subject),
                body = getString(R.string.support_email_body)
            )
        }

        binding.userAgreement.setOnClickListener {
            viewModel.onUserAgreementClicked(getString(R.string.user_agreement_url))
        }
    }

    private fun observeViewModel() {
        viewModel.navigationEvent.observe(this) { event ->
            when (event) {
                is NavigationEvent.ShareApp -> navigator.navigate(event)
                is NavigationEvent.ContactSupport -> navigator.navigate(event)
                is NavigationEvent.OpenUserAgreement -> navigator.navigate(event)
            }
        }
    }
}