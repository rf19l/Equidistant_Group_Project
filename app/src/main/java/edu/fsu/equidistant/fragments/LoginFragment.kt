package edu.fsu.equidistant.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentLoginBinding


class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)
        binding.apply {
            submitButton.setOnClickListener {

                val username = etUsername.text.toString()
                val password = etPassword.text.toString()

                // TODO: Make a proper validation with Firebase integration
                if (isUsernameOrPasswordEmpty(username, password)) {
                    val toast = Toast
                        .makeText(context, "Username or Password cannot be empty", Toast.LENGTH_LONG)
                    toast.show()
                } else {
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
            }
        }

    }


    private fun isUsernameOrPasswordEmpty(username: String, password: String) =
        username.isEmpty() || password.isEmpty()



}