package com.example.firejose

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEBOOK
}
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Setup
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")
        setup(email ?:"", provider ?:"")

        //Persistencia de datos
        val pref: SharedPreferences.Editor = getSharedPreferences(getString(R.string.pref_FILE), Context.MODE_PRIVATE)
            .edit()
        pref.putString("email", email)
        pref.putString("provider", provider)
        pref.apply()
    }

    private fun setup(email: String, provider: String){
        title = "Inicio"
        emailTextView.text = email
        psswdTextView.text = provider

        button.setOnClickListener {
            val pref: SharedPreferences.Editor = getSharedPreferences(getString(R.string.pref_FILE), Context.MODE_PRIVATE)
                .edit()
            pref.clear()
            pref.apply()


            if(provider == ProviderType.FACEBOOK.name){
                LoginManager.getInstance().logOut()
            }

            FirebaseAuth.getInstance().signOut()
            onBackPressed()

        }
    }
}