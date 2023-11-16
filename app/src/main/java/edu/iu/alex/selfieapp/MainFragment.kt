package edu.iu.alex.selfieapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment(), SensorEventListener {

    private lateinit var viewModel: ImageViewModel
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var loginButton: Button
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var logoutButton: Button


    // Shake detection parameters
    private val SHAKE_THRESHOLD = 800
    private var lastUpdate: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]

        imageRecyclerView = view.findViewById(R.id.image_recycler_view)
        loginButton = view.findViewById(R.id.button_login)

        val mainActivity = activity as? MainActivity
        if (mainActivity?.isUserLoggedIn == true) {
            loginButton.visibility = View.GONE
        } else {
            loginButton.visibility = View.VISIBLE
            loginButton.setOnClickListener {
                navigateToLoginFragment()
            }
        }

        imageAdapter = ImageAdapter(requireContext())
        imageRecyclerView.adapter = imageAdapter
        imageRecyclerView.layoutManager = LinearLayoutManager(context)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        viewModel.imageUrls.observe(viewLifecycleOwner) { urls ->
            imageAdapter.setImageUrls(urls)
            imageRecyclerView.visibility = if (urls.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.imageSaved.observe(viewLifecycleOwner) { isSaved ->
            if (isSaved) {
                viewModel.fetchData()
            }
        }

    }

    private fun navigateToLoginFragment() {
        val navController = findNavController()
        navController.navigate(R.id.mainFragment_to_loginFragment)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            viewModel.clearData()
            imageRecyclerView.visibility = View.GONE
        } else {
            viewModel.fetchData()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        viewModel.fetchData()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastUpdate) > 100) {
            val diffTime = currentTime - lastUpdate
            lastUpdate = currentTime

            val x = event?.values?.get(0) ?: 0f
            val y = event?.values?.get(1) ?: 0f
            val z = event?.values?.get(2) ?: 0f

            val speed = ((x + y + z - lastX - lastY - lastZ) / diffTime) * 10000

            if (speed > SHAKE_THRESHOLD) {
                Log.d("Main Fragment", "Shake detected, opening camera.")
                openCameraFragment()
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //not used
    }


    private fun openCameraFragment() {
        findNavController().navigate(R.id.mainFragment_to_cameraFragment)
    }
}
