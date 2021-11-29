package edu.fsu.equidistant

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import edu.fsu.equidistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)
                as NavHostFragment

        navController = navHostFragment.navController
        setSupportActionBar(binding.toolBar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.meetingFragment, R.id.loginFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.apply {
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.loginFragment
                    || destination.id == R.id.registerFragment
                ) {
                    bottomNavigation.visibility = View.GONE
                } else {
                    bottomNavigation.visibility = View.VISIBLE
                }
            }

            bottomNavigation.setupWithNavController(navController)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

}