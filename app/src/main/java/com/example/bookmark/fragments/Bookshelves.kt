package com.example.bookmark.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmark.Communicator
import com.example.bookmark.bookshelf.BookshelfAdapter
import com.example.bookmark.Home
import com.example.bookmark.R
import com.example.bookmark.bookshelf.ModelBookshelf
import com.example.bookmark.databinding.DialogBookshelfNewBinding
import com.example.bookmark.databinding.FragmentBookshelvesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Bookshelves : Fragment() {

    lateinit var binding : FragmentBookshelvesBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var showBookshelvesRecyclerView: RecyclerView
    private lateinit var adapterBookshelf: BookshelfAdapter
    private  var bookshelfArrayList :ArrayList<ModelBookshelf>? = null
    private lateinit var communicator: Communicator



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookshelfArrayList = ArrayList()
        firebaseAuth = FirebaseAuth.getInstance()
        adapterBookshelf = BookshelfAdapter(this@Bookshelves.requireContext(), bookshelfArrayList!!)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookshelvesBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        showBookshelvesRecyclerView = binding.RVBookshelves
        showBookshelvesRecyclerView.layoutManager = LinearLayoutManager(activity as Home, RecyclerView.VERTICAL, false)
        showBookshelvesRecyclerView.adapter = adapterBookshelf



        loadBookshelves()

        binding.BTNNewBookshelf.setOnClickListener{
            //show dialog_bookshelf_new
            newBookshelfDialog()

        }



        return view
    }

    private var title = ""

    private fun newBookshelfDialog() {

        val bookshelfAddBinding = DialogBookshelfNewBinding.inflate(LayoutInflater.from(this.context))

        //setup alert dialog
        val builder = AlertDialog.Builder(this.context, R.style.CustomDialog)
        builder.setView(bookshelfAddBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handle click, dismiss dialog
        bookshelfAddBinding.dialogBack.setOnClickListener{

            alertDialog.dismiss()
        }

        //handle click, new
        bookshelfAddBinding.BTNCreate.setOnClickListener{
            //get data
            title = bookshelfAddBinding.ETBookshelfTitle.text.toString().trim()
            //validate data
            if(title.isEmpty()){
                Toast.makeText(activity as Home, "Enter title", Toast.LENGTH_SHORT).show()

            } else{
                alertDialog.dismiss()
                createNewBookshelf()
            }

        }

    }

    private fun createNewBookshelf() {


        val timestamp = System.currentTimeMillis()
        //setup data to add to firebase
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp" // in string quotes because timestamp is in double, we need in string for id
        hashMap["title"] = title
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to firebase
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid.toString()).child("Bookshelves").child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(activity as Home, "Created successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                Toast.makeText(activity as Home, "Failed to create due to ${e.message}", Toast.LENGTH_SHORT).show()


            }
            }


    private fun loadBookshelves() {
        bookshelfArrayList = ArrayList()
        communicator = activity as Communicator

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid.toString()).child("Bookshelves")
            .addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                bookshelfArrayList!!.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelBookshelf::class.java)

                    bookshelfArrayList!!.add(model!!)
                }

                adapterBookshelf = BookshelfAdapter(this@Bookshelves.requireContext(), bookshelfArrayList!!)

                binding.RVBookshelves.adapter = adapterBookshelf

                adapterBookshelf.setOnItemClickListener(object : BookshelfAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {

                        communicator.passShelf(
                            bookshelfArrayList!![position].getID(), bookshelfArrayList!![position].getTitle(),
                            bookshelfArrayList!![position].getTimestamp(),bookshelfArrayList!![position].getUid()
                        )

                        title = bookshelfArrayList!![position].getTitle().toString()
                        Toast.makeText(this@Bookshelves.context, "Bookshelf $title clicked", Toast.LENGTH_SHORT).show()



                    }
                })
                adapterBookshelf.notifyDataSetChanged()


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }


}