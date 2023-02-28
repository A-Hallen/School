package com.hallen.school

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.hallen.school.databinding.ActivityMainBinding
import com.hallen.school.ui.login.LoginFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private lateinit var oneTapClient: SignInClient
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        loginFragment = LoginFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.login_include, loginFragment) //Reemplace container por el identificador de recurso de su contenedor
        ft.show(loginFragment)
        ft.commit()

        oneTapClient = Identity.getSignInClient(this)
    }


    private fun errorLogin(message:String = ""){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        loginFragment.progressBar.visibility = View.GONE
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        if (auth.currentUser != null){
                                            val database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
                                            val usersRef = database.getReference("usuarios")
                                            val uid = auth.currentUser!!.uid
                                            val name = auth.currentUser!!.displayName
                                            val email =auth.currentUser!!.email!!
                                            usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()) {
                                                        // El UID ya existe en la base de datos
                                                        startActivity(Intent(this@MainActivity, ResultActivity::class.java))
                                                    } else {
                                                        // El UID no existe en la base de datos

                                                        val nuevoUsuario = HashMap<String, Any>()
                                                        nuevoUsuario["nombre"] = name.toString()
                                                        nuevoUsuario["email"] = email
                                                        val childUpdates = HashMap<String, Any>()
                                                        childUpdates[uid] = nuevoUsuario
                                                        usersRef.updateChildren(childUpdates)
                                                        startActivity(Intent(this@MainActivity, ResultActivity::class.java))
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    errorLogin("A ocurrido un error")
                                                }
                                            })}

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        errorLogin("Error de autentificacion")
                                    }
                                }
                        }
                        else -> {
                            Log.d(TAG, "No ID token!") // Shouldn't happen.
                            errorLogin("A ocurrido un error")
                        }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                    errorLogin("A ocurrido un error")
                }
            }
        }
    }

}
