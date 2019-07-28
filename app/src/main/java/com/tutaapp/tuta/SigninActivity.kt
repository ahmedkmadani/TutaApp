package com.tutaapp.tuta

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_signin.*
import java.util.*
import android.opengl.Visibility as Visibility1




class SigninActivity: AppCompatActivity() {

    private var callbackManager: CallbackManager? = null
    val EMAIL = "email"
    val PUBLIC_PROFILE = "public_profile"

    val USER_PERMISSION = "user_friends"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        facebookLoginButton.setOnClickListener {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList(EMAIL, PUBLIC_PROFILE, USER_PERMISSION))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        val request = GraphRequest.newMeRequest(
                            loginResult.accessToken
                        ) { jsonObject, _ ->
                            Log.d("Facebook JsonObject", jsonObject.toString())

                            val email = jsonObject?.get("email")?.toString() ?: ""
                            val name = jsonObject.get("name").toString()
                            val id = jsonObject.get("id").toString()
                            val profileObjectImage = jsonObject?.getJSONObject("picture")?.getJSONObject("data")?.get("url").toString()

                            Toast.makeText(this@SigninActivity, "Welcome ${name} to Tuta App", Toast.LENGTH_LONG).show()
                            Log.d("Facebook Name", name)
                        }
                        val parameters = Bundle()
                        parameters.putString("fields", "id,name,email,picture.type(large)")
                        request.parameters = parameters
                        request.executeAsync()
                        Log.d("SigninActivity", "Facebook token: " + loginResult.accessToken.token)
                        onSiginSuccess()
                    }

                    override fun onCancel() {
                        Log.d("SigninActivity", "Facebook onCancel.")

                    }


                    override fun onError(exception: FacebookException) {
                        Log.d("SigninActivity", "Facebook onError.")
                    }
                })
        }


        btn_login.setOnClickListener {
            Sigin()
        }

        link_signup.setOnClickListener {
            startActivity(Intent(this@SigninActivity, SignupActivity::class.java))
            finish()
        }
    }


    private fun Sigin() {
//        if (!validate()) {
//            return
//        }


        btn_login.isEnabled = false

        val progressDialog = ProgressDialog(
            this@SigninActivity
        )

        progressDialog.isIndeterminate = true
        progressDialog.setMessage("Authenticating....")
        progressDialog.show()

        android.os.Handler().postDelayed(
            {
                // On complete call either onLoginSuccess or onLoginFailed
                onSiginSuccess()
                // onLoginFailed();
                progressDialog.dismiss()
            }, 3000
        )


}

    private fun onSiginSuccess() {

        btn_login.isEnabled
        Toast.makeText(this@SigninActivity, "Signin Successfully", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))

    }


    fun validate(): Boolean {
        var valid = true

        val email = input_email!!.text.toString()
        val password = input_password!!.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email!!.error = "enter a valid email address"
            valid = false
        } else {
            input_email!!.error = null
        }

        if (password.isEmpty() || password.length < 2 || password.length > 10) {
            input_password!!.error = "between 4 and 10 alphanumeric characters"
            valid = false
        } else {
            input_password!!.error = null
        }

        return valid
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)


    }
}