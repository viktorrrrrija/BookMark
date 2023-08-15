package com.example.bookmark.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmark.Communicator
import com.example.bookmark.Home
import com.example.bookmark.R
import com.example.bookmark.SwipeToDeleteCallback
import com.example.bookmark.book.BookAdapter
import com.example.bookmark.book.BookInfo
import com.example.bookmark.bookshelf.BookshelfAdapter
import com.example.bookmark.bookshelf.ModelBookshelf
import com.example.bookmark.databinding.FragmentBookshelvesBinding
import com.example.bookmark.databinding.FragmentSingleBookshelfBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class SingleBookshelf : Fragment() {

    var shelfTitle: String? = null
    var shelfID: String? = null
    var shelfTimestamp: String? = null
    var shelfUid: String? = null

    lateinit var binding : FragmentSingleBookshelfBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var showBooksRecyclerView: RecyclerView
    private lateinit var adapterBook: BookAdapter
    private var bookArrayList :ArrayList<BookInfo>? = null
    private lateinit var communicator: Communicator
    private lateinit var mDBRef: DatabaseReference
    private var uid: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookArrayList = ArrayList()
        adapterBook = BookAdapter(this@SingleBookshelf.requireContext(), bookArrayList!!)
        mAuth =  FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        binding = FragmentSingleBookshelfBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        binding.icon.visibility = View.GONE

        showBooksRecyclerView = binding.RVBooks
        showBooksRecyclerView.layoutManager = LinearLayoutManager(this@SingleBookshelf.requireContext(), RecyclerView.VERTICAL, false)
        showBooksRecyclerView.adapter = adapterBook

        shelfTitle = arguments?.getString("title")
        shelfID = arguments?.getString("id")
        shelfTimestamp = arguments?.getString("timestamp")
        shelfUid = arguments?.getString("uid")

        binding.TVBookshelfTitle.setText(shelfTitle)

        communicator = activity as Communicator

        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("Users").child(mAuth.currentUser!!.uid).child("Bookshelves").child(shelfID!!).child("Book").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookArrayList?.clear()
                for (postSnapshot in snapshot.children){
                    val currentBook = postSnapshot.getValue(BookInfo::class.java)
                    if(currentBook!!.getShelf() == shelfID)
                        bookArrayList?.add(currentBook!!)
                }


                binding.TVNum.setText(bookArrayList!!.size.toString() + " books")

                if(bookArrayList!!.size == 0){

                     binding.icon.visibility = View.VISIBLE
                }

                adapterBook.setOnItemClickListener(object : BookAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {

                        communicator.passSearchBook(
                            bookArrayList!![position]?.getTitle(), bookArrayList!![position]?.getSubtitle(), bookArrayList!![position]?.getAuthors()!!.toString(),
                            bookArrayList!![position]?.getPublisher(),  bookArrayList!![position]?.getPublishedDate(),  bookArrayList!![position]?.getDescription(),
                            bookArrayList!![position]?.getPageCount(),  bookArrayList!![position]?.getThumbnail(), bookArrayList!![position]?.getShelf(),
                            bookArrayList!![position]?.getBid(), bookArrayList!![position]?.getStars(), bookArrayList!![position]?.getReadDate()

                        )
                    }
                })
                adapterBook.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity as Home, "An error occurred! Please retry.", Toast.LENGTH_SHORT)
            }
        })
        val swipeToDeleteCallback = object: SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val book = adapterBook.getBookAtPosition(position)

                mDBRef = FirebaseDatabase.getInstance().getReference()
                mDBRef.child("Users").child(mAuth.currentUser!!.uid).child("Bookshelves").child(book.getShelf()!!).child("Book").child(book.getBid()!!)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Book deleted!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Unable to delete book due to ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }



                    adapterBook.deleteBook(position)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)

        itemTouchHelper.attachToRecyclerView(showBooksRecyclerView)




        return view
    }

    /*override  fun deleteBook(position : Int) {
        mAuth = FirebaseAuth.getInstance()
        //get id of bookshelf

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid.toString()).child("Bookshelves").child(shelfID!!).child("Books").child(
            position!!.toString()
        )
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Book deleted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Unable to delete book due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }*/


}