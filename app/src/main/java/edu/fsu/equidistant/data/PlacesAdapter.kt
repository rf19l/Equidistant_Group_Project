package edu.fsu.equidistant.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.fsu.equidistant.databinding.PlaceListItemBinding
import edu.fsu.equidistant.places.GooglePlaceModel

class PlacesAdapter(private var placesList: ArrayList<GooglePlaceModel>) :
    RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesAdapter.PlaceViewHolder {
        val binding = PlaceListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlacesAdapter.PlaceViewHolder, position: Int) {
        val currentItem = placesList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    inner class PlaceViewHolder(private val binding: PlaceListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

        }

        fun bind(place: GooglePlaceModel) {
            binding.apply {
                textViewPlaceNameLi.text = place.name
                textViewAddress.text = place.vicinity
                ratingBar.rating = place.rating?.toFloat()!!
                textViewCurrentlyOpen.text = place.businessStatus
            }

        }
    }


}