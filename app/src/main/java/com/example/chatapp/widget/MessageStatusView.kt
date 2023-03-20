package com.example.chatapp.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.chatapp.R
import com.sendbird.android.BaseMessage
import com.sendbird.android.GroupChannel

class MessageStatusView : FrameLayout {
    private var messageStatus: ImageView? = null
    private var progressBar: ProgressBar? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.view_message_status, this, true)
        messageStatus = view.findViewById(R.id.image_message_status)
        progressBar = view.findViewById(R.id.progress)
    }

    fun drawMessageStatus(channel: GroupChannel, message: BaseMessage) {
        when (message.sendingStatus) {
            BaseMessage.SendingStatus.CANCELED, BaseMessage.SendingStatus.FAILED -> drawError()
            BaseMessage.SendingStatus.SUCCEEDED -> {
                val unreadMemberCount: Int = channel.getReadReceipt(message)
                val unDeliveredMemberCount: Int = channel.getDeliveryReceipt(message)
                if (unreadMemberCount == 0) {
                    drawRead()
                } else if (unDeliveredMemberCount == 0) {
                    drawDelivered()
                } else {
                    drawSent()
                }
            }
            else -> drawProgress()
        }
    }

    private fun drawError() {
        setProgress(false)
        messageStatus!!.setImageResource(R.drawable.icon_error_filled)
    }

    private fun drawRead() {
        setProgress(false)
        messageStatus!!.setImageResource(R.drawable.icon_read)
    }

    private fun drawSent() {
        setProgress(false)
        messageStatus!!.setImageResource(R.drawable.icon_sent)
    }

    private fun drawDelivered() {
        setProgress(false)
        messageStatus!!.setImageResource(R.drawable.icon_delivered)
    }

    private fun drawProgress() {
        setProgress(true)
    }

    private fun setProgress(isProgress: Boolean) {
        if (isProgress) {
            messageStatus!!.visibility = GONE
            progressBar!!.visibility = VISIBLE
        } else {
            progressBar!!.visibility = GONE
            messageStatus!!.visibility = VISIBLE
        }
    }
}