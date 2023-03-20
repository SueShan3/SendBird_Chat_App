package com.example.chatapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.sendbird.android.SendBird
import kotlinx.android.synthetic.main.activity_login.*

/*** A basic Login screen for users to log in and set their nickname ***/

private const val APP_ID = "F542B22E-7EA8-4D10-9B17-D28CC6868631"

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize the Sendbird app with your respective AppID
        SendBird.init(APP_ID, this)

        button_login_connect.setOnClickListener {
            connectToSendBird(et_login_user_id.text.toString(), et_login_nickname.text.toString())
        }
    }

    private fun connectToSendBird(userID: String, nickname: String) {
        // Connect to Sendbird with a UserID and nickname
        // Sendbird server can authenticate a user with just a unique user ID.
        // If no matching userID is found, the server creates a new user account with the userID inputs.
        SendBird.connect(userID) { _, e ->
            if (e != null) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            } else {
                // Update the Nickname of the connected user
                SendBird.updateCurrentUserInfo(nickname, null) {
                    if (it != null) {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
                    val intent = Intent(this, ChannelListActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}