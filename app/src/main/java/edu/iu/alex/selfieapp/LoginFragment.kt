package edu.iu.alex.selfieapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(){


    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.login_fragment, container, false)
        auth = FirebaseAuth.getInstance()

        emailEditText = view.findViewById(R.id.email)
        passwordEditText = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.btn_login)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to Register Screen if "Don't have an account?" is clicked
        val registerTextView: TextView = view.findViewById(R.id.text_register)
        registerTextView.setOnClickListener {
            findNavController().navigate(R.id.loginFragment_to_registerFragment)
        }
        return view
    }

    /**
     * Method used to authentication user's email and password with firebase.
     *
     *
     * @param email string
     * @param password string
     */

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Welcome, $email.", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.isUserLoggedIn = true
                    findNavController().navigate(R.id.loginFragment_to_mainFragment)
                } else {
                    // Login failed
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

}