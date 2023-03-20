package com.example.chatapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatapp.R
import com.example.chatapp.utils.DateUtils
import com.sendbird.android.*
import kotlinx.android.synthetic.main.item_chat_me.view.*
import kotlinx.android.synthetic.main.item_chat_other.view.*
import java.util.*

private const val VIEW_TYPE_USER_MESSAGE_ME = 10
private const val VIEW_TYPE_USER_MESSAGE_OTHER = 11

class MessageAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages: MutableList<BaseMessage>
    private var mChannel: GroupChannel? = null
    private var context: Context

    init {
        messages = ArrayList()
        this.context = context
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadMessages(messages: MutableList<BaseMessage>, groupChannel: GroupChannel) {
        this.messages = messages
        this.mChannel = groupChannel
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addFirst(message: BaseMessage) {
        messages.add(0, message)
        notifyDataSetChanged()
    }

    // Return the customViewHolder that corresponds to the type of message.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_USER_MESSAGE_ME -> {
                MyUserHolder(layoutInflater.inflate(R.layout.item_chat_me, parent, false))
            }
            VIEW_TYPE_USER_MESSAGE_OTHER -> {
                OtherUserHolder(layoutInflater.inflate(R.layout.item_chat_other, parent, false))
            }
            else -> MyUserHolder(
                layoutInflater.inflate(
                    R.layout.item_chat_me,
                    parent,
                    false
                )
            ) //Generic return
        }
    }

    // Determine whether it is a "Me" message or an "Other" message and will return accordingly.
    override fun getItemViewType(position: Int): Int {

        return when (val message = messages[position]) {
            is UserMessage -> {
                if (message.sender.userId.equals(SendBird.getCurrentUser().userId)) VIEW_TYPE_USER_MESSAGE_ME
                else VIEW_TYPE_USER_MESSAGE_OTHER
            }
            //Handle other types of messages FILE/ADMIN ETC
            else -> -1
        }
    }

    // Return the current position of the message.
    override fun getItemCount() = messages.size

    // Binds the messages to the views.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var isContinuous = false
        var isNewDay = false

        // If there is at least one item preceding the current one, check the previous message.

        // If there is at least one item preceding the current one, check the previous message.
        if (position < messages.size - 1) {
            val prevMessage: BaseMessage = messages[position + 1]

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(messages[position].createdAt, prevMessage.createdAt)) {
                isNewDay = true
                isContinuous = false
            } else {
                isContinuous = isContinuous(messages[position], prevMessage)
            }
        } else if (position == messages.size - 1) {
            isNewDay = true
        }

        when (holder.itemViewType) {
            VIEW_TYPE_USER_MESSAGE_ME -> {
                holder as MyUserHolder
                holder.bindView(
                    context,
                    messages[position] as UserMessage,
                    mChannel,
                    isContinuous,
                    isNewDay
                )
            }
            VIEW_TYPE_USER_MESSAGE_OTHER -> {
                holder as OtherUserHolder
                holder.bindView(
                    context,
                    messages[position] as UserMessage,
                    mChannel,
                    isContinuous,
                    isNewDay
                )
            }
            //Handle other types of messages FILE/ADMIN ETC
        }
    }

    private fun isContinuous(currentMsg: BaseMessage?, precedingMsg: BaseMessage?): Boolean {
        // null check
        if (currentMsg == null || precedingMsg == null) {
            return false
        }

        var currentUser: User? = null
        var precedingUser: User? = null

        if (currentMsg is UserMessage) {
            currentUser = currentMsg.sender
        }

        if (precedingMsg is UserMessage) {
            precedingUser = precedingMsg.sender
        }

        return !(currentUser == null || precedingUser == null) && currentUser.userId == precedingUser.userId
    }

    // Binds the respective message sent by "Me" to the item
    class MyUserHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val messageText = view.text_gchat_message_me
        private val date = view.text_gchat_date_me
        private val messageDate = view.text_gchat_timestamp_me
        private val padding = view.view_group_chat_padding
        private val messageStatusView = view.message_status_group_chat

        fun bindView(
            context: Context,
            message: UserMessage,
            channel: GroupChannel?,
            isContinuous: Boolean,
            isNewDay: Boolean
        ) {

            messageText.text = message.message
            messageDate.text = DateUtils.formatTime(message.createdAt)

            // If continuous from previous message, remove extra padding.
            if (isContinuous) {
                padding.visibility = View.GONE
            } else {
                padding.visibility = View.VISIBLE
            }

            // If the message is sent on a different date than the previous one, display the date.
            if (isNewDay) {
                date.visibility = View.VISIBLE
                date.text = DateUtils.formatDate(message.createdAt)
            } else {
                date.visibility = View.GONE
            }

            if (channel != null) {
                messageStatusView.drawMessageStatus(channel, message)
            }
        }
    }

    // Binds the respective message sent by "Other" to the item
    class OtherUserHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val messageText = view.text_gchat_message_other
        private val date = view.text_gchat_date_other
        private val timestamp = view.text_gchat_timestamp_other
        private val profileImage = view.image_gchat_profile_other
        private val user = view.text_gchat_user_other

        fun bindView(
            context: Context,
            message: UserMessage,
            channel: GroupChannel?,
            isContinuous: Boolean,
            isNewDay: Boolean
        ) {

            messageText.text = message.message
            timestamp.text = DateUtils.formatTime(message.createdAt)

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                date.visibility = View.VISIBLE
                date.text = DateUtils.formatDate(message.createdAt)
            } else {
                date.visibility = View.GONE
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.visibility = View.INVISIBLE
                user.visibility = View.GONE
            } else {
                profileImage.visibility = View.VISIBLE
                Glide.with(context).load(message.sender.profileUrl).circleCrop()
                    .apply(RequestOptions().override(75, 75))
                    .into(profileImage)
                user.visibility = View.VISIBLE
                user.text = message.sender.nickname
            }
        }
    }
}