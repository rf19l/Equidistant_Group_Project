package edu.fsu.equidistant.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import edu.fsu.equidistant.databinding.PlaceListItemBinding
import edu.fsu.equidistant.places.GooglePlaceModel
import java.net.URLEncoder

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
            binding.apply {
                root.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val encodedAddress = URLEncoder
                            .encode(placesList[position].vicinity, "UTF-8")
                        val intentUri = Uri.parse("google.navigation:q=$encodedAddress")

                        val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        val context: Context = root.context
                        context.startActivity(mapIntent)
                    }
                }
            }
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