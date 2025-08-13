package com.example.playlistmaker.media.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playlistmaker.databinding.FragmentFavoriteTracksBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteTracksViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подписываемся на изменения в ViewModel
        // 1. Настройка RecyclerView, клик-лисенеров
        // 2. Подписка на LiveData из ViewModel
        // 3. Обработка кликов и других UI-событий

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}