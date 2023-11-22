package com.pupup.videoupload

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Video
import android.widget.Button
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class UpdateActivity : AppCompatActivity() {
    lateinit var  videoView : VideoView
    lateinit var db : FirebaseFirestore
    var intentId:String?=null
    lateinit var myAdapter: MyAdapter
    lateinit var listItem : ArrayList<User>

    @SuppressLint("MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

            videoView  = findViewById(R.id.update_videoView)
        db = FirebaseFirestore.getInstance()
        val intentvdo = intent.getStringExtra("videoUrl")
        intentId = intent.getStringExtra("videoId")
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        val videoUrl = Uri.parse(intentvdo)
        videoView.setVideoURI(videoUrl)
        val updateBtn : Button = findViewById(R.id.updateBtn)
        listItem= arrayListOf()

        val chooseVie0: Button = findViewById(R.id.update_chooseBtn)
        chooseVie0.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type="video/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent,"Pick Video"),100)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==100 && resultCode == Activity.RESULT_OK)
        {
            val videoUrl = data?.data
            videoView.setVideoURI(videoUrl)
            uploadToFirebaseStorage(videoUrl!!)
            myAdapter.notifyDataSetChanged()
        }
    }

    fun uploadToFirebaseStorage(fileUri : Uri){
        val storageRef = Firebase.storage.reference
        val fileName = "videos/${System.currentTimeMillis()}" // Unique filename

        val uploadTask = storageRef.child(fileName).putFile(fileUri)
            .addOnSuccessListener {
                // Upload succeeded
                it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()
                    // Use downloadUrl as needed (e.g., store it in Firestore, display it, etc.)
                    saveVideoUrlToFirestore(downloadUrl)

                    Toast.makeText(this, "Update successful! Download URL: $downloadUrl", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    // Handle failure to get download URL
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                // Handle unsuccessful uploads
                Toast.makeText(this, "Upload failed: $it", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveVideoUrlToFirestore(videoUrl: String) {
        db = FirebaseFirestore.getInstance()
        db.collection("videos").document(intentId!!)
            .update("url",videoUrl)
            .addOnSuccessListener {
                // Document added successfully
                Toast.makeText(this, "Video URL Update to Firestore!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle unsuccessful addition
                Toast.makeText(this, "Failed to add video URL to Firestore", Toast.LENGTH_SHORT).show()
            }
    }
}