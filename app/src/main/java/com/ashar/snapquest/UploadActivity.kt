package com.ashar.snapquest

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UploadActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var etCaption: EditText
    private lateinit var btnUpload: Button
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        auth = FirebaseAuth.getInstance()
        ivPreview = findViewById(R.id.ivPreview)
        etCaption = findViewById(R.id.etCaption)
        btnUpload = findViewById(R.id.btnUpload)

        val uriString = intent.getStringExtra("imageUri")
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            Glide.with(this).load(imageUri).into(ivPreview)
        }

        btnUpload.setOnClickListener {
            val caption = etCaption.text.toString().trim()
            imageUri?.let { uploadPhoto(it, caption) }
        }
    }

    private fun uploadPhoto(uri: Uri, caption: String) {
        val userId = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        btnUpload.isEnabled = false
        btnUpload.text = "Uploading..."

        val storageRef = FirebaseStorage.getInstance().reference
            .child("posts/$userId/$today.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val ref = FirebaseDatabase.getInstance()
                        .getReference("posts").child(userId).child(today)
                    val postData = mapOf(
                        "imageUrl" to downloadUrl.toString(),
                        "timestamp" to System.currentTimeMillis(),
                        "userId" to userId,
                        "username" to (auth.currentUser?.displayName ?: auth.currentUser?.email ?: "user"),
                        "caption" to caption
                    )
                    ref.setValue(postData).addOnSuccessListener {
                        Toast.makeText(this, "Photo uploaded!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .addOnFailureListener {
                btnUpload.isEnabled = true
                btnUpload.text = "Upload Photo"
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}