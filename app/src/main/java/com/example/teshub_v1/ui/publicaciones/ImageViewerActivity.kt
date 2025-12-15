package com.example.teshub_v1.ui.publicaciones
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.teshub_v1.R
class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val url = intent.getStringExtra("url") ?: return

        Glide.with(this)
            .load(url)
            .into(imageView)
    }
}
