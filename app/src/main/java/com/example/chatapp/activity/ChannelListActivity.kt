package com.example.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.adapter.ChannelListAdapter
import com.sendbird.android.GroupChannel
import kotlinx.android.synthetic.main.activity_channel.*

private const val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"

/*** A channel list to display the Groups Channels that a user can join ***/

class ChannelListActivity : AppCompatActivity(), ChannelListAdapter.OnChannelClickedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelListAdapter

    private val handler = Handler()
    private lateinit var refreshRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)

        adapter = ChannelListAdapter(this, this)

        // Set up recycler view
        recyclerView = recycler_group_channels
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fab_group_channel_create.setOnClickListener {
            val intent = Intent(this, ChannelCreateActivity::class.java)
            startActivity(intent)
        }

        addChannels()

        //Auto Refresh Adapter
        refreshRunnable = Runnable {
            // Update the data source
            // Notify the adapter that the data has changed
            addChannels()

            // Schedule the next update
            handler.postDelayed(refreshRunnable, 1000)
        }
    }

    override fun onStart() {
        super.onStart()

        // Start auto-refreshing
        handler.postDelayed(refreshRunnable, 1000)
    }

    override fun onStop() {
        super.onStop()

        // Stop auto-refreshing
        handler.removeCallbacks(refreshRunnable)
    }


    /** Create a query that retrieves a specified range of channels **/
    // Retrieve more channels by checking the .hasNext() call on the query object.
    private fun addChannels() {
        val channelList = GroupChannel.createMyGroupChannelListQuery()
        channelList.limit = 100
        channelList.next { list, e ->
            if (e != null) {
                Log.e("TAG", e.message.toString())
            }
            adapter.clearMap()
            adapter.addChannels(list)
        }
    }

    override fun onItemClicked(channel: GroupChannel) {
        // Create an intent to start the ChannelActivity with the channel.URL
        val intent = Intent(this, ChannelActivity::class.java)
        intent.putExtra(EXTRA_CHANNEL_URL, channel.url)
        startActivity(intent)
    }
}