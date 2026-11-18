package com.example.actgymart

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var intentosRestantes = 3
    private var estaBloqueado = false

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private lateinit var btnGoogle : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnInicioSesion = findViewById<AppCompatButton>(R.id.btnInicioSesion)
        val btnCrearCuenta = findViewById<Button>(R.id.btnCrearCuenta)

        btnGoogle = findViewById(R.id.btnGoogle)
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        val admin = AdminSQLiteOpenHelper(this)

        btnInicioSesion.setOnClickListener {
            if (estaBloqueado) {
                Toast.makeText(this, "Espera a que termine el tiempo de bloqueo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = etUsuario.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                if (admin.validarUsuario(user, pass)) {
                    intentosRestantes = 3
                    Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PantallaPrincipalActivity::class.java)
                    intent.putExtra("USUARIO_LOGUEADO", user)
                    startActivity(intent)
                    finish()
                } else {
                    intentosRestantes--
                    if (intentosRestantes > 0) {
                        Toast.makeText(
                            this,
                            "Usuario o contraseña incorrectos. Te quedan $intentosRestantes intento(s)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        estaBloqueado = true
                        Toast.makeText(this, "Has superado los 3 intentos. Espera 3 minutos", Toast.LENGTH_LONG).show()

                        object : CountDownTimer(180000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}

                            override fun onFinish() {
                                estaBloqueado = false
                                intentosRestantes = 3
                                Toast.makeText(this@MainActivity, "Ya puedes volver a intentarlo", Toast.LENGTH_SHORT).show()
                            }
                        }.start()
                    }
                }
            }
        }

        btnCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }
    }

    override fun onStart() {
        super.onStart()
        val usuarioActual = auth.currentUser
        if (usuarioActual != null) {
            val intent = Intent(this, PantallaPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun autenticarFirebase(idToken: String){

        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credencialFirebase).addOnCompleteListener(this) {

                inicio ->

            if(inicio.isSuccessful){
                val usuario = auth.currentUser

                Toast.makeText(this, "Bienvenido ${usuario?.displayName}", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, PantallaPrincipalActivity::class.java)

                startActivity(intent)

                finish()

            }else{
                Toast.makeText(this, "Error al iniciar con Firebase", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun procesarCredencial(credential: Credential){

        if(credential is CustomCredential && credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){

            try {

                val credencialGoogle = GoogleIdTokenCredential.createFrom(
                    credential.data
                )

                autenticarFirebase(credencialGoogle.idToken)

            }catch (e : GoogleIdTokenParsingException){
                Toast.makeText(this, "Credencial inválida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesionGoogle(){

        val inicioGoogle = GetSignInWithGoogleOption.Builder(
            getString(R.string.default_web_client_id)).build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(inicioGoogle)
            .build()

        lifecycleScope.launch {

            try {

                val resultado = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = solicitud
                )

                procesarCredencial(resultado.credential)

            }catch (e: GetCredentialException){
                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }

        }
    }
}