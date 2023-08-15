package com.example.bookmark

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.util.Patterns
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookmark.databinding.ActivityLogInBinding
import com.example.bookmark.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LogIn : AppCompatActivity() {

    val emailKey: String = "email"
    val passwordKey: String = "password"
    val SharedPreferenceDataToSave : String = "SharedPreferenceDataToSave"
    lateinit var  sharedpreferences: SharedPreferences
    var savedEmail: String? = null
    var savedPassword: String? = null
    var emailPlaceholder: String? = null
    var passwordPlaceholder: String? = null

    private lateinit var binding: ActivityLogInBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedpreferences = getSharedPreferences(SharedPreferenceDataToSave, Context.MODE_PRIVATE)


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        emailPlaceholder = intent.getStringExtra("email")
        passwordPlaceholder = intent.getStringExtra("password")
        if(intent.getStringExtra("email") == "toBeRemoved" && intent.getStringExtra("password") == "toBeRemoved")
            removeData()
        loadData()
        if(savedEmail != null && savedPassword != null){

            loginUser(savedEmail!!, savedPassword!!)
        } else {
            binding.btnLogin.setOnClickListener {

                validateData()
            }

            binding.register.setOnClickListener {
                startActivity(Intent(this@LogIn, Register::class.java))
                finish()
            }
        }


    }

    private fun loadData(){
        savedEmail = sharedpreferences.getString(emailKey, null)
        savedPassword = sharedpreferences.getString(passwordKey, null)
    }

    private fun saveData(email: String, password: String){
        var editor: SharedPreferences.Editor = sharedpreferences.edit()
        editor.putString(emailKey, email)
        editor.putString(passwordKey, password)
        editor.commit()
    }

    fun removeData(){
        var editor: SharedPreferences.Editor = sharedpreferences.edit()
        editor.remove(emailKey)
        editor.remove(passwordKey)
        editor.commit()
    }

    private var email = ""
    private var password = ""

    private fun validateData() {

        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPassword.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show()
        } else if(password.isEmpty()){

            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        } else {
            loginUser(email,password)
        }
    }

    private fun loginUser(email: String, password: String) {

        progressDialog.setMessage("Logging In!")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
                saveData(email,password)
            }
            .addOnFailureListener { e->
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun checkUser() {

        progressDialog.setMessage("Checking user...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    startActivity(Intent(this@LogIn,Home::class.java))
                    finish()

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}




