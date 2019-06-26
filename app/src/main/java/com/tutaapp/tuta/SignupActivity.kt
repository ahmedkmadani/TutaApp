package com.tutaapp.tuta

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
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
        if (!validate()) {
            return
        }
        onSiginSuccess()
    }

    private fun onSiginSuccess() {
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