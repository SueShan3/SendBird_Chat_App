package com.example.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.adapter.ChannelCreateAdapter
import com.sendbird.android.GroupChannel
import com.sendbird.android.GroupChannelParams
import com.sendbird.android.SendBird
import com.sendbird.android.User
import kotlinx.android.synthetic.main.activity_create.*

private const val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"

/*** A screen that allows a user to invite other users and create a channel ***/

class ChannelCreateActivity : AppCompatActivity(),
    ChannelCreateAdapter.OnItemCheckedChangeListener {

    private lateinit var selectedUsers: ArrayList<String>
    private lateinit var adapter: ChannelCreateAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        // The list of users you pass when creating a channel
        selectedUsers = ArrayList()

        adapter = ChannelCreateAdapter(this, this)
        recyclerView = recycler_create
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadUsers()
        button_create.setOnClickListener { createChannel(selectedUsers) }

        toolbar = toolbar_channel_create
        toolbar.title = ""
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** Uses the default generic query to get the application's users. **/
    private fun loadUsers() {
        val userListQuery = SendBird.createApplicationUserListQuery()

        userListQuery.next { list, e ->
            if (e != null) {
                Log.e("TAG", e.message.toString())
            } else {
                // add the returned users to the adapter
                adapter.addUsers(list)
            }
        }
    }

    /** Create channel with the passed users **/
    // By creating a params val, you can add users, add operators (Admins), set the channel name, and so on.
    private fun createChannel(users: MutableList<String>) {
        val params = GroupChannelParams()

        // operators = Admins
        val operatorId = ArrayList<String>()
        operatorId.add(SendBird.getCurrentUser().userId)

        // Specifying your optional params
        params.addUserIds(users)
        params.setOperatorUserIds(operatorId)

        GroupChannel.createChannel(params) { groupChannel, e ->
            if (e != null) {
                Log.e("TAG", e.message.toString())
            } else {
                val intent = Intent(this, ChannelActivity::class.java)
                intent.putExtra(EXTRA_CHANNEL_URL, groupChannel.url)
                startActivity(intent)
            }
        }
    }

    override fun onItemChecked(user: User, checked: Boolean) {
        // Indicates whether a user has been checked or not, and updates the selectedUsers list accordingly.
        if (checked) {
            selectedUsers.add(user.userId)
        } else {
            selectedUsers.remove(user.userId)
        }
    }
}