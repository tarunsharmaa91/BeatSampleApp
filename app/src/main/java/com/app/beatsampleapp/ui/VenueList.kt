package com.app.classify.ui.list.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.beatsampleapp.databinding.VenueListFragmentBinding
import com.app.beatsampleapp.adapter.VenueListAdapter
import com.app.beatsampleapp.viewmodels.MainActivityViewModel

class VenueList : Fragment() {
    private lateinit var model: MainActivityViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = VenueListFragmentBinding.inflate(inflater)

        //To allow data binding to observe live data with life cycle of fragment
        binding.lifecycleOwner = this

        model =
            ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
        //Now I have to give binding access to AdListViewModel
         binding.viewModel = model

         binding.adList.adapter = VenueListAdapter(VenueListAdapter.OnClickListener {
             model.setNavigationModeItem(it)
         })

        return binding.root
    }


}
