package ar.com.develup.tateti.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ar.com.develup.tateti.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.actividad_inicial.*
import com.google.firebase.analytics.FirebaseAnalytics //Para implementar analytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class ActividadInicial : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics //Declaramos el objeto que usará analytics
    private lateinit var auth: FirebaseAuth //Declaramos el objeto que usará Authentication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_inicial)

        firebaseAnalytics = Firebase.analytics //Lo inicializamos
        auth = Firebase.auth //Se lo inicializa

        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
            fetchTimeoutInSeconds = 10
        }
        Firebase.remoteConfig.setConfigSettingsAsync(settings)

        iniciarSesion.setOnClickListener { iniciarSesion() }
        registrate.setOnClickListener { registrate() }
        olvideMiContrasena.setOnClickListener { olvideMiContrasena() }

        if (usuarioEstaLogueado()) {
            // Si el usuario esta logueado, se redirige a la pantalla
            // de partidas
            verPartidas()
            finish()
        }
        actualizarRemoteConfig()
    }

    private fun usuarioEstaLogueado(): Boolean {
        // TODO-05-AUTHENTICATION
        // Validar que currentUser sea != null
        val user = auth.currentUser
        var userLogueado = false
        if(user != null){
            userLogueado = true
        }
        return userLogueado
    }

    private fun verPartidas() {
        val intent = Intent(this, ActividadPartidas::class.java)
        startActivity(intent)
    }

    private fun registrate() {
        firebaseAnalytics.logEvent("Boton registrate"){
            param("Presionado_Registrate", "Un usuario quiere abrise una cuenta")
        }
        val intent = Intent(this, ActividadRegistracion::class.java)
        startActivity(intent)
    }

    private fun actualizarRemoteConfig() {
        configurarDefaultsRemoteConfig()
        configurarOlvideMiContrasena()
    }

    private fun configurarDefaultsRemoteConfig() {
        // TODO-04-REMOTECONFIG
        // Configurar los valores por default para remote config,
        // ya sea por codigo o por XML
        Firebase.remoteConfig.setDefaultsAsync(R.xml.firebase_config_defaults)
    }

    private fun configurarOlvideMiContrasena() {
        // TODO-04-REMOTECONFIG
        // Obtener el valor de la configuracion para saber si mostrar
        // o no el boton de olvide mi contraseña
       /* val botonOlvideHabilitado = false*/
        Firebase.remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    val botonOlvideHabilitado = Firebase.remoteConfig.getBoolean("olvideMiContrasena")
                    if (botonOlvideHabilitado) {
                        olvideMiContrasena.visibility = View.VISIBLE
                    } else {
                        olvideMiContrasena.visibility = View.GONE
                    }
                }
    }

    private fun olvideMiContrasena() {
        // Obtengo el mail
        val email = email.text.toString()

        // Si no completo el email, muestro mensaje de error
        if (email.isEmpty()) {
            Snackbar.make(rootView!!, "Completa el email", Snackbar.LENGTH_SHORT).show()
        } else {
            // TODO-05-AUTHENTICATION
            // Si completo el mail debo enviar un mail de reset
            // Para ello, utilizamos sendPasswordResetEmail con el email como parametro
            // Agregar el siguiente fragmento de codigo como CompleteListener, que notifica al usuario
            // el resultado de la operacion
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                     Snackbar.make(rootView, "Email enviado", Snackbar.LENGTH_SHORT).show()
                  } else {
                      Snackbar.make(rootView, "Error " + task.exception, Snackbar.LENGTH_SHORT).show()
                  }
              }
        }
    }

    private fun iniciarSesion() {
        val email = email.text.toString()
        val password = password.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(applicationContext, "Por favor llene los campos", Toast.LENGTH_LONG).show()
            /*
            //CRASH FORZADO PARA VER LOS REGISTROS EN LA CONSOLA DE CRASHLYTICS DE FIREBASE

            //Evento que registra que hizo el usuario antes del crash
            FirebaseCrashlytics.getInstance().log("El usuario intentó iniciar sesión con alguno de los campos vacíos (email, password)")

            //Info contextual en forma de clave-valor
            FirebaseCrashlytics.getInstance().setCustomKey("InicioSesion","El usuario quiso iniciar sesión sin llenar ambos campos (email, password)")

            //Lanzamiento del crash
            throw RuntimeException("Test Crash")
            */
        }else{
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN){
                param("LogIn","Un usuario ha iniciado sesión exitosamente")
            }
            auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(authenticationListener)
           // verPartidas()
        }
        // TODO-05-AUTHENTICATION
        // IMPORTANTE: Eliminar  la siguiente linea cuando se implemente authentication
       // verPartidas()


        // TODO-05-AUTHENTICATION
        // hacer signInWithEmailAndPassword con los valores ingresados de email y password
        // Agregar en addOnCompleteListener el campo authenticationListener definido mas abajo
    }

        private val authenticationListener: OnCompleteListener<AuthResult?> = OnCompleteListener<AuthResult?> { task ->
           if (task.isSuccessful) {
               val user = auth.currentUser
                if (usuarioVerificoEmail(user)) {
                    verPartidas()
                } else {
                    desloguearse()
                    Snackbar.make(rootView!!, "Verifica tu email para continuar", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                if (task.exception is FirebaseAuthInvalidUserException) {
                    Snackbar.make(rootView!!, "El usuario no existe", Snackbar.LENGTH_SHORT).show()
                } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(rootView!!, "Credenciales inválidas", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

    private fun usuarioVerificoEmail(user: FirebaseUser?): Boolean {
        // TODO-05-AUTHENTICATION
        // Preguntar al currentUser si verifico email
        var emailVerified = false
        if(user!!.isEmailVerified){
            emailVerified = true
        }
        return emailVerified
    }

    private fun desloguearse() {
        // TODO-05-AUTHENTICATION
        // Hacer signOut de Firebase
        auth.signOut()
    }
}