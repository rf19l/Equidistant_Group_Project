package edu.fsu.equidistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import edu.fsu.equidistant.R

/*Fragment imports */
import edu.fsu.equidistant.fragments.*
import edu.fsu.equidistant.fragments.LoginFragment
import edu.fsu.equidistant.fragments.RegisterFragment
import edu.fsu.equidistant.fragments.SplashFragment

class MainActivity : AppCompatActivity(), SplashFragment.SplashInterface, LoginFragment.LoginInterface,
RegisterFragment.RegisterInterface{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onSplash()
    }

    private fun onSplash():Unit{
        val fragment : SplashFragment = SplashFragment()
        val tag = fragment::class.java.canonicalName
        supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame,fragment,tag).commitNow()
    }

    override fun onLogin() {
        val fragment: Fragment = LoginFragment()
        val tag = fragment::class.java.canonicalName
        supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame,fragment,tag).commitNow()
    }

    override fun onRegister() {
        val fragment: Fragment = RegisterFragment()
        val tag = fragment::class.java.canonicalName
        supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame,fragment,tag).commitNow()
    }

    override fun onSubmit(username: String, password: String) {
        val fragment: Fragment = HomeFragment()
        val tag = fragment::class.java.canonicalName
        supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame,fragment,tag).commitNow()
    }

    override fun onSubmit(username: String, password: String, phoneNumber: String) {
        val fragment: Fragment = HomeFragment()
        val tag = fragment::class.java.canonicalName
        supportFragmentManager.beginTransaction().replace(R.id.fragmentFrame,fragment,tag).commitNow()
    }
}