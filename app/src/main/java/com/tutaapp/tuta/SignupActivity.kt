package com.tutaapp.tuta

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_signup.*


class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        btn_sigin.setOnClickListener(View.OnClickListener {
            Signup()
        })

        link_signin.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@SignupActivity, SigninActivity::class.java))
            finish()
        })
    }

    private fun Signup() {
//        if (!validate()) {
//            return
//        }

        btn_sigin.isEnabled = false

        val progressDialog = ProgressDialog(
            this@SignupActivity
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

        btn_sigin.isEnabled
        Toast.makeText(this@SignupActivity, "SignUp Successfully", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun validate(): Boolean {
        var valid = true

        val phone = input_phone!!.text.toString()
        val password = input_password!!.text.toString()

        if (phone.isEmpty() || !android.util.Patterns.PHONE.matcher(phone).matches() || !android.util.Patterns.EMAIL_ADDRESS.matcher(phone).matches()) {
            input_phone!!.error = "enter a valid phone number"
            valid = false
        } else {
            input_phone!!.error = null
        }

        if (password.isEmpty() || password.length < 2 || password.length > 10) {
            input_password!!.error = "between 4 and 10 alphanumeric characters"
            valid = false
        } else {
            input_password!!.error = null
        }

        return valid
    }
}