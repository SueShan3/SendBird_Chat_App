package com.example.chatapp.utils

import com.sendbird.android.GroupChannel
import com.sendbird.android.SendBird

class TextUtils {
    companion object {
        fun getGroupChannelTitle(channel: GroupChannel): String? {
            val members = channel.members
            return if (members.size < 2 || SendBird.getCurrentUser() == null) {
                "No Members"
            } else if (members.size == 2) {
                val names = StringBuffer()
                for (member in members) {
                    if (member.userId == SendBird.getCurrentUser().userId) {
                        continue
                    }
                    names.append(", " + member.nickname)
                }
                names.delete(0, 2).toString()
            } else {
                var count = 0
                val names = StringBuffer()
                for (member in members) {
                    if (member.userId == SendBird.getCurrentUser().userId) {
                        continue
                    }
                    count++
                    names.append(", " + member.nickname)
                    if (count >= 10) {
                        break
                    }
                }
                names.delete(0, 2).toString()
            }
        }
    }
}