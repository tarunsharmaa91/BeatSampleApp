package com.app.beatsampleapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.beatsampleapp.databinding.SelectedVenueCardBinding
import com.app.beatsampleapp.ui.viewmodels.MainActivityViewModel

/**
 * A fragment representing a list of Items.
 */
class SelectedItemFragment : Fragment() {
    private lateinit var model: MainActivityViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = SelectedVenueCardBinding.inflate(inflater)

        //To allow data binding to observe live data with life cycle of fragment
        binding.lifecycleOwner = this

        model = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        //Now I have to give binding access to AdListViewModel
        binding.viewModel = model

        return binding.root
    }
}