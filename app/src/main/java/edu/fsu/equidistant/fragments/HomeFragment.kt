package edu.fsu.equidistant.fragments

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import edu.fsu.equidistant.*
import edu.fsu.equidistant.databinding.FragmentHomeBinding

private const val TAG = "HomeFragment"

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var locationServiceBound = false


    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("DEBUG","permission granted")
                locationService?.subscribeToLocationUpdates()

            } else {
                Log.d("DEBUG","permission denied")
                Snackbar.make(
                    requireView().findViewById(R.id.fragment_home),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID,
                            null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()

            }
        }


    private val args: HomeFragmentArgs by navArgs()

    private var binding: FragmentHomeBinding? = null

    // Provides location updates for while-in-use feature.
    private var locationService: LocationService? = null

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            if (foregroundPermissionApproved()){
            locationService!!.subscribeToLocationUpdates()}
           // locationService!!.unsubscribeToLocationUpdates()
           // locationService!!.subscribeToLocationUpdates()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
        }
    }

    // Keeps track of users location sharing preferences
    private lateinit var sharedPreferences: SharedPreferences

    // Listens for location broadcasts from LocationService.
    private lateinit var locationServiceBroadcastReceiver: HomeFragment.LocationServiceBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationServiceBroadcastReceiver = LocationServiceBroadcastReceiver()
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

    }

    //TODO CLEAN UP, DONT NEED ANYMORE
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = FragmentHomeBinding.bind(view)
        binding!!.apply {
            textViewEmail.text = args.email
            textViewUserid.text = args.userId
        }
        sharedPreferences=
            activity?.getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE)!!
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    requireView().findViewById(R.id.root_container),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID,
                            null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.option_logout) {
            FirebaseAuth.getInstance().signOut()

            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)


        } else if (id == R.id.option_editProfile) {
            val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(args.userId)
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    // TODO
    //  Do I need this to start the service? I am not setting any views or doing anyting with the data in the fragment?
    /**
     * Receiver for location broadcasts from [ForegroundOnlyLocationService].
     */
    private inner class LocationServiceBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                LocationService.EXTRA_LOCATION
            )
            if (location != null) {
                logResultsToScreen(location.toText())
            }

        }
        // for debug
        private fun logResultsToScreen(output: String) {
            Log.d(TAG,output)
            Toast.makeText(context,output, Toast.LENGTH_LONG).show()
        }
    }

    private fun onLocationChanged(location: Location): GeoPoint {
        val lat = (location.latitude * 1E6).toInt()
        val lng = (location.longitude * 1E6).toInt()
        val point = GeoPoint(lat.toDouble(), lng.toDouble())
        return point
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(context, LocationService::class.java)
        requireActivity().bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationServiceBroadcastReceiver,
            IntentFilter(
                LocationService.ACTION_LOCATION_SERVICE_BROADCAST)
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            locationServiceBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (locationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            locationServiceBound = false
        }
        super.onStop()
    }





}

