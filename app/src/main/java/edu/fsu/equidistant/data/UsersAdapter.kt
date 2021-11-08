package edu.fsu.equidistant.data

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import edu.fsu.equidistant.databinding.UserListItemBinding
import edu.fsu.equidistant.notifications.NotificationData
import edu.fsu.equidistant.notifications.PushNotification
import edu.fsu.equidistant.notifications.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

const val TOPIC = "/topics/myTopic"

class UsersAdapter(private val usersList: MutableList<User>)
    : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersAdapter.UserViewHolder {
        val binding = UserListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }


    override fun onBindViewHolder(holder: UsersAdapter.UserViewHolder, position: Int) {
        val currentItem = usersList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    inner class UserViewHolder(private val binding: UserListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.apply{
                    buttonInvite.setOnClickListener {
                        val user = usersList[absoluteAdapterPosition]
                        val message = "You've got a meeting invite! Touch me!"
                        val title = "Invitation to Meet in the Middle"
                        PushNotification(
                            NotificationData(title, message),
                            user.token
                        ).also {
                            sendNotification(it)
                        }

                    }
                }
            }

        fun bind(user: User) {
            binding.apply {
                textViewEmailList.text = user.email
                textViewUsername.text = user.username
            }

        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(TAG, "Response: ${Gson().toJson(response)}")
                } else {
                    Log.e(TAG, response.errorBody().toString())
                }

            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
}