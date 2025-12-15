package com.example.teshub_v1.ui.publicaciones

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teshub_v1.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var recyclerPdf: RecyclerView
    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        recyclerPdf = findViewById(R.id.recyclerPdf)
        recyclerPdf.layoutManager = LinearLayoutManager(this)

        val url = intent.getStringExtra("url") ?: return

        lifecycleScope.launch {
            val file = descargarPdf(url)
            mostrarTodasLasPaginas(file)
        }
    }

    private suspend fun descargarPdf(url: String): File = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection()
        val input = connection.getInputStream()
        val file = File(cacheDir, "temp.pdf")

        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
        file
    }

    private suspend fun mostrarTodasLasPaginas(file: File) = withContext(Dispatchers.IO) {
        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        renderer = PdfRenderer(fileDescriptor!!)

        val bitmaps = mutableListOf<Bitmap>()

        for (i in 0 until renderer!!.pageCount) {
            val page = renderer!!.openPage(i)

            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
            page.close()
        }

        withContext(Dispatchers.Main) {
            recyclerPdf.adapter = PdfAdapter(bitmaps)
        }
    }

    override fun onDestroy() {
        renderer?.close()
        fileDescriptor?.close()
        super.onDestroy()
    }
}
