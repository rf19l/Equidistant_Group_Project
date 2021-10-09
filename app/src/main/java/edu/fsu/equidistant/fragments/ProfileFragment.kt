package edu.fsu.equidistant.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.fsu.equidistant.MainActivity
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(R.layout.fragment_home) {
    private val storage = Firebase.storage
    private val args: ProfileFragmentArgs by navArgs()
    private var binding: FragmentProfileBinding? = null
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        user = FirebaseAuth.getInstance().currentUser
        binding!!.apply{
            imageViewProfilePhoto.setOnClickListener {
                choosePicture()
            }

        }



    }

    private fun choosePicture(){
        val intent : Intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        //TODO Get images from gallery
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        user = null
    }


}