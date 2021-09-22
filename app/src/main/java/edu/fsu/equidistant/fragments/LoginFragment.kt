package edu.fsu.equidistant.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import edu.fsu.equidistant.R


class LoginFragment : Fragment() {

    private var listener : LoginFragment.LoginInterface? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView : View = inflater.inflate(R.layout.fragment_login, container, false)
        val submit: Button = rootView.findViewById(R.id.submit)
        val user: EditText = rootView.findViewById(R.id.et_username)
        val pw: EditText = rootView.findViewById(R.id.et_password)
        submit.setOnClickListener{
            if (validate(user,pw)) {
                //TODO implement backend on FireBase
                listener?.onSubmit(user.text.toString(), pw.text.toString())
            }


        }

        return rootView
    }

    //TODO Make more in depth input validation
    private fun validate(username:EditText,password:EditText):Boolean{
        var flag = true
        if (username.text.toString().isEmpty() || username.text.toString().trim().isEmpty()){
            username.error = "Username cannot be empty"
            flag = false
        }
        if (password.text.toString().trim().isEmpty()){
            password.error = "Password cannot be empty"
            flag = false
        }
        return flag
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginFragment.LoginInterface){
            listener = context
        }
        else{
            throw ClassCastException(
                context.toString() + "must implement interface"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface LoginInterface{
        fun onSubmit(username:String,password:String)
    }
}