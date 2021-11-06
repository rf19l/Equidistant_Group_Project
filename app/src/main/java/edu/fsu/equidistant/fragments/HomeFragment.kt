package edu.fsu.equidistant.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentHomeBinding
import kotlin.system.exitProcess

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val args: HomeFragmentArgs by navArgs()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var binding: FragmentHomeBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding = FragmentHomeBinding.bind(view)
        binding!!.apply {
            textViewEmail.text = args.email
            textViewUserid.text = args.userId
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
}

