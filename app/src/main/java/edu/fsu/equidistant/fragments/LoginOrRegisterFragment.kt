package edu.fsu.equidistant.fragments
import edu.fsu.equidistant.R

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import edu.fsu.equidistant.databinding.FragmentLoginOrRegisterBinding


class LoginOrRegisterFragment : Fragment(R.layout.fragment_login_or_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginOrRegisterBinding.bind(view)
        binding.apply {

            loginButton.setOnClickListener {
                val action = LoginOrRegisterFragmentDirections
                    .actionLoginOrRegisterFragmentToLoginFragment()
                findNavController().navigate(action)
            }

            registerButton.setOnClickListener {
                val action = LoginOrRegisterFragmentDirections
                    .actionLoginOrRegisterFragmentToRegisterFragment()
                findNavController().navigate(action)
            }
        }
    }


}