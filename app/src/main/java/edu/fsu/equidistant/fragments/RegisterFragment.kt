package edu.fsu.equidistant.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        val binding = FragmentRegisterBinding.bind(view)
        binding.apply {
            submitButton.setOnClickListener {
                val username = etUsername.text.toString().trim { it <= ' ' }
                val password = etPassword.text.toString().trim { it <= ' ' }
                val confirmPassword = etValidate.text.toString().trim { it <= ' ' }
                val email = etEmail.text.toString().trim { it <= ' ' }

                if (validate(username, password, confirmPassword, email)) {
                    registerUser(email, password, username)
                }

            }

            textViewLogin.setOnClickListener {
                findNavController().popBackStack()
            }

        }
    }


    private fun validate(
        username: String, password: String,
        confirmPassword: String, number: String
    ): Boolean {

        if (username.isEmpty() || password.isEmpty()
            || confirmPassword.isEmpty() || number.isEmpty()
        ) {
            Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG).show()
            return false
        } else if (password != confirmPassword) {
            Toast.makeText(context, "Passwords don't match", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun registerUser(email: String, password: String, username: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    Toast.makeText(context, "Registration successful", Toast.LENGTH_LONG).show()
                    val action = RegisterFragmentDirections
                        .actionRegisterFragmentToHomeFragment(email, firebaseUser.uid)

                    // add the user to the users collection using their firebase.uid
                    addToFirestore(username, email, firebaseUser.uid)
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

    private fun addToFirestore(
        username: String,
        email: String,
        uid: String
    ) {
        val data = hashMapOf(
            "email" to email,
            "username" to username,
            "token" to null
        )

        database.collection("users").document(uid).set(data)
    }

    private fun retrieveAndStoreToken() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token: String? = task.result

                    val tokenReference = database.collection("users").document(uid)
                    tokenReference
                        .update("token", token)
                        .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error updating document", e) }
                }
            }


    }

}