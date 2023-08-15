package com.example.bookmark

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookmark.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    val defaultBookshelvesArrayList = ArrayList<String>()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //progress dialog, will show while creating an account
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.TVCancel.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            (this@Register)!!.finish()
        }

        binding.btnRegister.setOnClickListener {

            validateData()
            }

        defaultBookshelvesArrayList.add("Want To Read")
        defaultBookshelvesArrayList.add("Currently Reading")
        defaultBookshelvesArrayList.add("Read")

        }

        private var name = ""
        private var email = ""
        private var password = ""

        private fun validateData(){
            name = binding.edtname.text.toString().trim()
            email = binding.edtemail.text.toString().trim()
            password = binding.edtpassword.text.toString().trim()
            val confPass = binding.edtconfPass.text.toString().trim()

            if(name.isEmpty()){

                Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show()
            } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show()
            } else if(password.isEmpty()){

                Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
            } else if(confPass.isEmpty()){

                Toast.makeText(this, "Confirm password...", Toast.LENGTH_SHORT).show()
            } else if( password != confPass){

                Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show()
            } else {

                createUserAccount()
            }
        }

    private fun createUserAccount() {

        progressDialog.setMessage("Creating Account!")
        progressDialog.show()

        createDefaultBookshelves()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
            updateUserInfo()
                createDefaultBookshelves()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account due to ${e.message}", Toast.LENGTH_SHORT).show()

            }

        createDefaultBookshelves()
    }

    private fun createDefaultBookshelves() {

        defaultBookshelvesArrayList.forEachIndexed{ index, element ->

            val timestamp = System.currentTimeMillis()
            //setup data to add to firebase
            val hashMap = HashMap<String, Any>()
            hashMap["id"] = "$timestamp" // in string quotes because timestamp is in double, we need in string for id
            hashMap["title"] = "$element"
            hashMap["timestamp"] = timestamp
            hashMap["uid"] = "${firebaseAuth.uid}"

            //add to firebase
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid.toString()).child("Bookshelves").child("$timestamp")
                .setValue(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this@Register, "Created successfully", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener{e->
                    Toast.makeText(this@Register, "Failed to create due to ${e.message}", Toast.LENGTH_SHORT).show()


                }


        }


    }

    private fun updateUserInfo() {

        progressDialog.setMessage("Saving user info!")

        val timestamp  = System.currentTimeMillis()

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // empty, will be added in profile editing
        hashMap["timeStamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Register, LogIn::class.java))
                finish()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving user info due to ${e.message}", Toast.LENGTH_SHORT).show()


            }
    }


}

