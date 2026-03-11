package com.example.pocketguard.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.pocketguard.constants.ApiConstants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

/**
 * Helper class para manejar Google Sign-In
 *
 * IMPORTANTE: Necesitas configurar OAuth 2.0 en Google Cloud Console
 * 1. Ve a https://console.cloud.google.com/
 * 2. Crea un proyecto o selecciona uno existente
 * 3. Ve a "APIs & Services" → "Credentials"
 * 4. Crea un "OAuth 2.0 Client ID" tipo "Android"
 * 5. Agrega el SHA-1 de tu keystore (obtenlo con: ./gradlew signingReport)
 * 6. Usa el Client ID que te genere en el campo web_client_id abajo
 */
class GoogleSignInHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(ApiConstants.GOOGLE_CLIENT_ID)
            .requestServerAuthCode(ApiConstants.GOOGLE_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Obtiene el Intent para iniciar el flujo de Google Sign-In
     * Cierra la sesión actual antes para forzar la selección de cuenta
     */
    fun getSignInIntent(): Intent {
        // Cerrar sesión silenciosamente para forzar selector de cuentas
        googleSignInClient.signOut()
        Log.d(TAG, "Sesión de Google cerrada para forzar selector de cuentas")
        return googleSignInClient.signInIntent
    }

    /**
     * Procesa el resultado del Activity de Google Sign-In
     *
     * @param data Intent con el resultado
     * @return GoogleSignInAccount si fue exitoso, null si falló
     */
    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInTask(task)
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar resultado de Google Sign-In", e)
            null
        }
    }

    /**
     * Procesa el Task de Google Sign-In
     */
    private fun handleSignInTask(completedTask: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Google Sign-In exitoso: ${account?.email}")
            account
        } catch (e: ApiException) {
            Log.e(TAG, "Error en Google Sign-In: ${e.statusCode} - ${e.message}")
            null
        }
    }

    /**
     * Obtiene el ID Token del usuario autenticado
     * Este token es el que debes enviar a tu backend
     */
    fun getIdToken(account: GoogleSignInAccount?): String? {
        return account?.idToken
    }

    /**
     * Cierra la sesión de Google
     */
    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d(TAG, "Sesión de Google cerrada")
            onComplete()
        }
    }

    /**
     * Revoca el acceso completamente
     */
    fun revokeAccess(onComplete: () -> Unit) {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            Log.d(TAG, "Acceso de Google revocado")
            onComplete()
        }
    }

    /**
     * Verifica si ya hay una sesión activa de Google
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}



