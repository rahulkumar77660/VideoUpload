package com.pupup.videoupload

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class MainActivity : AppCompatActivity() {
    var a = 1
    lateinit var videoView : VideoView
    var  videoUri: Uri? = null
    private lateinit var gestureDetector: GestureDetector
    lateinit var db : FirebaseFirestore
    lateinit var progressDialog : ProgressDialog
    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val chooseBtn: Button = findViewById(R.id.chooseBtn)
         videoView  = findViewById(R.id.videoView)
        val nextActiviyt: Button = findViewById(R.id.nextActivity)
        progressDialog = ProgressDialog(this@MainActivity)
        nextActiviyt.setOnClickListener{
            startActivity(Intent(applicationContext,VideoActivity::class.java))
        }
        chooseBtn.setOnClickListener {
            openFilePicker()
        }
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.stopPlayback()

        gestureDetector = GestureDetector(this,object :GestureDetector.SimpleOnGestureListener(){
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (videoView.isPlaying) {
                    videoView.pause()
                } else {
                    videoView.start()
                }
                return true
            }
            override fun onLongPress(e: MotionEvent) {
                stopVideo()
            }
        })
        videoView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

    }
    private fun stopVideo() {
        videoView.stopPlayback()
    }
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "video/*" // Specify the MIME type for videos
        startActivityForResult(Intent.createChooser(intent,"Pick Video"),100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100){
            if (resultCode == Activity.RESULT_OK){
                progressDialog.setTitle("Video Upload")
                progressDialog.show()
                 videoUri = data?.data
                videoView.setVideoURI(videoUri)
                    videoView.pause()
                uploadToFirebaseStorage(videoUri!!)
            }
        }
    }
    private fun uploadToFirebaseStorage(fileUri: Uri) {
        val storageRef = Firebase.storage.reference
        val fileName = "videos/${System.currentTimeMillis()}" // Unique filename

        val uploadTask = storageRef.child(fileName).putFile(fileUri)

            .addOnSuccessListener {
            // Upload succeeded
                progressDialog.dismiss()
                it.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()
                    // Use downloadUrl as needed (e.g., store it in Firestore, display it, etc.)
                    saveVideoUrlToFirestore(downloadUrl)
                    Toast.makeText(this, "Upload successful! Download URL: $downloadUrl", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    // Handle failure to get download URL
                    Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()


            }.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this, "Upload failed: $it", Toast.LENGTH_SHORT).show()
        }
            .addOnProgressListener {
                val value = (it.bytesTransferred/it.totalByteCount)*100
                progressDialog.setTitle("Uploaded" + value + "%")
            }
    }

    private fun saveVideoUrlToFirestore(videoUrl: String) {
        db = FirebaseFirestore.getInstance()
        val randomId = UUID.randomUUID().toString()
        val videoData = hashMapOf(
            "url" to videoUrl,
            "id" to randomId,
            "text" to "Video $a"
            // Add other fields as needed
        )
        a++
        db.collection("videos").document(randomId)
            .set(videoData)
            .addOnSuccessListener {
                // Document added successfully
                Toast.makeText(this, "Video URL added to Firestore!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle unsuccessful addition
                Toast.makeText(this, "Failed to add video URL to Firestore", Toast.LENGTH_SHORT).show()
            }
    }
}