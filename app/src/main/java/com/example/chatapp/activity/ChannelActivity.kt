package com.example.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.adapter.MessageAdapter
import com.sendbird.android.*
import com.sendbird.android.BaseChannel.SendUserMessageHandler
import com.sendbird.android.GroupChannel.GroupChannelGetHandler
import kotlinx.android.synthetic.main.activity_chat.*

private const val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
private const val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_ID"

class ChannelActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupChannel: GroupChannel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setUpRecyclerView()
        setButtonHandlers()
    }

    /*** Set up the recyclerview and set the adapter ***/
    private fun setUpRecyclerView() {
        adapter = MessageAdapter(this)
        recyclerView = recycler_gchat
        recyclerView.adapter = adapter
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        recyclerView.layoutManager = layoutManager
        recyclerView.scrollToPosition(0)
    }


    /** Function handles setting handlers for back/send button **/
    private fun setButtonHandlers() {
        val back = button_gchat_back
        back.setOnClickListener {
            val intent = Intent(this, ChannelListActivity::class.java)
            startActivity(intent)
        }

        val send = button_gchat_send
        send.setOnClickListener {
            sendMessage()
        }
    }

    /** Sends the message from the edit text, and clears text field **/
    private fun sendMessage() {
        val params = UserMessageParams()
            .setMessage(edit_gchat_message.text.toString())
        groupChannel.sendUserMessage(params,
            SendUserMessageHandler { userMessage, e ->
                if (e != null) {    // Error.
                    return@SendUserMessageHandler
                }
                // When successfully sent, add it to the adapter, and clear the editText.
                adapter.addFirst(userMessage)
                edit_gchat_message.text.clear()
            })
    }

    /**
     * Get the Channel URL from the passed intent
     */
    private fun getChannelURl(): String? {
        val intent = this.intent
        return intent.getStringExtra(EXTRA_CHANNEL_URL)
    }

    /**
     * Function to get previous messages in channel
     */
    private fun getMessages() {

        val previousMessageListQuery = groupChannel.createPreviousMessageListQuery()

        previousMessageListQuery.load(
            100,
            true
        ) { messages, e ->
            if (e != null) {
                Log.e("Error", e.message.toString())
            }
            // call loadMessages on the adapter
            // gets all the previous messages in the conversation.
            adapter.loadMessages(messages!!, groupChannel)
        }
    }

    /** Handle getting the passed channel, and where will register the channel handler to get various event **/
    // Set a channel handler to get various events such as typing indicators, message read, message delivered events, and on MessageReceived events.
    override fun onResume() {
        super.onResume()
        val channelUrl = getChannelURl()

        // After have the channelURL, make a call with the GroupChannel.getChannel().
        // This call will take the channelUrl and retrieve the channel object to send messages and get relevant channel info.
        GroupChannel.getChannel(channelUrl,
            GroupChannelGetHandler { groupChannel, e ->
                if (e != null) {
                    // Error!
                    e.printStackTrace()
                    return@GroupChannelGetHandler
                }
                this.groupChannel = groupChannel
                getMessages()
            })

        SendBird.addChannelHandler(
            CHANNEL_HANDLER_ID,
            object : SendBird.ChannelHandler() {
                // This event fires every time a message comes in from another user.
                override fun onMessageReceived(
                    baseChannel: BaseChannel,
                    baseMessage: BaseMessage
                ) {
                    if (baseChannel.url == channelUrl) {
                        // Add new message to view
                        adapter.addFirst(baseMessage)
                        groupChannel.markAsRead()
                    }
                }
            })
    }

    override fun onPause() {
        super.onPause()
        // remove the channel handler for clean-up
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
    }
}