package edu.fsu.equidistant.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.fsu.equidistant.R
import edu.fsu.equidistant.data.User
import edu.fsu.equidistant.data.UsersAdapter
import edu.fsu.equidistant.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.system.exitProcess

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val args: HomeFragmentArgs by navArgs()
    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var usersAdapter: UsersAdapter
    private val meetingID: UUID = UUID.randomUUID()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        createDocument()

        val usersList: MutableList<User> = mutableListOf()
        usersAdapter = UsersAdapter(usersList, meetingID)
        val binding = FragmentHomeBinding.bind(view)

        binding.apply {
            recyclerViewUserList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            getUsersList(usersAdapter, binding, usersList)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_logout -> {
                clearToken(FirebaseAuth.getInstance().currentUser!!.uid)
                FirebaseAuth.getInstance().signOut()
                val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                findNavController().navigate(action)
                true
            }
            R.id.option_editProfile -> {
                val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(args.userId)
                findNavController().navigate(action)
                true
            }
            R.id.option_quit -> {
                exitProcess(0)
            }
            R.id.action_search -> {
                search(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // create that menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
    }

    private fun clearToken(userId: String) {
        database.collection("users")
            .document(userId)
            .update("token", "")
    }

    private fun getUsersList(
        usersAdapter: UsersAdapter,
        binding: FragmentHomeBinding,
        usersList: MutableList<User>
    ) {

        database.collection("users")
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error)
                    return@addSnapshotListener
                }

                // TODO(): Perhaps use ListAdapter DiffUtil instead of clear() or FirestoreRecyclerView (auto updates)
                usersList.clear()

                for (document in documents!!) {
                    val data = document.data
                    val user = User(
                        data["username"].toString(),
                        data["email"].toString(),
                        data["token"].toString()
                    )

                    usersList.add(user)
                }

                binding.recyclerViewUserList.adapter = usersAdapter
            }
    }

    private fun search(item: MenuItem) {
        val searchView: SearchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                usersAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun createDocument() {
        database.collection("meetings")
            .document(meetingID.toString())
            .set({})
    }
}

