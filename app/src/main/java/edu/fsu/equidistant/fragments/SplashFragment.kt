package edu.fsu.equidistant.fragments
import android.content.Context
import edu.fsu.equidistant.R

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


/**
 * A simple [Fragment] subclass.
 * Use the [SplashFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SplashFragment : Fragment() {

    var listener : SplashInterface? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val rootView : View = inflater.inflate(R.layout.fragment_splash, container, false)
        val login = rootView.findViewById<Button>(R.id.loginButton)
        val register = rootView.findViewById<Button>(R.id.registerButton)
        login.setOnClickListener{
            listener?.onLogin()
        }
        register.setOnClickListener{
            listener?.onRegister()
        }
        return rootView;
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SplashInterface){
            listener = context
        }
        else{
            throw ClassCastException(
                context.toString() + "must impliment interface"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface SplashInterface{
        fun onLogin()
        fun onRegister()

    }
}