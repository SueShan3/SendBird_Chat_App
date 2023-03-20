package com.example.chatapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.chatapp.R
import com.example.chatapp.utils.TextUtils
import com.sendbird.android.*
import com.stfalcon.multiimageview.MultiImageView
import kotlinx.android.synthetic.main.item_channel_chooser.view.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/*** An Adapter to populate the RecyclerView with the returned channel list. ***/

class ChannelListAdapter(context: Context, listener: OnChannelClickedListener) :
    RecyclerView.Adapter<ChannelListAdapter.ChannelHolder>() {

    companion object {
        private var mSimpleTargetIndexMap: ConcurrentHashMap<SimpleTarget<Bitmap>, Int>? = null
        private var mSimpleTargetGroupChannelMap: ConcurrentHashMap<SimpleTarget<Bitmap>, GroupChannel>? =
            null
        private var mChannelImageNumMap: ConcurrentHashMap<String, Int>? = null
        private var mChannelImageViewMap: ConcurrentHashMap<String, ImageView>? = null
        private var mChannelBitmapMap: ConcurrentHashMap<String, SparseArray<Bitmap>>? = null
    }

    private var context: Context

    // adds an onClickListener to each view, launching the chat Activity for a respective channel.
    interface OnChannelClickedListener {
        fun onItemClicked(channel: GroupChannel)
    }

    private val listener: OnChannelClickedListener
    private var channels: MutableList<GroupChannel>

    init {
        channels = ArrayList()
        this.context = context
        this.listener = listener

        mSimpleTargetIndexMap = ConcurrentHashMap()
        mSimpleTargetGroupChannelMap = ConcurrentHashMap()
        mChannelImageNumMap = ConcurrentHashMap()
        mChannelImageViewMap = ConcurrentHashMap()
        mChannelBitmapMap = ConcurrentHashMap()
    }

    fun clearMap() {
        mSimpleTargetIndexMap!!.clear()
        mSimpleTargetGroupChannelMap!!.clear()
        mChannelImageNumMap!!.clear()
        mChannelImageViewMap!!.clear()
        mChannelBitmapMap!!.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addChannels(channels: MutableList<GroupChannel>) {
        this.channels = channels
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
        return ChannelHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_channel_chooser, parent, false)
        )
    }

    override fun getItemCount() = channels.size

    override fun onBindViewHolder(holder: ChannelHolder, position: Int) {
        holder.bindViews(context, channels[position], listener)
    }

    // Binds data to the corresponding views.
    class ChannelHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val channelImage = v.image_group_channel_list_cover
        private val channelName = v.text_channel_name
        private val channelRecentMessage = v.text_channel_recent
        private val channelMemberCount = v.text_channel_member_count
        private val channelUnreadCountText = v.text_group_channel_list_unread_count

        // Passed a groupChannel object and an OnChannelClickedListener to set up the views.
        fun bindViews(
            context: Context,
            groupChannel: GroupChannel,
            listener: OnChannelClickedListener
        ) {
            val lastMessage = groupChannel.lastMessage

            if (lastMessage != null) {

                when (lastMessage) {
                    is UserMessage -> channelRecentMessage.text = lastMessage.message
                    is AdminMessage -> channelRecentMessage.text = lastMessage.message
                    else -> {
                        val fileMessage = lastMessage as FileMessage
                        val sender = fileMessage.sender.nickname

                        channelRecentMessage.text = sender
                    }
                }
            }

            //Unread messages
            val unreadCount: Int = groupChannel.unreadMessageCount
            // If there are no unread messages, hide the unread count badge.
            if (unreadCount == 0) {
                channelUnreadCountText.visibility = View.INVISIBLE
            } else {
                channelUnreadCountText.visibility = View.VISIBLE
                channelUnreadCountText.text = unreadCount.toString()
            }

            // Set the onClickListener on the itemView to the listener passed in the bindViews method.
            itemView.setOnClickListener {
                listener.onItemClicked(groupChannel)
                groupChannel.markAsRead()
            }

            channelImage.shape = MultiImageView.Shape.CIRCLE
            setChannelImage(context, groupChannel, channelImage)

            channelName.text = TextUtils.getGroupChannelTitle(groupChannel)
            channelMemberCount.text = groupChannel.memberCount.toString()
        }

        private fun setChannelImage(
            context: Context,
            channel: GroupChannel,
            multiImageView: MultiImageView
        ) {
            val members: List<Member> = channel.members
            val size = members.size
            if (size >= 1) {
                var imageNum = size
                if (size >= 4) {
                    imageNum = 4
                }

                if (!mChannelImageNumMap?.containsKey(channel.url)!!) {
                    mChannelImageNumMap?.put(channel.url, imageNum)
                    mChannelImageViewMap?.put(channel.url, multiImageView)

                    multiImageView.clear()

                    for (index in 0 until imageNum) {
                        val simpleTarget: SimpleTarget<Bitmap> = object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                bitmap: Bitmap,
                                transition: Transition<in Bitmap?>?
                            ) {
                                val channel = mSimpleTargetGroupChannelMap!![this]
                                val index = mSimpleTargetIndexMap!![this]
                                if (channel != null && index != null) {
                                    var bitmapSparseArray: SparseArray<Bitmap>? =
                                        mChannelBitmapMap?.get(channel.url)
                                    if (bitmapSparseArray == null) {
                                        bitmapSparseArray = SparseArray()
                                        mChannelBitmapMap?.put(channel.url, bitmapSparseArray)
                                    }
                                    bitmapSparseArray.put(index, bitmap)
                                    val num = mChannelImageNumMap!![channel.url]
                                    if (num != null && num == bitmapSparseArray.size()) {
                                        val multiImageView =
                                            mChannelImageViewMap!![channel.url] as MultiImageView?
                                        if (multiImageView != null) {
                                            for (i in 0 until bitmapSparseArray.size()) {
                                                multiImageView.addImage(bitmapSparseArray[i])
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        mSimpleTargetIndexMap?.put(simpleTarget, index)
                        mSimpleTargetIndexMap?.put(simpleTarget, index)
                        mSimpleTargetGroupChannelMap?.put(simpleTarget, channel)
                        val myOptions = RequestOptions()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        Glide.with(context)
                            .asBitmap()
                            .load(members[index].profileUrl)
                            .apply(myOptions)
                            .into(simpleTarget)
                    }
                } else {
                    val bitmapSparseArray: SparseArray<Bitmap> =
                        mChannelBitmapMap?.get(channel.url)!!
                    val num = mChannelImageNumMap!![channel.url]
                    if (num != null && num == bitmapSparseArray.size()) {
                        multiImageView.clear()
                        for (i in 0 until bitmapSparseArray.size()) {
                            multiImageView.addImage(bitmapSparseArray[i])
                        }
                    }
                }
            }
        }
    }
}