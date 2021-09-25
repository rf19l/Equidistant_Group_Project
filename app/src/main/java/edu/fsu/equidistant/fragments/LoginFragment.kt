package edu.fsu.equidistant.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentLoginBinding


class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentLoginBinding.bind(view)
        binding.apply {
            loginButton.setOnClickListener {

                val email = etUsername.text.toString()
                val password = etPassword.text.toString()

                if (isUsernameOrPasswordEmpty(email, password)) {
                    Toast.makeText(
                        context,
                        "Username or Password cannot be empty",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    loginUser(email, password)
                }
            }

            textViewRegister.setOnClickListener {
                val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                findNavController().navigate(action)
            }

        }

    }


    private fun isUsernameOrPasswordEmpty(username: String, password: String) =
        username.isEmpty() || password.isEmpty()

    private fun loginUser(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
                    Toast.makeText(context, "Registration successful", Toast.LENGTH_LONG).show()
                    val action = LoginFragmentDirections
                        .actionLoginFragmentToHomeFragment(email, firebaseUser)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(
                        context,
                        task.exception!!.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }


}