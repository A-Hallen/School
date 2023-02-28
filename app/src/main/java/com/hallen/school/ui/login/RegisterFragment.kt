package com.hallen.school.ui.login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hallen.school.R
import com.hallen.school.ResultActivity
import com.hallen.school.databinding.FragmentRegisterBinding
import com.hallen.school.model.OneTap

class RegisterFragment(private val parentView: View, private val loginFragment: LoginFragment) : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val text = "Olvidé mi contraseña"; val regText = "Login"
        val spannableString = SpannableString(text).apply { setSpan(UnderlineSpan(), 0, length, 0)  }
        val spannableString2 = SpannableString(regText).apply { setSpan(UnderlineSpan(), 0, length, 0)  }
        binding.alreadyAccount.text = spannableString2; binding.forgotPass.text = spannableString

        setListeners()
    }

    private fun setListeners() {
        binding.alreadyAccount.setOnClickListener {
            loginFragment.childFragmentManager.beginTransaction().hide(this).commit()
            parentView.visibility = View.VISIBLE
        }
        binding.registerButton.setOnClickListener { register() }
        binding.signGoogle.setOnClickListener { regGoogle() }
    }

    private fun regGoogle() {
        OneTap(requireActivity()).register()
    }

    private fun register() {
        if (binding.email.text.isBlank()){
            binding.email.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_error)
            Toast.makeText(requireContext(), "El correo electrónico no debe estar vacío", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.password.length() < 4 || binding.password.text.isBlank()){
            binding.password.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_error)
            Toast.makeText(requireContext(), "La contraseña no debe contener menos de 4 caracteres", Toast.LENGTH_SHORT).show()
            return }

        // Crear el usuario
        binding.loading.visibility = View.VISIBLE
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        // Initialize Firebase Auth
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    startActivity(Intent(requireActivity(), ResultActivity::class.java))

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

}