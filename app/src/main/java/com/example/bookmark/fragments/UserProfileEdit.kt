package com.example.bookmark.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bookmark.Home
import com.example.bookmark.R
import com.example.bookmark.databinding.FragmentUserProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.format.DateTimeFormatter
import android.content.ContentResolver
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookmark.databinding.FragmentUserProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage

private lateinit var binding: UserProfileEdit

class UserProfileEdit : Fragment() {

    private lateinit var binding: FragmentUserProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    //progress dialog
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserProfileEditBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        progressDialog = ProgressDialog(activity as Home)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadUserInfo()

        binding.TVCancel.setOnClickListener{
            (activity as Home).supportFragmentManager.beginTransaction().apply {
                replace(R.id.flWrapper, (activity as Home).getUserFragment())
                addToBackStack(null)
                commit()
            }
        }

        binding.TVSave.setOnClickListener {

            validateData()
        }

        /*binding.EditUserProfileImage.setOnClickListener{
            showImageAttachMenu()
        }*/

        return view
    }

    private var name = ""
    private fun validateData() {

        name = binding.EditUserName.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(activity as Home, "Enter name", Toast.LENGTH_SHORT).show()

        } else{

            //name is entered

            if(imageUri == null){
                //update without image
                updateProfile("")
            } else {
                //update with image
                uploadImage()
            }
        }

    }

    private fun uploadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

       //image path and name, using uid to replace previous
       val filePathAndName =  "ProfileImages/" + firebaseAuth.uid

        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener {taskSnapshot->
                //image uploaded, get url of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)


        }
            .addOnFailureListener{ e->
                //failed to upload image
                progressDialog.dismiss()
                Toast.makeText(activity as Home, "Failed to upload image due to $e.message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadImageUrl: String) {
        progressDialog.setMessage("Updating profile...")

        //set info to update to db
        val hashMap : HashMap <String, Any> = HashMap()
        hashMap["name"] = "$name"
        if(imageUri != null) {
            hashMap["profileImage"] = uploadImageUrl
        }

        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                //profile updated
                progressDialog.dismiss()
                Toast.makeText(activity as Home, "Profile updated.", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                //failed to update profile
                progressDialog.dismiss()
                Toast.makeText(activity as Home, "Failed to update profile due to $e.message", Toast.LENGTH_SHORT).show()

            }

    }

    fun loadUserInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    //val profileImage = "${snapshot.child("profileImage").value}"
                   /* val timestamp = "${snapshot.child("timestamp").value}"

                    var formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
                    val formattedDate = timestamp.format(formatter)*/

                    binding.EditUserName.setText(name)
                    //binding.EditReadingGoalData.setText("0")


                    /*try {
                        Glide.with(this@UserProfileEdit)
                            .load(profileImage)
                            .placeholder(R.drawable.user2)
                            .into(binding.EditUserProfileImage)


                    } catch (e: java.lang.Exception) {

                    }*/
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }


            })
    }
    
    /*private fun showImageAttachMenu(){

        val popupMenu = PopupMenu(activity as Home, binding.EditUserProfileImage)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

         popupMenu.setOnMenuItemClickListener { item ->

             val id = item.itemId
             if(id == 0){
                 pickImageCamera()
             } else if (id == 1){
                 pickImageGallery()
             }
             true

         }

        }*/

    private fun pickImageCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp")

        val contentResolver = context?.contentResolver
        //imageUri = contentResolver?.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

        imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    private fun pickImageGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)

    }

    //used to handle result of camera intent
    private val cameraActivityResultLauncher = registerForActivityResult(

        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->

            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                //imageUri = data!!.data

                //binding.EditUserProfileImage.setImageURI(imageUri)

            }
            else {
                Toast.makeText(activity as Home, "Cancelled", Toast.LENGTH_SHORT).show()
            }

        }
    )

    //used to handle result of gallery intent
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result->

            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data

                //binding.EditUserProfileImage.setImageURI(imageUri)

            }
            else{
                Toast.makeText(activity as Home, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}