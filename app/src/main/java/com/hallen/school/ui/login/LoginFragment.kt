package com.hallen.school.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.annotations.concurrent.Background
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.hallen.school.R
import com.hallen.school.ResultActivity
import com.hallen.school.databinding.FragmentLoginBinding
import com.hallen.school.model.OneTap


class LoginFragment : Fragment() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var binding: FragmentLoginBinding
    lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        oneTapClient = Identity.getSignInClient(requireContext()) // Inicializamos la variable onTapClient
        return binding.root // Retornamos la vista
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.loading
        // Cambiamos el estilo de estos dos TextViews para que se parezcan a un hiperenlace
        val text = "Olvidé mi contraseña"; val regText = "Registrarse"
        val spannableString = SpannableString(text).apply { setSpan(UnderlineSpan(), 0, length, 0)  }
        val spannableString2 = SpannableString(regText).apply { setSpan(UnderlineSpan(), 0, length, 0)  }
        binding.register.text = spannableString2; binding.forgotPass.text = spannableString
        setListeners() // Ejecutamos los listeners del fragment.
    }

    private fun changeBackground(view: EditText, blank: Boolean = false){
        if (!blank) {
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_bg)
        } else {
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.edit_error)    }
    }
    private fun toast(text: String) = Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT)

    private fun setListeners() {
        binding.logginButton.setOnClickListener {   login()   } // Llamamos a la funcion login encargada de logear al usuario
        binding.email.doOnTextChanged    {_,_,_,_ -> changeBackground(binding.email)     } // Colocamos normal el editext del email si el texto en el no esta vacio.
        binding.password.doOnTextChanged {_,_,_,_ -> changeBackground(binding.password)  } // Hacemos lo mismo con el editext del password.
        binding.register.setOnClickListener   {     register()        }
        binding.signGoogle.setOnClickListener {     loginGoogle()     }
    }

    private fun loginGoogle() {
        progressBar.visibility = View.VISIBLE // Hacemos visible el progressBar
        OneTap(requireContext()).login(oneTapClient, progressBar) // Logeamos al usuario con la clase OnTap, el resto del codigo esta alla
    }

    private fun login() {
        if (binding.email.text.isBlank()){
            changeBackground(binding.email, true) // Cambiamos el fondo del edit a error
            toast("El nombre de usuario no debe estar vacío").show(); return
        } else if (binding.password.length() < 4 || binding.password.text.isBlank()){
            changeBackground(binding.password, true) // Cambiamos el fondo del edit a edit_error
            toast("La contraseña no debe contener menos de 4 caracteres").show(); return }

        //Autentificar
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        // Initialize Firebase Auth
        val credential = EmailAuthProvider.getCredential(email, password)
        val auth = Firebase.auth
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // El usuario ha iniciado sesión correctamente.
                    if (auth.currentUser != null) {
                        val database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
                        val usersRef = database.getReference("usuarios")
                        val uid = auth.currentUser!!.uid
                        val name = auth.currentUser!!.displayName
                        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // El UID ya existe en la base de datos
                                    startActivity(Intent(requireContext(), ResultActivity::class.java))
                                } else {
                                    // El UID no existe en la base de datos
                                    val nuevoUsuario = HashMap<String, Any>()
                                    nuevoUsuario["nombre"] = name.toString()
                                    nuevoUsuario["email"] = email
                                    val childUpdates = HashMap<String, Any>()
                                    childUpdates[uid] = nuevoUsuario
                                    usersRef.updateChildren(childUpdates)
                                    startActivity(Intent(requireContext(), ResultActivity::class.java))
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "A ocurrido un error", Toast.LENGTH_SHORT).show()
                                // Error al realizar la consulta
                                // Realiza las acciones necesarias aquí
                            }
                        })

                    }
                } else {
                    // Ha habido un error al iniciar sesión.
                    task.exception!!.printStackTrace()
                    Toast.makeText(requireContext(),"A ocurrido un error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
            }


    }

    private fun register() {
        // Reemplazar el contenedor actual (LinearLayout) con el nuevo fragment
        val registerFragment = RegisterFragment(binding.loginContainer, this)
        val ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.form_layout, registerFragment) //Reemplace container por el identificador de recurso de su contenedor
        binding.loginContainer.visibility = View.GONE
        ft.addToBackStack(null)
        ft.show(registerFragment)
        ft.commit()
    }

}