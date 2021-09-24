package edu.fsu.equidistant.fragments

import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.fsu.equidistant.R
import edu.fsu.equidistant.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRegisterBinding.bind(view)
        binding.apply {
            submitButton.setOnClickListener {
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()
                val confirmPassword = etValidate.text.toString()
                val number = etPhoneNumber.text.toString()

                if (validate(username, password, confirmPassword, number)) {
                    val action = RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    // TODO: Implement Firebase validation
    private fun validate(
        username: String, password: String,
        confirmPassword: String, number: String
    ): Boolean {

        if (username.isEmpty() || password.isEmpty()
            || confirmPassword.isEmpty() || number.isEmpty()) {
            Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG).show()
            return false
        } else if (password != confirmPassword) {
            Toast.makeText(context, "Passwords don't match", Toast.LENGTH_LONG).show()
            return false
        } else if (!PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            Toast.makeText(context, "Invalid number", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

}