package com.example.bookmark.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bookmark.Home
import com.example.bookmark.LogIn
import com.example.bookmark.R
import com.example.bookmark.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.format.DateTimeFormatter

private lateinit var binding: UserProfile


class UserProfile : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        loadUserInfo()

        binding.IVBackToolBar.setOnClickListener {
            (activity as Home).supportFragmentManager.beginTransaction().apply {
                replace(R.id.flWrapper, (activity as Home).getHomeFragment())
                addToBackStack(null)
                commit()
            }
        }

        binding.TVEditToolBar.setOnClickListener {

                Toast.makeText(activity as Home, "Edit clicked", Toast.LENGTH_SHORT).show()
                (activity as Home).supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flWrapper, (activity as Home).getUserEditFragment())
                    addToBackStack(null)
                    commit()
                }
            }

            binding.TVSignOut.setOnClickListener{
                firebaseAuth.signOut()
                Toast.makeText(activity as Home, "Signout is clicked", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity as Home, LogIn::class.java)
                intent.putExtra("email", "toBeRemoved")
                intent.putExtra("password", "toBeRemoved")
                activity!!.startActivity(intent)
                (activity as Home?)!!.finish()

            }


        return view
        }




    fun loadUserInfo() {

        mDBRef = FirebaseDatabase.getInstance().getReference()
        //val ref = FirebaseDatabase.getInstance().getReference("Users")
        mDBRef.child("Users").child(firebaseAuth.uid!!).child("readingChallenge")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                val goal = "${snapshot.child("num").value}"
                    binding.TVReadingGoalData.text = goal.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    //val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    var formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
                    val formattedDate = timestamp.format(formatter)

                    binding.UserName.text = name
                    binding.TVMemberSinceData.text = formattedDate
                   // binding.TVReadingGoalData.text = "0"
                    //binding.TVBookshelvesCount.text = "0"

                   /* try {
                        Glide.with(this@UserProfile)
                            .load(profileImage)
                            .placeholder(R.drawable.user2)
                            .into(binding.UserProfileImage)


                    } catch (e: java.lang.Exception) {

                    }*/
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }


            })
    }
}




