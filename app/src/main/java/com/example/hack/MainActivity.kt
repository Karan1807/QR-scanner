package com.example.hack


import android.Manifest
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    lateinit var btnScan: Button
    lateinit var res: TextView
    private val CAMERA_PERMISSION_CODE =123
    lateinit var inputImage: InputImage
    lateinit var scanner: BarcodeScanner

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan = findViewById(R.id.scanQR)
        res = findViewById(R.id.result)

        scanner = BarcodeScanning.getClient()

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object :ActivityResultCallback<ActivityResult>{
                override fun onActivityResult(result: ActivityResult?) {
                    val data = result?.data

                    try {
                        val photo = data?.extras?.get("data") as  Bitmap
                        inputImage = InputImage.fromBitmap(photo,0)
                        processQr()
                    }catch (e:Exception){
                        Log.d(TAG, "onActivityResult" +e.message)
                    }
                }

            }
        )

        btnScan.setOnClickListener {
        val options = arrayOf("camera")

            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("select")

            builder.setItems(options, DialogInterface.OnClickListener { dialog, which -> if(which==0){
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(cameraIntent)
            }else{
                val storageIntent = Intent()
                storageIntent.setType("image/*")
                storageIntent.setAction((Intent.ACTION_GET_CONTENT))

            }

            })
            builder.show()
        }

    }

    private fun processQr() {

        scanner.process(inputImage).addOnSuccessListener {
            for(barcode: Barcode in it ){
                val valueType = barcode.valueType
                    when(valueType){
                        Barcode.TYPE_TEXT ->{
                            val data = barcode.displayValue
                            res.text="${data}"
                        }

                    }
            }

        }.addOnFailureListener {
            Log.d(TAG,"processQr: ${it.message}")
        }
    }

    override fun onResume() {
        super.onResume()
         checkPermission(android.Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE)
    }



    private fun checkPermission(permission:String,requestCode:Int) {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission Granted Already", Toast.LENGTH_LONG)
                .show()
        }
    }
        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if(requestCode==CAMERA_PERMISSION_CODE){
                if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_LONG)
                        .show()
                }else{
                    Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
}
