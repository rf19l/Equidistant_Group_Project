package edu.fsu.equidistant.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentHomeBinding

class ProfileFragment : Fragment(R.layout.fragment_home) {

    private val args: HomeFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    /*
        Options Menu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.option_logout) {
            FirebaseAuth.getInstance().signOut()
            val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        return super.onOptionsItemSelected(item)

    }

    // hide edit profile option while on profile page
    override fun onPrepareOptionsMenu(menu: Menu) {
        val logout: MenuItem = menu.findItem(R.id.option_editProfile)
        logout.setVisible(false)
        super.onPrepareOptionsMenu(menu)
    }


}