package com.example.bookmark.bookshelf

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmark.R
import com.example.bookmark.databinding.BookshelfItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BookshelfAdapter (val context: Context, val bookshelfArrayList: ArrayList<ModelBookshelf>): RecyclerView.Adapter <BookshelfAdapter.HolderBookshelves> (){



    private var mListener: onItemClickListener? = null

    private lateinit var binding : BookshelfItemBinding

    private lateinit var firebaseAuth: FirebaseAuth

    interface onItemClickListener {
        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBookshelves {
        binding = BookshelfItemBinding.inflate(LayoutInflater.from(context), parent, false)
        val view: View = binding.root

        return HolderBookshelves(view, mListener!!)
    }

    override fun getItemCount(): Int {
        return bookshelfArrayList.size // number of bookshelves
    }

    override fun onBindViewHolder(holder: HolderBookshelves, position: Int) {
        val model = bookshelfArrayList[position]
        val id = model.getID()
        val title = model.getTitle()
        val uid = model.getUid()
        val timestamp = model.getTimestamp()





        holder.titleTV.text = title
        //holder.count.text = bookshelfArrayList.size.toString()



        if((title == "Want To Read") || (title == "Read") || (title == "Currently Reading")){
            binding.mMenu.visibility = View.GONE

        }

        binding.mMenu.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this bookshelf?")
                .setPositiveButton("Confirm"){a, d->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteBookshelf(model, holder)
                }
                .setNegativeButton("Cancel"){a, d->
                        a.dismiss()
                }
                .show()
        }
    }

    private fun deleteBookshelf(model: ModelBookshelf, holder: HolderBookshelves) {

        firebaseAuth = FirebaseAuth.getInstance()
            //get id of bookshelf
        val id = model.getID()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid.toString()).child("Bookshelves").child(id!!)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Bookshelf deleted!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Unable to delete bookshelf due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

    }

    inner class HolderBookshelves(itemView: View, listener :onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        var titleTV: TextView = binding.TVBookshelfTitle
        val moreBtn : ImageView = binding.mMenu
        var count : TextView = binding.TVBookNumberBookshelf

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }





    }
}