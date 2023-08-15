package com.example.bookmark.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.bookmark.R
import com.example.bookmark.book.BookInfo
import com.example.bookmark.bookshelf.ModelBookshelf
import com.example.bookmark.databinding.DialogBookshelfNewBinding
import com.example.bookmark.databinding.DialogEditReadingGoalBinding
import com.example.bookmark.databinding.DialogSetGoalBinding
import com.example.bookmark.databinding.FragmentReadingGoalBinding
import com.example.bookmark.user.AnnualReadingChallenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime


class ReadingGoalFragment : Fragment() {


    private lateinit var binding : FragmentReadingGoalBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDBRef: DatabaseReference
    private var num: TextView? = null
    private var uid : String? = null
    private var bid: String? = null
    private var shelf: String? = null
    var visible: Boolean = false
    var yearReadingChallenge: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReadingGoalBinding.inflate(inflater, container, false )
        val view: View = binding.root

        mDBRef = FirebaseDatabase.getInstance().getReference()

        progressReadingChallenge()

        mDBRef.child("Users").child(mAuth.currentUser?.uid!!).child("readingChallenge").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    progressReadingChallenge()
                   binding.RGFragmentUser.visibility = View.VISIBLE
                   binding.RGFragmentGoal.visibility = View.VISIBLE
                   binding.editGoalBtn.visibility = View.VISIBLE
                   binding.pbGoal.visibility = View.VISIBLE
                   binding.tvPercent.visibility = View.VISIBLE
                   binding.RGFragmentLeft.visibility = View.VISIBLE
                } else {
                    showSetDialog()
                    binding.RGNoGoal.visibility = View.VISIBLE
                    binding.IVNoGoal.visibility = View.VISIBLE
                    //binding.RGFragmentUser.setText("Click here to set your goal!")
                    binding.RGFragmentUser.visibility = View.GONE
                    binding.RGFragmentGoal.visibility = View.GONE
                    binding.editGoalBtn.visibility = View.GONE
                    binding.pbGoal.visibility = View.GONE
                    binding.tvPercent.visibility = View.GONE
                    binding.RGFragmentLeft.visibility = View.GONE

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        binding.RGNoGoal.setOnClickListener {

            showSetDialog()
        }

        binding.editGoalBtn.setOnClickListener {

            showEditDialog()
        }








        return view

    }

    @SuppressLint("NewApi")
    private fun showEditDialog() {

        val editGoalBinding = DialogEditReadingGoalBinding.inflate(LayoutInflater.from(this.context))

        val builder = AlertDialog.Builder(this.context, R.style.CustomDialog)
        builder.setView(editGoalBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        editGoalBinding.dialogBack.setOnClickListener{

            alertDialog.dismiss()
        }

        editGoalBinding.BTNEdit.setOnClickListener {

            goal = editGoalBinding.ETGoal.text.toString().trim()
            if(goal.isEmpty()){
                editGoalBinding.ETGoal!!.error = "Please enter your new goal"
            }else{
                mDBRef.child("Users").child(mAuth.currentUser?.uid!!).child("readingChallenge").setValue(
                    AnnualReadingChallenge(goal, LocalDateTime.now().year.toString()))

                alertDialog.dismiss()
            }

        }

    }

    private var goal = ""

    @SuppressLint("NewApi")
    private fun showSetDialog() {

        val setGoalBinding = DialogSetGoalBinding.inflate(LayoutInflater.from(this.context))

        //setup alert dialog
        val builder = AlertDialog.Builder(this.context, R.style.CustomDialog)
        builder.setView(setGoalBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handle click, dismiss dialog
        setGoalBinding.dialogBack.setOnClickListener{

            alertDialog.dismiss()
        }

        setGoalBinding.BTNSave.setOnClickListener {

            goal = setGoalBinding.ETGoal.text.toString().trim()
            if(goal.isEmpty()){
                setGoalBinding.ETGoal!!.error = "Please enter your new goal"
            } else{
                mDBRef.child("Users").child(mAuth.currentUser?.uid!!).child("readingChallenge").setValue(
                    AnnualReadingChallenge(goal, LocalDateTime.now().year.toString()))

                alertDialog.dismiss()

                binding.RGFragmentUser.visibility = View.VISIBLE
                binding.RGFragmentGoal.visibility = View.VISIBLE
                binding.editGoalBtn.visibility = View.VISIBLE
                binding.pbGoal.visibility = View.VISIBLE
                binding.tvPercent.visibility = View.VISIBLE
                binding.RGFragmentLeft.visibility = View.VISIBLE
                binding.RGNoGoal.visibility = View.GONE
                binding.IVNoGoal.visibility = View.GONE


            }
            setGoalBinding.ETGoal.text.clear()

        }
    }

    private var shelfRead = ""

   private fun progressReadingChallenge() {
        var numBookRead = 0
        var numBookChallenge: String?
        var bookRead: ArrayList<BookInfo>? = null

       mDBRef.child("Users").child(mAuth.currentUser!!.uid).child("Bookshelves")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        val currentShelf = postSnapshot.getValue(ModelBookshelf::class.java)
                        if (currentShelf!!.getTitle() == "Read")
                            shelfRead = currentShelf!!.getID().toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        mDBRef.child("Users").child(mAuth.currentUser!!.uid).child("Bookshelves").child(shelfRead!!).child("Book")
            .addValueEventListener(object : ValueEventListener {
            @SuppressLint("NewApi")
            override fun onDataChange(snapshot: DataSnapshot) {

                    for (postSnapshot in snapshot.children) {
                        val currentBook = postSnapshot.getValue(BookInfo::class.java)
                        if (currentBook!!.getShelf() == shelfRead && currentBook.getReadDate()!!
                                .contains(LocalDateTime.now().year.toString())
                        ) {
                            bookRead?.add(currentBook!!)
                            numBookRead++
                        }
                    }

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReadingGoalFragment.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })

       val ref = FirebaseDatabase.getInstance().getReference("Users")
       ref.child(mAuth.uid!!)
           .addValueEventListener(object : ValueEventListener {
               @RequiresApi(Build.VERSION_CODES.O)
               override fun onDataChange(snapshot: DataSnapshot) {

                   val name = "${snapshot.child("name").value}"
                   binding.RGFragmentUser.text =
                       "$name\'s ${yearReadingChallenge} reading goal"
               }

               override fun onCancelled(error: DatabaseError) {
                   TODO("Not yet implemented")
               }
           })

                   mDBRef.child("Users").child(mAuth.currentUser?.uid!!).child("readingChallenge").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NewApi")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val challenge = snapshot.getValue(AnnualReadingChallenge::class.java)
                    numBookChallenge = challenge!!.getNum()
                    yearReadingChallenge = challenge!!.getYear()

                    if(challenge!!.getYear() == LocalDateTime.now().year.toString()) {
                        numBookChallenge = challenge!!.getNum()

                    binding.RGFragmentGoal.text =
                            "Your goal is to read $numBookChallenge books!"
                    if(numBookChallenge!!.toInt() > numBookRead){
                            val left = numBookChallenge!!.toInt() - numBookRead
                            binding.RGFragmentLeft.text =
                                "You have ${left} books left to complete your goal!"
                            val percentage = (numBookRead*100) / numBookChallenge!!.toInt()
                            binding.tvPercent.text = "$percentage%"
                        } else if(numBookChallenge!!.toInt() < numBookRead){
                            val left = numBookRead - numBookChallenge!!.toInt()
                            binding.RGFragmentLeft.text =
                                "Goal completed! \n You have read ${left} books more than expected!"
                            val percentage = 100
                            binding.tvPercent.text = "$percentage%"
                        } else{
                            binding.RGFragmentLeft.text =
                                "Goal completed! "
                            val percentage = 100
                            binding.tvPercent.text = "$percentage%"
                        }

                        binding.pbGoal.max = numBookChallenge!!.toInt()
                        binding.pbGoal.progress = numBookRead


                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReadingGoalFragment.requireContext(), "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
    }


}