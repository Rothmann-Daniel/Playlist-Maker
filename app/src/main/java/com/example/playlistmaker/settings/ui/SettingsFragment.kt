package com.example.playlistmaker.settings.ui

import com.example.playlistmaker.settings.data.repository.SettingsNavigatorImpl
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.domain.repository.NavigationEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()
    private lateinit var navigator: SettingsNavigatorImpl

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigator = SettingsNavigatorImpl(requireActivity())

        setupThemeSwitch()
        setupClickListeners()
        observeViewModel()
    }


    private fun setupThemeSwitch() {
        viewModel.themeState.observe(viewLifecycleOwner) { isDarkTheme ->
            binding.switchTheme.isChecked = isDarkTheme
        }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateThemeSettings(isChecked)
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
        viewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NavigationEvent.ShareApp -> navigator.navigate(event)
                is NavigationEvent.ContactSupport -> navigator.navigate(event)
                is NavigationEvent.OpenUserAgreement -> navigator.navigate(event)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
