package com.example.firejose

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private lateinit var auth: FirebaseAuth
    private var callbackmanager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.sleep(1500)
        setTheme(R.style.Theme_FireJose)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integracion de Firebase Completa !!")
        analytics.logEvent("InitScreen", bundle)





        //Setup
        setup()
        sesion()
    }

    override fun onStart() {
        super.onStart()
        authLayout.visibility = View.VISIBLE

    }
    private fun sesion(){
        val pref: SharedPreferences = getSharedPreferences(getString(R.string.pref_FILE), Context.MODE_PRIVATE)
        val email: String? = pref.getString("email", null)
        val provider: String? = pref.getString("provider", null)

        if(email != null && provider != null){
            authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup(){
        title="Autenticacion"
        registrarB.setOnClickListener{
            if (emailText.text.isNotEmpty() && psswdText.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(emailText.text.toString(),
                        psswdText.text.toString()).addOnCompleteListener{
                            if(it.isSuccessful){
                                showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                            }else{
                                showAlert()
                            }
                    }
            }
        }
        loginB.setOnClickListener{
            if (emailText.text.isNotEmpty() && psswdText.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(emailText.text.toString(),
                        psswdText.text.toString()).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        }else{
                            showAlert()
                        }
                    }
            }
        }

        googleButton.setOnClickListener{
            //Configuracion
            val googleConf :GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.d_web_client_id))
                .requestEmail()
                .build()
            //Cliente
            var googleClient : GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)


        }



    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando el usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, HomeActivity::class.java)
            .apply { putExtra("email", email)
                    putExtra("provider", provider.name)
                    }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_SIGN_IN){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account = task.getResult(ApiException::class.java)

                if(account != null) {

                    val credential:AuthCredential =GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showHome(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showAlert()
                            }
                        }
                }


            }catch (e:ApiException){
                showAlert()

            }
        }
    }
}