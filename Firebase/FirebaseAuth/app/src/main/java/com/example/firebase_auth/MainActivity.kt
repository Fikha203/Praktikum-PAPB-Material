package com.example.firebase_auth

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
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
                // Menampilkan composable LoginScreen dengan state pengguna dan action ketika tombol sign-in atau sign-out ditekan
                LoginScreen(
                    user = currentUser,
                    onSignInClick = { signIn() },
                    onSignOutClick = { signOut() }
                )
            }
        }
    }

    private fun signIn() {
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
                handleSignIn(result) // Memproses credential yang didapat
            } catch (e: GetCredentialException) {
                Log.d(
                    "Error",
                    e.message.toString()
                ) // Menangani kesalahan jika gagal mendapatkan credential
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Mengecek tipe credential yang diterima (misal: Google ID Token)
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Parsing Google ID Token dan melakukan proses autentikasi dengan Firebase
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken) // Melakukan login dengan token
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

    private fun firebaseAuthWithGoogle(idToken: String) {
        // Menggunakan credential Google untuk melakukan login dengan Firebase
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success") // Debugging ketika Login berhasil, log ini dapat dilihat di Logcat
                    val user = auth.currentUser // Mendapatkan pengguna saat ini dari Firebase
                    updateUI(user) // Mengupdate UI berdasarkan status login
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception) // Login gagal
                    updateUI(null) // Mengupdate UI menjadi kondisi tidak login
                }
            }
    }

    private fun signOut() {
        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@MainActivity)
            auth.signOut() // Melakukan logout dari Firebase
            credentialManager.clearCredentialState(ClearCredentialStateRequest()) // Menghapus status credential
        }
        updateUI(null) // Mengupdate UI setelah logout
    }

    private fun updateUI(user: FirebaseUser?) {
        // Mengupdate state `currentUser` untuk mencerminkan status login pengguna
        currentUser = user
    }

    override fun onStart() {
        super.onStart()
        // Mengecek apakah ada pengguna yang sudah login saat aplikasi dimulai
        val currentUser = auth.currentUser
        updateUI(currentUser) // Mengupdate UI berdasarkan status pengguna saat ini
    }

    companion object {
        private const val TAG = "MainActivity" // Tag untuk logging
    }
}


@Composable
fun LoginScreen(
    // Menggunakan stateless UI state
    user: FirebaseUser?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() } // State untuk mengelola Snackbar
    val coroutineScope = rememberCoroutineScope() // Coroutine scope untuk memicu tampilan snackbar

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) } // Menampilkan snackbar di layar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (user == null) { // Jika belum login, tampilkan tombol Sign In
                Button(
                    onClick = {
                        onSignInClick() // Memanggil fungsi sign-in saat tombol diklik
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4285F4),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_logo), // Ikon Google, jangan lupa tambahkan gambar di file drawable
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Sign In with Google")
                    }
                }
            } else { // Jika sudah login, tampilkan pesan dan tombol Sign Out
                Text(text = "Hello, ${user.displayName ?: "User"}!")
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSignOutClick() // Memanggil fungsi sign-out saat tombol diklik
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Anda telah logout") // Menampilkan snackbar saat logout
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, // Warna merah untuk tombol sign-out
                        contentColor = Color.White
                    ),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "Sign Out")
                }

                // Memicu snackbar ketika pengguna login
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Anda telah login")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(user = null, onSignInClick = {}, onSignOutClick = {})
}

