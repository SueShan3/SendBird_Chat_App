package com.example.chatapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.sendbird.android.SendBird
import com.sendbird.android.User
import kotlinx.android.synthetic.main.item_create.view.*

class ChannelCreateAdapter(context: Context, listener: OnItemCheckedChangeListener) :
    RecyclerView.Adapter<ChannelCreateAdapter.UserHolder>() {

    private var context: Context

    // An interface for when an item gets checked
    interface OnItemCheckedChangeListener {
        fun onItemChecked(user: User, checked: Boolean)
    }

    private var users: MutableList<User>
    private var checkedListener: OnItemCheckedChangeListener

    // A SparseBooleanArray() to handle when Users are checked/unchecked.
    companion object {
        fun selectedUsers() = ArrayList<String>()
        fun sparseArray() = SparseBooleanArray()
    }

    init {
        users = ArrayList()
        this.context = context
        this.checkedListener = listener
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addUsers(users: MutableList<User>) {
        this.users = users
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): UserHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserHolder(layoutInflater.inflate(R.layout.item_create, parent, false))
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        holder.bindViews(context, users[position], position, checkedListener)
    }

    class UserHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val checkbox = view.checkbox_create
        private val userName = view.text_create_user

        fun bindViews(
            context: Context,
            user: User,
            position: Int,
            listener: OnItemCheckedChangeListener
        ) {

            if (user.userId == SendBird.getCurrentUser().userId) {
                userName.text = context.getString(R.string.operator_name, user.nickname)
            } else {
                userName.text = user.nickname
            }

            // Handling checking and unchecking of the CheckBoxes
            checkbox.isChecked = sparseArray().get(position, false)

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                listener.onItemChecked(user, isChecked)

                if (isChecked) {
                    selectedUsers().add(user.userId)
                    sparseArray().put(position, true)
                } else {
                    selectedUsers().remove(user.userId)
                    sparseArray().put(position, false)
                }
            }
        }
    }
}