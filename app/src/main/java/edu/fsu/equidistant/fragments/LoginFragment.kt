package edu.fsu.equidistant.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentLoginBinding


class LoginFragment : Fragment(R.layout.fragment_login) {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

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
                    val action = LoginFragmentDirections
                        .actionLoginFragmentToHomeFragment(email, firebaseUser)

                    retrieveAndStoreToken()
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

    private fun retrieveAndStoreToken() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token: String? = task.result

                    val userRef = database.collection("users").document(uid)
                    userRef
                        .update("token", token)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                }
            }

    }
}