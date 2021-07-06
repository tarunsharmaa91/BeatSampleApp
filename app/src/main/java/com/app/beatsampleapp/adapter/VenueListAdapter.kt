package com.app.beatsampleapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.beatsampleapp.databinding.VenueCardBinding
import com.app.beatsampleapp.model.LocationResult

class VenueListAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<LocationResult, VenueListAdapter.ListViewHolder>(DiffCallback) {

    class ListViewHolder(private var binding: VenueCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ads: LocationResult) {
            binding.listing = ads
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LocationResult>() {
        override fun areItemsTheSame(oldItem: LocationResult, newItem: LocationResult): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: LocationResult, newItem: LocationResult): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(VenueCardBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val results = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(results)
        }
        holder.bind(results)
    }

    class OnClickListener(val clickListener: (results: LocationResult) -> Unit) {
        fun onClick(results: LocationResult) = clickListener(results)
    }
}