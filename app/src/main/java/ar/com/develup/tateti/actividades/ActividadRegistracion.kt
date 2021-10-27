package ar.com.develup.tateti.actividades

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.actividad_registracion.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ActividadRegistracion : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics //Declaramos el objeto que usará analytics
    private lateinit var auth: FirebaseAuth //Declaramos el objeto que usará authentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_registracion)

        firebaseAnalytics = Firebase.analytics //Lo inicializamos
        auth = Firebase.auth //Se lo inicializa

        registrar.setOnClickListener { registrarse() }
    }

    fun registrarse() {
        val passwordIngresada = password.text.toString()
        val confirmarPasswordIngresada = confirmarPassword.text.toString()
        val email = email.text.toString()

        if (email.isEmpty()) {
            // Si no completo el email, muestro mensaje de error
            Snackbar.make(rootView, "Email requerido", Snackbar.LENGTH_SHORT).show()
        } else if (passwordIngresada == confirmarPasswordIngresada) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP){
                param("SIGNUP","Se creó una nueva cuenta")
            }

            // Si completo el email y las contraseñas coinciden, registramos el usuario en Firebase
            registrarUsuarioEnFirebase(email, passwordIngresada)

        } else {
            // No coinciden las contraseñas, mostramos mensaje de error
            Snackbar.make(rootView, "Las contraseñas no coinciden", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun registrarUsuarioEnFirebase(email: String, passwordIngresada: String) {
        // TODO-05-AUTHENTICATION
        // Crear el usuario con el email y passwordIngresada
        // Ademas, registrar en CompleteListener el listener registracionCompletaListener definido mas abajo
        auth.createUserWithEmailAndPassword(email,passwordIngresada)
                .addOnCompleteListener(registracionCompletaListener)
    }

    private val registracionCompletaListener: OnCompleteListener<AuthResult?> = OnCompleteListener { task ->
       if (task.isSuccessful) {
           // Si se registro OK, muestro mensaje y envio mail de verificacion
            Snackbar.make(rootView, "Registro exitoso", Snackbar.LENGTH_SHORT).show()
            val user = auth.currentUser
            enviarEmailDeVerificacion(user)
       } else if (task.exception is FirebaseAuthUserCollisionException) {
           // Si el usuario ya existe, mostramos error
           Snackbar.make(rootView, "El usuario ya existe", Snackbar.LENGTH_SHORT).show()
        } else {
            // Por cualquier otro error, mostramos un mensaje de error
            Snackbar.make(rootView, "El registro fallo: " + task.exception, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun enviarEmailDeVerificacion(user: FirebaseUser?) {
        user!!.sendEmailVerification()
        // TODO-05-AUTHENTICATION
        // Enviar mail de verificacion al usuario currentUser
    }
}