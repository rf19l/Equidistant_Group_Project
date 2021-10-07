package edu.fsu.equidistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

//TODO:
// App is still routing to the login page even though the user stays logged in after exiting app. Add functionality to automatically route to Home Fragment if user is already logged in.

//TODO:
// Imple ment buttons of the options menu to exit, logout, etc.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onPrepareOptionsMenu(menu: Menu):Boolean{
        val logout: MenuItem = menu.findItem(R.id.logout)
        val editProfile: MenuItem = menu.findItem(R.id.editProfile);
        logout.setVisible(FirebaseAuth.getInstance().getCurrentUser() != null);
        editProfile.setVisible(FirebaseAuth.getInstance().getCurrentUser() !=null);
        return true;
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

}
