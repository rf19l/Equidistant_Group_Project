package edu.fsu.equidistant.fragments

import android.Manifest
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import edu.fsu.equidistant.BuildConfig
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.SharedViewModel
import edu.fsu.equidistant.data.User
import edu.fsu.equidistant.data.UsersAdapter
import edu.fsu.equidistant.databinding.FragmentHomeBinding
import edu.fsu.equidistant.location.service.MyLocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.system.exitProcess

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val args: HomeFragmentArgs by navArgs()
    private val viewModel: SharedViewModel by activityViewModels()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var usersAdapter: UsersAdapter

    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser == null) {
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)
        } else {
            retrieveAndStoreToken()
        }

        val serviceIntent = Intent(context, MyLocationService::class.java)
        requireActivity().bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        createDocument()

        val usersList: MutableList<User> = mutableListOf()
        usersAdapter = UsersAdapter(usersList, viewModel.meetingID)
        val binding = FragmentHomeBinding.bind(view)

        binding.apply {
            recyclerViewUserList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            getUsersList(usersAdapter, binding, usersList)
        }

        if (foregroundPermissionApproved()) {
            foregroundOnlyLocationService?.subscribeToLocationUpdates()
                ?: Log.d(TAG, "Service Not Bound")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_logout -> {
                clearToken(FirebaseAuth.getInstance().currentUser!!.uid)
                FirebaseAuth.getInstance().signOut()
                val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                findNavController().navigate(action)
                true
            }
            R.id.option_editProfile -> {
                val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(args.userId)
                findNavController().navigate(action)
                true
            }
            R.id.option_quit -> {
                exitProcess(0)
            }
            R.id.action_search -> {
                search(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // create that menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
    }

    private fun clearToken(userId: String) {
        database.collection("users")
            .document(userId)
            .update("token", "")
    }

    private fun getUsersList(
        usersAdapter: UsersAdapter,
        binding: FragmentHomeBinding,
        usersList: MutableList<User>
    ) {

        database.collection("users")
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                usersList.clear()

                for (document in documents!!) {
                    val data = document.data
                    val user = User(
                        data["username"].toString(),
                        data["email"].toString(),
                        data["token"].toString(),
                        data["longitude"] as Double,
                        data["latitude"] as Double
                    )

                    Log.d(TAG, "GetUsersList longitude: ${data["longitude"]}")

                    usersList.add(user)
                }

                binding.recyclerViewUserList.adapter = usersAdapter
            }
    }

    private fun search(item: MenuItem) {
        val searchView: SearchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                usersAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun createDocument() {
        val docRef = database.collection("meetings")
            .document(viewModel.meetingID.toString())

        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "Doc exists!")
                } else {
                    database.collection("meetings")
                        .document(viewModel.meetingID.toString())
                        .set({})
                }

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    private fun retrieveAndStoreToken() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token: String? = task.result

                    val userRef = database.collection("users").document(uid)
                    userRef
                        .update("token", token)
                        .addOnSuccessListener {
                            Log.d(
                                TAG,
                                "DocumentSnapshot successfully updated!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                }
            }
    }

    /*
    LOCATION STUFF
     */

    private var foregroundOnlyLocationServiceBound = false
    private var foregroundOnlyLocationService: MyLocationService? = null
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            if (foregroundPermissionApproved()){
                foregroundOnlyLocationService!!.subscribeToLocationUpdates()}
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("DEBUG","permission granted")
                foregroundOnlyLocationService?.subscribeToLocationUpdates()

            } else {
                Log.d("DEBUG","permission denied")
                Snackbar.make(
                    requireView().findViewById(R.id.homeFragment),
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

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {}
            }
        }
    }

}