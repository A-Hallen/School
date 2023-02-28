package com.hallen.school.model

import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentSender
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.hallen.school.MainActivity
import com.hallen.school.R

class OneTap(private val context: Context) {
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var signInRequest: BeginSignInRequest

    // Funcion para logearse con google
    fun login(oneTapClient: SignInClient, loading: ProgressBar) {
        signInRequest = BeginSignInRequest.builder() // Creamos el request
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(context.getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener((context as MainActivity)) { result ->
                try {
                    context.startIntentSenderForResult(
                        result.pendingIntent.intentSender, 2,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(context) { e ->
                e.printStackTrace()
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.e(TAG, "No saved credentials found: ${e.localizedMessage}")
                register()
                Log.d(TAG, e.localizedMessage)
            }
    }

    fun register(){
        val oneTapClient = Identity.getSignInClient(context)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(context.getString(R.string.your_web_client_id))
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener((context as MainActivity)) { result ->
                try {
                    context.startIntentSenderForResult(
                        result.pendingIntent.intentSender, 2,
                        null, 0, 0, 0)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(context) { e ->
                // No Google Accounts found. Just continue presenting the signed-out UI.
                Log.d(TAG, e.localizedMessage)
            }
    }
}