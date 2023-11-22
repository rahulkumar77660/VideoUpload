package com.pupup.videoupload

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.firestore.FirebaseFirestore
interface VideoClickListener {
    fun onDeleteClicked(position: String, position1: Int)
    fun onUpdateClicked(position: String, position1: Int)
    // You can add more methods for other actions if needed
}
class MyAdapter(val context: Context,val listItem : ArrayList<User>,val listener: VideoClickListener) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    private var exoPlayer: SimpleExoPlayer? = null
    val db = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item,parent,false)
        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = listItem[position]

        // Initialize ExoPlayer
        exoPlayer = SimpleExoPlayer.Builder(context).build()
        exoPlayer?.playWhenReady = false
        holder.playerView.player = exoPlayer

        // Set video URL to Player
        val mediaItem: MediaItem = MediaItem.fromUri(Uri.parse(user.url))
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        holder.playerView.setOnClickListener {
            if (exoPlayer!!.isPlaying){
                exoPlayer?.pause()
            }
            else{
                exoPlayer?.play()
            }
        }
        holder.textView.text = user.text
        holder.moreVert.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.menu_item) // Create a menu xml (e.g., your_context_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delete -> {
                        user.id.let { listener.onDeleteClicked(it!!,position) }
                        true
                    }
                    R.id.update -> {
                        user.id.let { listener.onUpdateClicked(it!!,position) }
                        true
                    }
                    else -> true
                }
            }
            popupMenu.show()
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val playerView : PlayerView = itemView.findViewById(R.id.rcy_videoView)
        val textView : TextView = itemView.findViewById(R.id.textView)
        val moreVert : ImageView = itemView.findViewById(R.id.moreVert)
    }

}