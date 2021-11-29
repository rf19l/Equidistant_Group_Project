package edu.fsu.equidistant

<<<<<<<<< Temporary merge branch 1
import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import kotlin.system.exitProcess
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar

//private const val TAG = "MainActivity"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

class MainActivity : AppCompatActivity(){

    companion object{
        val TAG: String = MainActivity::class.java.simpleName
        var PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        )
    }


=========
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
>>>>>>>>> Temporary merge branch 2

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
<<<<<<<<< Temporary merge branch 1
        setContentView(R.layout.activity_main)

    }
    /*
        Menu Functions
     */
    override fun onPrepareOptionsMenu(menu: Menu):Boolean{
        val logout: MenuItem = menu.findItem(R.id.option_logout)
        val editProfile: MenuItem = menu.findItem(R.id.option_editProfile);
        logout.setVisible(FirebaseAuth.getInstance().getCurrentUser() != null);
        editProfile.setVisible(FirebaseAuth.getInstance().getCurrentUser() !=null);
        return true;
    }

=========
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
>>>>>>>>> Temporary merge branch 2

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }


}
