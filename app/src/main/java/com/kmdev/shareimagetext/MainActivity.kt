package com.kmdev.shareimagetext

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var btnShare: TextView
    val PERMISSION_REQUEST_CODE = 123
    private var preloadedBitmap: Bitmap? = null // Variable to hold the preloaded bitmap

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views after setContentView
        imageView = findViewById(R.id.image_show_imv)
        btnShare = findViewById(R.id.btn_share)


        // Check for the READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, proceed with your logic
            setShareButtonListener()
        } else {
            // Permission not granted, request it
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ), PERMISSION_REQUEST_CODE
            )
            setShareButtonListener()
        }
    }

    private fun setShareButtonListener() {
        downloadImageAndShare()
        btnShare.setOnClickListener {
            // Fire async request to download image
//            downloadImageAndShare()
            if (preloadedBitmap != null) {
                // Preloaded image is available, share it
                shareImage(preloadedBitmap!!)
            } else {
                // Preloaded image is not available, download and share
                downloadImageAndShare()
            }
        }
    }

    private fun downloadImageAndShare() {
        val imageUrl =
            "https://cdn2.thecatapi.com/images/MTgyOTU2Mw.jpg" // URL of the image to download

        // You can use any image downloading library here (e.g., Glide, Picasso, etc.)
        // Here, I'm using Glide for simplicity
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // Image downloaded successfully, now share it
//                    shareImage(resource)
                    preloadedBitmap = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Do nothing here
                }
            })
    }

    private fun shareImage(bitmap: Bitmap) {
        // Get access to the URI for the bitmap
        val bmpUri = getBitmapUriFromBitmap(this, bitmap)
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            val whatsappNumber = "8840237528" // Replace with the actual WhatsApp number

            val sendIntent = Intent("android.intent.action.Main").apply {
                putExtra(Intent.EXTRA_STREAM, bmpUri)
                putExtra("jid", "91$whatsappNumber@s.whatsapp.net")
                putExtra(Intent.EXTRA_TEXT, "mk whatsappmessage")
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                `package` = "com.whatsapp"
                type = "image/*"
            }
            startActivity(sendIntent)
        } else {
            Toast.makeText(this, "Failed to share image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBitmapUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_${System.currentTimeMillis()}.png"
            )
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()

            // wrap File object into a content provider.
            bmpUri = FileProvider.getUriForFile(context, "com.kmdev.shareimagetext.provider", file)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }


    fun onShareItem(ivImage: ImageView) {
        // Get access to bitmap image from view
        // Get access to the URI for the bitmap
        val bmpUri = getLocalBitmapUri(this, ivImage)
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            val whatsappNumber = "8840237528" // Replace with the actual WhatsApp number

            val sendIntent = Intent("android.intent.action.Main").apply {
                putExtra(Intent.EXTRA_STREAM, bmpUri)
                putExtra("jid", "91$whatsappNumber@s.whatsapp.net")
                putExtra(Intent.EXTRA_TEXT, "mk whatsappmessage")
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                `package` = "com.whatsapp"
//                type = "text/plain"
                type = "image/*"
            }
            startActivity(sendIntent)
        } else {
            Toast.makeText(this, "bmuri us null sharing failed", Toast.LENGTH_SHORT).show()
        }
    }


    // Returns the URI path to the Bitmap displayed in specified ImageView
    fun getLocalBitmapUri(context: Context, imageView: ImageView): Uri? {
        // Extract Bitmap from ImageView drawable
        val drawable: Drawable? = imageView.drawable
        var bmp: Bitmap? = null
        if (drawable is BitmapDrawable) {
            bmp = drawable.bitmap
        } else {
            return null
        }
        // Store image to default external storage directory
        var bmpUri: Uri? = null
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
//            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_${System.currentTimeMillis()}.png")
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".png"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            // **Warning:** This will fail for API >= 24, use a FileProvider as shown below instead.
//            bmpUri = Uri.fromFile(file)
            // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
            // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths


// wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration

// wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            bmpUri = FileProvider.getUriForFile(this, "com.kmdev.shareimagetext.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    private fun sendToWhatsApp1() {

        try {


            val whatsappNumber = "8840237528" // Replace with the actual WhatsApp number


            val sendIntent = Intent("android.intent.action.Main").apply {
//                    putExtra(Intent.EXTRA_STREAM, imageFile)
                putExtra("jid", "91$whatsappNumber@s.whatsapp.net")
                putExtra(Intent.EXTRA_TEXT, "mk whatsappmessage")
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                `package` = "com.whatsapp"
                type = "image/*"
            }

            startActivity(sendIntent)
//            } else {
//                Toast.makeText(this, "File path not exist", Toast.LENGTH_SHORT).show()
//            }
        } catch (t: Exception) {
            t.printStackTrace()
        }
    }


    private fun sendToWhatsApp() {

        try {


            val whatsappNumber = "8840237528" // Replace with the actual WhatsApp number

//        val drawableResource = R.drawable.outline_attach_file_24
//        val imageUri = Uri.parse("android.resource://${packageName}/$drawableResource")

            // Load and display the image in the ImageView
//        loadAndDisplayImage(imageUri, imageView)

            val imageUri: Uri? = FileProvider.getUriForFile(
                this, BuildConfig.APPLICATION_ID + ".provider", File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "/profile.png"
                )
            )

//            val imageFile = File("/storage/emulated/0/Download/webp.jpeg")
//            val imageFile = File(getExternalFilesDir(null), "webp.jpeg")
//            val imageFile = File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                "webp.jpeg"
//            )
//            val drawable =
//                ContextCompat.getDrawable(this, R.drawable.outline_attach_file_24)?.toBitmap()
//            val drawable =
//                ContextCompat.getDrawable(applicationContext, R.drawable.outline_attach_file_24)
//            val bm: Bitmap? = drawable?.toBitmap()

            val drawable =
                ContextCompat.getDrawable(applicationContext, R.drawable.outline_attach_file_24)
            val filesDir: File = applicationContext.filesDir
            val imageFile = File(filesDir, "ABeautifulFilename.png")

            if (drawable is VectorDrawableCompat) {
                try {
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)

                    // Set the bounds of the drawable without adjusting the canvas size
                    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                    drawable.draw(canvas)

                    val os: OutputStream = FileOutputStream(imageFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os) // 100% quality
                    os.flush()
                    os.close()
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error writing vector drawable to bitmap", e)
                }
            } else {
                Log.e(javaClass.simpleName, "Drawable is not a VectorDrawableCompat")
            }



            if (imageFile.exists()) {
                val fileUri = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider", imageFile
                )

                val sendIntent = Intent("android.intent.action.Main").apply {
                    putExtra(Intent.EXTRA_STREAM, imageFile)
                    putExtra("jid", "91$whatsappNumber@s.whatsapp.net")
                    putExtra(Intent.EXTRA_TEXT, "mk whatsappmessage")
                    action = Intent.ACTION_SEND
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    `package` = "com.whatsapp"
                    type = "image/*"
                }

                startActivity(sendIntent)
            } else {
                Toast.makeText(this, "File path not exist", Toast.LENGTH_SHORT).show()
            }
        } catch (t: Exception) {
            t.printStackTrace()
        }
    }


//    fun getDrawableUri(drawableId: Int): Uri? {
//        val context = this.applicationContext
//        val resourceUri = Uri.parse("android.resource://${packageName}/$drawableId")
//        val file = resourceUri.path?.let { File(it) }
//
//        return file?.let { FileProvider.getUriForFile(context, "${packageName}.provider", it) }
//    }

//        private fun getLocalBitmapUri(drawable: Drawable): Uri {
//            val bmp = (drawable as BitmapDrawable).bitmap
//            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "share_image.png")
//            try {
//                val out = FileOutputStream(file)
//                bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
//                out.close()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//            return FileProvider.getUriForFile(this, "${packageName}.provider", file)
//        }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your logic
                    btnShare.setOnClickListener {
                        sendToWhatsApp()
                    }
                } else {
                    // Permission denied, handle accordingly (e.g., show a message, disable functionality)
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


//    private fun loadAndDisplayImage(imageUri: Uri, imageView: ImageView) {
//        try {
//            val inputStream = contentResolver.openInputStream(imageUri)
//            if (inputStream != null) {
//                val bitmap = BitmapFactory.decodeStream(inputStream)
//                imageView.setImageBitmap(bitmap)
//            } else {
//                // Handle the case where inputStream is null (e.g., image not found)
//                // You might want to set a placeholder image or show an error message.
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//            // Handle the exception, such as displaying an error message
//        }
//    }

}
