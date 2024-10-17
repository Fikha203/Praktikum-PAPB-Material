package com.example.firebase_auth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase_auth.screen.home.HomeScreen
import com.example.firebase_auth.screen.login.LoginScreen
import com.example.firebase_auth.ui.theme.Firebase_AuthTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth // Inisialisasi variabel FirebaseAuth untuk otentikasi pengguna Firebase
    private var currentUser by mutableStateOf<FirebaseUser?>(null) // Mutable state untuk menyimpan data pengguna saat ini (null jika belum login)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = Firebase.auth // Mengambil instance FirebaseAuth


        setContent {
            Firebase_AuthTheme {

                val navController = rememberNavController()

                // Tentukan navGraph
                NavHost(
                    navController = navController,
                    startDestination = if (currentUser != null) "home" else "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onSignInClick = { signIn(navController) },
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            user = currentUser,
                            onSignOutClick = { signOut(navController) }
                        )
                    }
                }

            }
        }
    }

    private fun signIn(navController: NavController) {
        // Inisialisasi CredentialManager untuk mengelola credential pengguna
        val credentialManager = CredentialManager.create(this)

        // Opsi untuk mendapatkan Google ID Token dengan akun yang belum diotorisasi sebelumnya
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.your_web_client_id)) // Mengambil Web Client ID dari resource, tambahkan value web_client_id kalian disini
            .build()

        // Request untuk mendapatkan credential
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                // Mendapatkan credential pengguna melalui CredentialManager
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@MainActivity,
                )
                handleSignIn(result, navController) // Memproses credential yang didapat
            } catch (e: GetCredentialException) {
                Log.d(
                    "Error",
                    e.message.toString()
                ) // Menangani kesalahan jika gagal mendapatkan credential
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse, navController: NavController) {
        // Mengecek tipe credential yang diterima (misal: Google ID Token)
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Parsing Google ID Token dan melakukan proses autentikasi dengan Firebase
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(
                            googleIdTokenCredential.idToken,
                            navController
                        ) // Melakukan login dengan token
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(
                            TAG,
                            "Received an invalid google id token response",
                            e
                        ) // Menangani error parsing ID Token
                    }
                } else {
                    Log.e(
                        TAG,
                        "Unexpected type of credential"
                    ) // Menangani credential yang tidak dikenali
                }
            }

            else -> {
                Log.e(
                    TAG,
                    "Unexpected type of credential"
                ) // Menangani credential yang tidak dikenali
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, navController: NavController) {
        // Menggunakan credential Google untuk melakukan login dengan Firebase
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG,
                        "signInWithCredential:success"
                    ) // Debugging ketika Login berhasil, log ini dapat dilihat di Logcat
                    val user = auth.currentUser // Mendapatkan pengguna saat ini dari Firebase

                    updateUI(user) // Mengupdate UI berdasarkan status login
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception) // Login gagal
                    updateUI(null) // Mengupdate UI menjadi kondisi tidak login
                }
            }
    }

    private fun signOut(navController: NavController) {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@MainActivity)
            auth.signOut() // Melakukan logout dari Firebase
            credentialManager.clearCredentialState(ClearCredentialStateRequest()) // Menghapus status credential
        }
        updateUI(null) // Mengupdate UI setelah logout
        navController.navigate("login") { popUpTo("home") { inclusive = true } }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Mengupdate state `currentUser` untuk mencerminkan status login pengguna
        currentUser = user
    }

    override fun onStart() {
        super.onStart()
        // Mengecek apakah ada pengguna yang sudah login saat aplikasi dimulai
        updateUI(auth.currentUser) // Mengupdate UI berdasarkan status pengguna saat ini
    }

    companion object {
        private const val TAG = "MainActivity" // Tag untuk logging
    }
}




