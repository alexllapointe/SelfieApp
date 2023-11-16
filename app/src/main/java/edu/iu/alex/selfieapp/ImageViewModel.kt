package edu.iu.alex.selfieapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ImageViewModel : ViewModel() {

    private val storageRef = FirebaseStorage.getInstance().reference
    private val _imageUrls = MutableLiveData<List<String>>()
    val imageUrls: LiveData<List<String>> = _imageUrls

    private val _imageSaved = MutableLiveData<Boolean>()
    val imageSaved: LiveData<Boolean> = _imageSaved

    private val _openCamera = MutableLiveData<Boolean>()
    val openCamera: LiveData<Boolean> = _openCamera

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            fetchData()
        } else {
            _imageUrls.value = listOf()
        }
    }

    fun notifyImageSaved() {
        Log.d("ImageViewModel", "Image has been saved")
        _imageSaved.value = true
        _imageSaved.value = false
    }

    fun fetchData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("ImageViewModel", "User ID is null, cannot fetch data")
            _imageUrls.value = listOf()
            return
        }

        Log.d("ImageViewModel", "Fetching data for user: $userId")
        storageRef.child("images/$userId").listAll().addOnSuccessListener { listResult ->
            val urls = mutableListOf<String>()
            for (fileRef in listResult.items) {
                Log.d("ImageViewModel", "Found file reference: ${fileRef.path}")
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("ImageViewModel", "Download URL obtained: $uri")
                    urls.add(uri.toString())
                    _imageUrls.value = urls
                }.addOnFailureListener { exception ->
                    Log.e("ImageViewModel", "Error getting download URL", exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("ImageViewModel", "Error listing files", exception)
        }
    }
    fun clearData() {
        _imageUrls.value = listOf()
    }
}
