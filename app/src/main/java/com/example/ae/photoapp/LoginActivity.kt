package com.example.ae.photoapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.parse.ParseUser
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_login.*
import org.w3c.dom.Text

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if(ParseUser.getCurrentUser()!=null)
        {
            Log.i("info","User Found")
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        login.setOnClickListener{
            Log.i("info","Btn click")
            val username = findViewById<EditText>(R.id.username)
            val password = findViewById<EditText>(R.id.password)

            ParseUser.logInInBackground(username.text.toString(),password.text.toString(),{user,e->
                if(e==null)
                {
                    Log.i("info","User Found :"+user.username)
                    val intent= Intent(this,MainActivity::class.java)
                    startActivity(intent)

                }
                else
                {
                    Log.i("info","No User Found"+e.message)
                }

            })
        }
    }
    fun signUp()
    {
        val parseUser = ParseUser()
        parseUser.username=username.text.toString()
        parseUser.setPassword(password.text.toString())
        parseUser.signUpInBackground { e->
            if(e==null)
            {
                Log.i("info" , "SignUp Successfull")
            }
            else
            {
                Log.i("info" , "SignUp not Successfull"+e.message)
            }
        }
    }
}
