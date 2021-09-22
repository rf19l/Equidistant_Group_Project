package edu.fsu.equidistant.fragments

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import edu.fsu.equidistant.R

class RegisterFragment : Fragment() {

    var listener : RegisterInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView: View = inflater.inflate(R.layout.fragment_register, container, false)

        val username: EditText = rootView.findViewById(R.id.et_username)
        val password: EditText = rootView.findViewById(R.id.et_password)
        val pw_validate: EditText = rootView.findViewById(R.id.et_validate)
        val phone :EditText = rootView.findViewById(R.id.et_phoneNumber)
        val submit: Button =rootView.findViewById(R.id.submit)

        //TODO implement backend on FireBase
        submit.setOnClickListener{
            if (validate(username,password,pw_validate,phone)) {
                listener?.onSubmit(
                    username.text.toString(),
                    password.text.toString(),
                    phone.text.toString()
                )
            }
        }

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RegisterFragment.RegisterInterface){
            listener = context
        }
        else{
            throw ClassCastException(
                context.toString() + "must implement interface"
            )
        }
    }

    //TODO implement more robust error checking
    fun validate(username: EditText,password: EditText,valid_pw: EditText,cell: EditText):Boolean{
        var valid = true
        if(username.text.toString().trim().isEmpty()){
            username.error = "Username field required"
            valid = false
        }
        if (password.text.toString().trim().isEmpty()){
            password.error = "Password field required"
            valid = false
        }
        if (! valid_pw.text.toString().equals(password.text.toString())){
            valid_pw.error = "Passwords do not match"
            valid = false
        }
        if (! PhoneNumberUtils.isGlobalPhoneNumber(cell.text.toString())){
            cell.error = "Invalid phone number"
            valid = false
        }
        return valid
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface RegisterInterface{
        fun onSubmit(username:String,password:String,phoneNumber:String)
    }

}