package edu.fsu.equidistant.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import edu.fsu.equidistant.MainActivity
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val args: HomeFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)
        binding.apply {
            textViewEmail.text = args.email
            textViewUserid.text = args.userId

            buttonLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()

                val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                findNavController().navigate(action)
            }
        }
    }


}