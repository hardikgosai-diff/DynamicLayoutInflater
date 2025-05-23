package com.dynamiclayoutinflater

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.ImageFormat
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dynamiclayoutinflater.databinding.ActivityMainBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initCamera()
        initDynamicLayout("12")
        Handler().postDelayed({

        }, 3000)
    }

    private fun initCamera() {
        binding.cameraView.setLifecycleOwner(this)
        binding.cameraView.addFrameProcessor(object : FrameProcessor {
            private var lastTime = System.currentTimeMillis()
            override fun process(frame: Frame) {
                val newTime = frame.time
                lastTime = newTime

                if (frame.format == ImageFormat.NV21 && frame.dataClass == ByteArray::class.java) {
                    val data = frame.getData<ByteArray>()

                    val inputImage = InputImage.fromByteArray(
                        data, frame.size.width,  // Frame width
                        frame.size.height, // Frame height
                        frame.rotationToUser, // Frame rotation (0, 90, 180, 270)
                        InputImage.IMAGE_FORMAT_NV21 // Specify the format (NV21 in this case)
                    )

                    detectQrCode(inputImage)
                }
            }
        })
    }

    private fun initDynamicLayout(folder: String) {
        try {
            val view =
                DynamicLayoutInflater.inflate(this, assets.open("$folder/layout.xml"), binding.rl)
            DynamicLayoutInflater.setDelegate(view, this)

            val ivMain = DynamicLayoutInflater.findViewByIdString(view, "ivMain")
            val tvLatitude = DynamicLayoutInflater.findViewByIdString(view, "tvLatitude")
            val tvLongitude = DynamicLayoutInflater.findViewByIdString(view, "tvLongitude")
            val ivSmall = DynamicLayoutInflater.findViewByIdString(view, "ivSmall")
            val tvDate = DynamicLayoutInflater.findViewByIdString(view, "tvDate")
            val tvTime = DynamicLayoutInflater.findViewByIdString(view, "tvTime")
            val tvDegree = DynamicLayoutInflater.findViewByIdString(view, "tvDegree")
            val tvAddress = DynamicLayoutInflater.findViewByIdString(view, "tvAddress")
            val tvDateTime = DynamicLayoutInflater.findViewByIdString(view, "tvDateTime")
            val ivText = DynamicLayoutInflater.findViewByIdString(view, "ivText")
            val ivMarker = DynamicLayoutInflater.findViewByIdString(view, "ivMarker")
            val divider = DynamicLayoutInflater.findViewByIdString(view, "divider")
            val ivBg = DynamicLayoutInflater.findViewByIdString(view, "ivBg")

            if (ivMain is ImageView) {
                val bitmap = BitmapFactory.decodeStream(assets.open("$folder/main.png"))
                ivMain.setImageBitmap(bitmap)
            }
            if (ivSmall is ImageView) {
                val bitmap = BitmapFactory.decodeStream(assets.open("$folder/small.png"))
                ivSmall.setImageBitmap(bitmap)
            }
            if (tvLatitude is TextView) {
                tvLatitude.apply {
                    text = "20.59370556466"
                    if (tag != null) {
                        typeface = Typeface.createFromAsset(assets, "$folder/${tag}")
                    }
                }
            }
            if (tvLongitude is TextView) {
                tvLongitude.apply {
                    text = "78.9629464648"
                    if (tag != null) {
                        typeface = Typeface.createFromAsset(assets, "$folder/${tag}")
                    }
                }
            }
            if (tvDate is TextView) {
                tvDate.apply {
                    text = "Wed, 21th May 2025"
                    if (tag != null) {
                        typeface = Typeface.createFromAsset(assets, "$folder/${tag}")
                    }
                }
            }
            if (tvTime is TextView) {
                tvTime.apply {
                    text = "06:50 PM"
                    if (tag != null) {
                        typeface = Typeface.createFromAsset(assets, "$folder/${tag}")
                    }
                }
            }
            if (tvDegree is TextView) {
                tvDegree.apply {
                    text = "10 C"
                    if (tag != null) {
                        typeface = Typeface.createFromAsset(assets, "$folder/${tag}")
                    }
                }
            }
            if (tvAddress is TextView) {
                tvAddress.apply {
                    text = "Surat, Gujarat, India"
                    if (tag != null) {
                        typeface = Typeface.createFromAsset(assets, "$folder/${tag}")
                    }
                }
            }
            if (tvDateTime is TextView) {
                tvDateTime.text = "Wed, 21th May 2025 08:50 PM"
                if (tvDateTime.tag != null) {
                    val bitmap = BitmapFactory.decodeStream(assets.open("$folder/date_time_bg.png"))
                    tvDateTime.background = BitmapDrawable(resources, bitmap)
                }
            }
            if (ivText is ImageView) {
                val bitmap = BitmapFactory.decodeStream(assets.open("$folder/text.png"))
                ivText.setImageBitmap(bitmap)
            }
            if (ivMarker is ImageView) {
                val bitmap = BitmapFactory.decodeStream(assets.open("$folder/marker.png"))
                ivMarker.setImageBitmap(bitmap)
            }
            if (divider is FrameLayout) {
                val bitmap = BitmapFactory.decodeStream(assets.open("$folder/divider.png"))
                divider.background = BitmapDrawable(resources, bitmap)
            }
            if (ivBg is ImageView) {
                ivBg.apply {
                    if (tag != null) {
                        val bitmap = BitmapFactory.decodeStream(assets.open("$folder/$tag"))
                        ivBg.background = BitmapDrawable(resources, bitmap)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun detectQrCode(inputImage: InputImage) {
        val scanner = BarcodeScanning.getClient()

        scanner.process(inputImage).addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                val barcode = barcodes[0]
                val qrValue = barcode.rawValue
                println(qrValue)
            }
        }.addOnFailureListener { e -> e.printStackTrace() }
    }

    private fun setTextToBlur(tv: TextView, radius: Float) {
        tv.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val filter = BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER)
        tv.paint.maskFilter = filter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray, deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        val valid = grantResults.all { it == PERMISSION_GRANTED }
        if (valid && !binding.cameraView.isOpened) {
            binding.cameraView.open()
        }
    }
}