package edu.iu.alex.selfieapp

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CameraFragment : Fragment() {

    private lateinit var viewModel: ImageViewModel
    private var imageCapture: ImageCapture? = null
    private lateinit var viewFinder: PreviewView
    private lateinit var takePictureButton: ImageButton
    private lateinit var homeButton: ImageButton

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ImageViewModel::class.java]

        viewFinder = view.findViewById(R.id.viewFinder)
        takePictureButton = view.findViewById(R.id.button_take_picture)
        homeButton = view.findViewById(R.id.button_home)

        requestCameraPermission()

        takePictureButton.setOnClickListener {
            Log.d(TAG, "Take picture button clicked")
            takePhoto()
        }
        homeButton.setOnClickListener{
            navigateToMainFragment()
            closeCamera()
            viewFinder.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closeCamera()
    }

    /*
    * Method used to capture the photo when called.
    *
    * @param ImageViewHolder,position
    *
     */

    private fun takePhoto() {
        Log.d(TAG, "takePhoto: Starting photo capture")
        val imageCapture = this.imageCapture ?: run {
            Log.e(TAG, "ImageCapture is not initialized")
            return
        }
        val photoFile = createTempFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Image saved successfully!")
                    uploadImageToFirebase(photoFile)
                }
            }
        )
        Log.d(TAG, "takePhoto: Photo capture completed")
    }

    /*
    * Called when necessary permission to access the camera is granted.
    *
    *
    *
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /*
    * Called when app shuts down and navigates away from fragment.
    *
     */
    private fun closeCamera() {
        val cameraProvider: ProcessCameraProvider =
            ProcessCameraProvider.getInstance(requireContext()).get()
        cameraProvider.unbindAll()
    }

    /*
    * Takes in a file and uploads the url to that file to Firebase Storage.
    *
    * @param file
    *
     */

    private fun uploadImageToFirebase(file: File) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
        val uniqueImageName = "image_$userId" + "_$timeStamp.jpg"

        val storageReference =
            FirebaseStorage.getInstance().getReference("images/$userId/$uniqueImageName")

        // Upload file to Firebase Storage
        storageReference.putFile(Uri.fromFile(file))
            .addOnSuccessListener {
                Log.d(TAG, "Image uploaded to Firebase Storage successfully")
                closeCamera()
                Log.d(TAG, "Navigating to MainFragment...")
                requireActivity().supportFragmentManager.popBackStack()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to upload image to Firebase Storage", exception)
            }
    }

    private fun navigateToMainFragment() {
        findNavController().navigate(R.id.cameraFragment_to_mainFragment)
    }

    /*
    * Method returns a file to be stored on the local machine in the picture directory.
    *
    *
    * @return file
     */

    private fun createTempFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val storageDir: File? =
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Log.d("Camera Fragment", "Temp file created at $timeStamp in $storageDir")
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }


    /*
    * Asks user for permission to use the camera.
    *
     */
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            startCamera()
        }
    }
}
