package com.ashar.snapquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.work.*
import java.util.concurrent.TimeUnit
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private fun scheduleDailyReminder() {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (currentTime.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = targetTime.timeInMillis - currentTime.timeInMillis

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    private lateinit var auth: FirebaseAuth

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val intent = Intent(this, UploadActivity::class.java)
            intent.putExtra("imageUri", uri.toString())
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        scheduleDailyReminder()

        auth = FirebaseAuth.getInstance()

        val btnUploadPhoto = findViewById<android.widget.LinearLayout>(R.id.btnUploadPhoto)
        val btnLogout = findViewById<android.widget.LinearLayout>(R.id.btnLogout)
        val btnFeed = findViewById<android.widget.LinearLayout>(R.id.btnFeed)
        val btnProfile = findViewById<android.widget.LinearLayout>(R.id.btnProfile)
        val btnTrending = findViewById<android.widget.LinearLayout>(R.id.btnTrending)
        btnTrending.setOnClickListener {
            startActivity(Intent(this, TrendingActivity::class.java))
        }
        val btnSearch = findViewById<android.widget.EditText>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        btnUploadPhoto.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val ref = FirebaseDatabase.getInstance().getReference("posts").child(userId).child(today)

            ref.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(this, "You have already uploaded a photo today!", Toast.LENGTH_LONG).show()
                } else {
                    pickImage.launch("image/*")
                }
            }
        }

        btnFeed.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleClient = GoogleSignIn.getClient(this, gso)
            googleClient.signOut().addOnCompleteListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}