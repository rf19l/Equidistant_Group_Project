package edu.fsu.equidistant.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.fsu.equidistant.databinding.UserInMeetingItemBinding
import edu.fsu.equidistant.databinding.UserListItemBinding

class MeetingAdapter(private var usersList: MutableList<User>) :
    RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingAdapter.MeetingViewHolder {
        val binding = UserInMeetingItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MeetingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeetingAdapter.MeetingViewHolder, position: Int) {
        val currentItem = usersList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    inner class MeetingViewHolder(private val binding: UserInMeetingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {

        }

        fun bind(user: User) {
            binding.apply {
                textViewEmailList.text = user.email
                textViewUsername.text = user.username
            }

        }
    }


}