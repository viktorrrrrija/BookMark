package com.example.bookmark

import android.app.AlertDialog
import android.content.Context
import android.graphics.ColorSpace.Model
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmark.bookshelf.ModelBookshelf
import com.example.bookmark.databinding.BookshelfItemBinding
import com.example.bookmark.databinding.BottomSheetItemBinding
import com.google.firebase.database.FirebaseDatabase

class SheetItemAdapter (val context: Context, val bookshelfArrayList: ArrayList<ModelBookshelf>) : RecyclerView.Adapter <SheetItemAdapter.HolderSheetItem> (){

    private lateinit var binding : BottomSheetItemBinding

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSheetItem {
        binding = BottomSheetItemBinding.inflate(LayoutInflater.from(context), parent, false)
        val view: View = binding.root
        return HolderSheetItem(view, mListener)
    }

    override fun getItemCount(): Int {
        return bookshelfArrayList.size // number of bookshelves
    }

    override fun onBindViewHolder(holder: HolderSheetItem, position: Int) {
        val model = bookshelfArrayList[position]
        val id = model.getID()
        val title = model.getTitle()
        val uid = model.getUid()
        val timestamp = model.getTimestamp()

        holder.titleTV.text = title


    }



    inner class HolderSheetItem(itemView: View,  listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        var titleTV: TextView = binding.sheetItem

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }

    }


}