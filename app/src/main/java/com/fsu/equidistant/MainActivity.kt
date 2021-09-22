package com.fsu.equidistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

/*Fragment imports */
import com.fsu.equidistant.fragments.*

class MainActivity : AppCompatActivity(), SplashFragment.SplashInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onSplash()
    }

    fun onSplash():Unit{
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
}