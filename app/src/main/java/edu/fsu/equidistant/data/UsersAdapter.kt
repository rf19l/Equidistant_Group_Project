package edu.fsu.equidistant.data

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.fsu.equidistant.databinding.UserListItemBinding

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
                        Log.d("User: ", user.toString())
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
}