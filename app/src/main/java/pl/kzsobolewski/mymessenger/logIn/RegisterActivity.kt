package pl.kzsobolewski.mymessenger.logIn

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import pl.kzsobolewski.mymessenger.R
import pl.kzsobolewski.mymessenger.messages.LatestMessegesActivity
import java.util.*
import pl.kzsobolewski.mymessenger.models.User

class RegisterActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            performRegister()
        }

        alreadyHaveAcc_textView_register.setOnClickListener {
            Log.d(R.string.register_debug_name.toString().toString(), "clicking already have an account text")

            // launching the pl.kzsobolewski.mymessenger.logIn.SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        img_button_register.setOnClickListener{

            val intent  = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)



        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(R.string.register_debug_name.toString().toString(), "photo was selected")

            selectedPhotoUri =  data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri )

            selectedphoto_imageview_register.setImageBitmap(bitmap)

            img_button_register.alpha = 0f
        }
    }

    private fun performRegister()
    {

        val username = username_editText_register.text.toString()
        val email = email_editText_register.text.toString()
        val password = password_editText_register.text.toString()
        val passwordConfirmation = passwordConfirm_editText_register.text.toString()
        val termsAgreement = terms_checkBox_register

        Log.d("RegisterActivity", "Username: " + username)
        Log.d("RegisterActivity", "Email:" + email)
        Log.d("RegisterActivity", "Password :" + password)
        Log.d("RegisterActivity", "Password confirmation :" + passwordConfirmation)
        Log.d("RegisterActivity", "Terms :" + termsAgreement)

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Type in your email and password",Toast.LENGTH_SHORT).show()
            return
        }

        if(passwordConfirmation != password){
            Toast.makeText(this, "Passwords don't match",Toast.LENGTH_SHORT).show()
            return
        }

        if(termsAgreement.isChecked == false){
            Toast.makeText(this, "Please confirm the terms and conditions",Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase authorisation to create new user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener
                    Toast.makeText(this, "User created!",Toast.LENGTH_SHORT).show()
                    Log.d("RegisterActivity", "Successfully created the user with uid: " + it.result.user.uid)

                    uploadImageToFirebase()
                    val intent = Intent(this, LatestMessegesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Toast.makeText(this, it.message,Toast.LENGTH_LONG).show()
                    Log.d("RegisterActivity", "Failed to create the user: " + it.message)
                }
    }



    private fun uploadImageToFirebase(){
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/" + filename)


        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("RegisterActivity", "FB: Successfully added image: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                Log.d( "RegisterActivity" , "FB: File location: $it")

                saveUserToFirebaseDatabase(it.toString())
            }
                    .addOnFailureListener {
                        Log.d("RegisterActivity", "FB: Adding image failed: " + it.message)
                    }
        }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_editText_register.text.toString(), profileImageUrl )

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "FB: Successfully added user.")
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "FB: Adding user failed: " + it.message)
                }
    }
}

