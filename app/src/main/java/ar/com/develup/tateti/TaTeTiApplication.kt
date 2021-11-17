package ar.com.develup.tateti

import android.app.Application
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class TaTeTiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO-07-NOTIFICATION
        // Suscribirse al topic "general"
        Firebase.messaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                // En este momento conocemos el valor del token
                Log.d("Notificaciones", it.result!!)
            }
        }
    }
}