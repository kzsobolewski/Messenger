package pl.kzsobolewski.mymessenger.logIn

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import pl.kzsobolewski.mymessenger.R
import pl.kzsobolewski.mymessenger.messages.LatestMessegesActivity


class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        login_button_signIn.setOnClickListener{
            val email = email_editText_signIn.text.toString()
            val password = password_editText_signIn.text.toString()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Type in your email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener{
                        if(!it.isSuccessful) {
                            Toast.makeText(this, "couldn't log in", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                        val intent = Intent(this, LatestMessegesActivity::class.java)
                        startActivity(intent)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        Toast.makeText(this, "logged in", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }

        }

    }
}
