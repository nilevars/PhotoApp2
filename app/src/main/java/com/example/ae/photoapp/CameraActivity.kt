package com.example.ae.photoapp

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.graphics.SurfaceTexture
import android.view.TextureView
import android.content.Context.CAMERA_SERVICE
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Size
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.os.Handler
import java.util.Collections.singletonList
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.Surface
import com.example.ae.photoapp.R.id.texture_view
import kotlinx.android.synthetic.main.activity_camera.*
import java.util.*
import android.hardware.camera2.CameraAccessException
import android.os.HandlerThread
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore.Images.Media.getBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat


class CameraActivity : AppCompatActivity() {

    lateinit var cameraManager : CameraManager
    var cameraFacing : Int = 0
    lateinit var surfaceTextureListener : TextureView.SurfaceTextureListener
    val CAMERA_REQUEST_CODE=10
    var cameraId : String = ""
    lateinit var previewSize : Size
    lateinit var stateCallback : CameraDevice.StateCallback
    var cameraDevice : CameraDevice? = null
    var backgroundHandler : Handler? = null
    var cameraCaptureSession : CameraCaptureSession? = null
    lateinit var captureRequestBuilder : CaptureRequest.Builder
    var backgroundThread : HandlerThread? = null

    var username : String = ""
    var requestId : String = ""
    var userLat : Double = 0.0
    var userLng : Double = 0.0

    /*On create to initialize camera manager, camera facing surface texture listener and state callbacks*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        username=intent.getStringExtra("username")
        requestId=intent.getStringExtra("requestId")
        userLat=intent.getDoubleExtra("userLat",0.0)
        userLng=intent.getDoubleExtra("userLng",0.0)
        close.setOnClickListener {
            onBackPressed()
        }
        reverseCam.setOnClickListener {
            var a = AnimationUtils.loadAnimation(this@CameraActivity,R.anim.rotate);
            a.setDuration(3000);
            reverseCam.startAnimation(a);
            Log.i("Info","Reverse Click")
            closeCamera()
            val rotate = RotateAnimation(0f, 360f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f)
            rotate.duration = 4000
            reverseCam.visibility=GONE

            reverseCam.setAnimation(rotate)
            if(cameraFacing==CameraCharacteristics.LENS_FACING_FRONT)
            {
                cameraFacing = CameraCharacteristics.LENS_FACING_BACK
            }
            else
            {
                cameraFacing = CameraCharacteristics.LENS_FACING_FRONT
            }
            setUpCamera()
            openCamera()
            reverseCam.visibility= VISIBLE

        }
        ActivityCompat.requestPermissions(this, arrayOf<String>(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK

        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                setUpCamera()
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {

            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

            }
        }
        stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                this@CameraActivity.cameraDevice = cameraDevice
                createPreviewSession()
            }

            override fun onDisconnected(cameraDevice: CameraDevice) {
                cameraDevice.close()
                this@CameraActivity.cameraDevice = null
            }

            override fun onError(cameraDevice: CameraDevice, error: Int) {
                cameraDevice.close()
                this@CameraActivity.cameraDevice = null
            }
        }
        fab_take_photo.setOnClickListener {
            var bmp : Bitmap? = null
            Log.i("info","Clicked fab")
            lock()
            try {
                bmp = texture_view.getBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                unlock()

                try {
                    if(bmp != null)
                    {
                        try {
                            val intent= Intent(applicationContext,PhotoViewActivity::class.java)
                            intent.putExtra("filename",createImageFromBitmap(bmp))
                            intent.putExtra("username",username)
                            intent.putExtra("requestId",requestId)
                            intent.putExtra("userLat",userLat)
                            intent.putExtra("userLng",userLng)
                            startActivity(intent)
                        }catch (e : Exception)
                        {
                            Log.e("Exception",e.message)
                            Log.e("Exception",e.cause.toString())
                        }

                    }
                    else
                    {
                        Log.i("info","Bmp is null")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun createImageFromBitmap(bitmap: Bitmap): String? {
        var fileName: String? = "myImage"//no .png or .jpg needed
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val fo = openFileOutput(fileName, Context.MODE_PRIVATE)
            fo.write(bytes.toByteArray())
            // remember close file output
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
            fileName = null
        }

        return fileName
    }
    private fun setUpCamera() {
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) === cameraFacing) {
                    val streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    previewSize = streamConfigurationMap!!.getOutputSizes(SurfaceTexture::class.java)[0]
                    this.cameraId = cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun createPreviewSession() {
        try {
            val surfaceTexture = texture_view.surfaceTexture
            surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
            val previewSurface = Surface(surfaceTexture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(previewSurface)

            cameraDevice?.createCaptureSession(Collections.singletonList(previewSurface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            if (cameraDevice == null) {
                                return
                            }

                            try {
                                var captureRequest = captureRequestBuilder?.build()
                                this@CameraActivity.cameraCaptureSession = cameraCaptureSession
                                this@CameraActivity.cameraCaptureSession?.setRepeatingRequest(captureRequest, null, backgroundHandler)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }

                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {

                        }
                    }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        openBackgroundThread()
        if (texture_view.isAvailable()) {
            setUpCamera()
            openCamera()
        } else {
            texture_view.setSurfaceTextureListener(surfaceTextureListener)
        }
    }

    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("camera_background_thread")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.getLooper())
    }

    override fun onStop() {
        super.onStop()
        closeCamera()
        closeBackgroundThread()
    }

    private fun closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession?.close()
            cameraCaptureSession = null
        }

        if (cameraDevice != null) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread?.quitSafely()
            backgroundThread = null
            backgroundHandler = null
        }
    }

    private fun lock() {
        try {
            cameraCaptureSession?.capture(captureRequestBuilder?.build(),
                    null, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun unlock() {
        try {
            cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder?.build(),
                    null, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }
}
