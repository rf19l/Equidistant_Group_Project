package edu.fsu.equidistant.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import edu.fsu.equidistant.R


class MapFragment : Fragment(R.layout.fragment_map) {
//    , OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val args: MapFragmentArgs by navArgs()

//    override fun onMapReady(googleMap: GoogleMap?) {
//        if (googleMap != null) {
//            map = googleMap
//        }
//
//        val placesSearchResults = NearbySearch(args.latitude.toDouble(), args.longitude.toDouble()).run().results
//
//        Log.e("response1Tag", placesSearchResults[0].toString())
//        Log.e("response2Tag", placesSearchResults[1].toString())
//
//        val lat1 = placesSearchResults[0].geometry.location.lat
//        val lng1 = placesSearchResults[0].geometry.location.lng
//
//        val lat2 = placesSearchResults[1].geometry.location.lat
//        val lng2 = placesSearchResults[1].geometry.location.lng
//
//        map.addMarker(MarkerOptions().position(LatLng(lat1, lng1)))
//        map.addMarker(MarkerOptions().position(LatLng(lat2, lng2)))
//
//        map.setMinZoomPreference(14.0f)
//        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat1, lng1)))
//    }

}