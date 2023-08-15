package com.example.bookmark.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookmark.Home
import com.example.bookmark.R
import com.example.bookmark.SheetItemAdapter
import com.example.bookmark.book.BookInfo
import com.example.bookmark.bookshelf.BookshelfAdapter
import com.example.bookmark.bookshelf.ModelBookshelf
import com.example.bookmark.databinding.BottomSheetBinding
import com.example.bookmark.databinding.DialogReviewAddBinding
import com.example.bookmark.databinding.FragmentBookDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class BookDetails : Fragment() {

    var title: String? = null; var subtitle: String? = null; var publisher: String? = null; var publishedDate: String? = null
    var description: String? = null; var thumbnail: String? = null; var previewLink:  String? = null; var infoLink: String? = null
    var buyLink: String? = null; var pageCount: String? = null; private var authors: String? = null; var bid: String? = null; var readDate: String? = null
    private lateinit var binding: FragmentBookDetailsBinding
    var shelf: String? = null
    var shelfRead: String? = null
    var want: String? = null
    var newDate: String? = null
    private var stars: String = "noStars"
    private lateinit var mDBRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    var visible: Boolean = false
    private lateinit var dialog: BottomSheetDialog
    private var listAdd = ArrayList<ModelBookshelf>()
    private lateinit var adapterSheetItem: SheetItemAdapter
    private lateinit var addBookshelvesRecyclerView: RecyclerView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       mAuth = FirebaseAuth.getInstance()
        listAdd = ArrayList()
        adapterSheetItem = SheetItemAdapter(this@BookDetails.requireContext(), listAdd)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookDetailsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()


        mDBRef = FirebaseDatabase.getInstance().getReference()

        if(arguments?.getString("readDate")!= "noReadDate")
            readDate = arguments?.getString("readDate")
        else
            readDate = "noReadDate"

        /*if(shelf == null)
            shelf = "noShelf"
        if(bid == null)
            bid = "noBID"*/

        mDBRef.child("Users").child(mAuth.currentUser!!.uid).child("Bookshelves")
                            .addValueEventListener(object : ValueEventListener {
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

        if(arguments?.getString("shelf") == shelfRead){
            binding.RBBookDetail.visibility = View.VISIBLE
            binding.showCalendar.visibility = View.VISIBLE
            binding.calendarBtn.visibility = View.GONE
            binding.calendar.visibility = View.GONE
            binding.readDateTV.visibility = View.VISIBLE
            binding.TVRate.visibility = View.VISIBLE

        } else{
            binding.RBBookDetail.visibility = View.GONE
            binding.showCalendar.visibility = View.GONE
            binding.calendarBtn.visibility = View.GONE
            binding.calendar.visibility = View.GONE
            binding.readDateTV.visibility = View.GONE
            binding.TVRate.visibility = View.GONE
            binding.readDateTV.visibility = View.GONE
            binding.TVSetRating.visibility = View.GONE
        }




        binding.RBBookDetail.setOnRatingBarChangeListener { ratingBar, fl, b ->
            stars = binding.RBBookDetail.rating.toString()
        }

        binding.TVSetRating.setOnClickListener {
            if(binding.RBBookDetail.rating.toString() == "0.0")
                stars = "noStars"
            updateRating()

        }



        //if(readDate == "noReadDate")
        binding.showCalendar.setOnClickListener {
            if(readDate == "noReadDate")
            {
                binding.calendar.visibility = View.VISIBLE
                binding.calendarBtn.visibility = View.VISIBLE
                binding.showCalendar.text = "Add Read Date"
                binding.calendarBtn.text = "Add Read Date"
            }
           else{

                binding.calendar.visibility = View.VISIBLE
                binding.calendarBtn.visibility = View.VISIBLE
                binding.calendarBtn.text = "Edit Read Date"
                binding.showCalendar.text = "Edit Read Date"
           }

            binding.calendar.setOnDateChangeListener(object : CalendarView.OnDateChangeListener {
                override fun onSelectedDayChange(p0: CalendarView, p1: Int, p2: Int, p3: Int) {
                    var day = p3.toString()
                    var month = (p2+1).toString()
                    if(p3<=9)
                        day = "0" + p3
                    if(p2 <9)
                        month = "0" + (p2 + 1).toString()
                    newDate = "$day/$month/$p1"
                    binding.readDateTV.text = "READ DATE: $newDate"

                }
            })


        }

        binding.calendarBtn.setOnClickListener {


            if(checkDate(newDate)){
                updateDate()
                binding.calendar.visibility = View.GONE
                binding.calendarBtn.visibility = View.GONE

            } else
                Toast.makeText(this@BookDetails.requireContext(), "Insert a valid date!", Toast.LENGTH_SHORT)
                    .show()
        }


       // (activity as Home?)!!.setActionBarTitle(title)

        binding.btnAdd.setOnClickListener{

            showBottomSheet()
        }

        /*binding.IVAddReviewBtn.setOnClickListener{

            addReviewDialog()
        }*/

        title = arguments?.getString("title")
        subtitle = arguments?.getString("subtitle")
        publisher= arguments?.getString("publisher")
        publishedDate= arguments?.getString("publishedDate")
        description= arguments?.getString("description")
        thumbnail= arguments?.getString("thumbnail")
        previewLink= arguments?.getString("previewLink")
        infoLink= arguments?.getString("infoLink")
        buyLink= arguments?.getString("buyLink")
        pageCount= arguments?.getString("pageCount")
        authors= arguments?.getString("authors")
        bid = arguments?.getString("bid")
        shelf = arguments?.getString("shelf")
        readDate = arguments?.getString("readDate")



        binding.TVBookDetailTitle.setText(title)
        if(subtitle != "" && subtitle != null)
            binding.TVBookDetailSubtitle.setText(subtitle)
        else
            binding.TVBookDetailSubtitle.visibility = View.GONE
        if(publisher != "" && publisher != null)
            binding.idTVPublisher.setText("Publisher: " + publisher)
        else
            binding.idTVPublisher.visibility = View.GONE
        if(publishedDate != "" && publishedDate != null)
            binding.idTVPublishDate.setText("Published on: " + publishedDate)
        else
            binding.idTVPublishDate.visibility = View.GONE
        if(description != "" && description != null)
            binding.TVBookDetailDescription.setText("Description: \n" + description)
        else
            binding.TVBookDetailDescription.visibility = View.GONE
        if(pageCount != "" && pageCount != null)
            binding.idTVNoOfPages.setText("No Of Pages: " + pageCount)
        else
            binding.idTVNoOfPages.visibility = View.GONE
        if(authors != "[]" && authors != "[, ]" && authors != "[, , ]" && authors != "[, , , ]" && authors != "[, , , , ]")
            binding.TVBookDetailAuthor.setText("by " + authors?.replace("[", "")?.replace("]", ""))
        else
            binding.TVBookDetailAuthor.visibility = View.GONE
        if(stars != "noStars")
            binding.RBBookDetail.rating = stars?.toFloat()!!
        else
            binding.RBBookDetail.rating = 0F
        Picasso.get().load(thumbnail?.replace("http", "https")).placeholder(com.example.bookmark.R.drawable.logo1).noFade().into(binding.IVBookDetail)

        if(arguments?.getString("readDate")!= "noReadDate")
            readDate = arguments?.getString("readDate")
        else
            readDate = "noReadDate"

        if(readDate != "noReadDate")
            binding.readDateTV.text = "Read date: $readDate"
        else
            binding.readDateTV.text = "Read date: no read date available"




        return  view


    }

    private fun updateRating(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(mAuth.currentUser?.uid!!)
            .child("Bookshelves")
            .child(shelf!!)
            .child("Book")
            .child(bid!!)

        val update = HashMap<String, Any>()
        update["stars"] = stars.toString()

        ref.updateChildren(update)
            .addOnSuccessListener {
                Toast.makeText(this.requireContext(), "Rating modified!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{e->
                Toast.makeText(this.requireContext(), "Failed to modify rating due to $e.message", Toast.LENGTH_SHORT).show()

            }
    }

    private fun updateDate() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(mAuth.currentUser?.uid!!)
            .child("Bookshelves")
            .child(shelf!!)
            .child("Book")
            .child(bid!!)

        val update = HashMap<String, Any>()
        update["readDate"] = newDate.toString()

        ref.updateChildren(update)
            .addOnSuccessListener {
                Toast.makeText(this.requireContext(), "Date modified!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{e->
                Toast.makeText(this.requireContext(), "Failed to modify date due to $e.message", Toast.LENGTH_SHORT).show()

            }

    }

    private var review = ""

    private fun addReviewDialog() {
        val reviewAddBinding = DialogReviewAddBinding.inflate(LayoutInflater.from(activity as Home, ))

        val builder = AlertDialog.Builder(activity as Home, R.style.CustomDialog)
        builder.setView(reviewAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()

        reviewAddBinding.dialogBack.setOnClickListener { alertDialog.dismiss() }

        reviewAddBinding.BTNSubmit.setOnClickListener {
            //get data
            review = reviewAddBinding.ETDialogReview.text.toString().trim()

            //validate data
            if(review.isEmpty()){
                Toast.makeText(activity as Home, "Enter review", Toast.LENGTH_SHORT).show()

            } else{
                alertDialog.dismiss()
                addReview()
            }
        }
    }

    private fun addReview() {

        val timestamp = "${System.currentTimeMillis()}"


        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
       //hashMap["bookISBN"] = uid
        hashMap["timestamp"] = "$timestamp"
        hashMap["review"] = review

    }

    private var pos = ""
    private var titleShelf = ""

    private fun showBottomSheet() {

        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        dialog = BottomSheetDialog(activity as Home, R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)
        addBookshelvesRecyclerView = dialogView.findViewById(R.id.RVSheet)
        addBookshelvesRecyclerView.layoutManager = LinearLayoutManager(activity as Home, RecyclerView.VERTICAL, false)
        listAdd = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid.toString()).child("Bookshelves")
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listAdd.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelBookshelf::class.java)

                    listAdd.add(model!!)
                }

                adapterSheetItem = SheetItemAdapter(activity as Home, listAdd)

                addBookshelvesRecyclerView.adapter = adapterSheetItem

                adapterSheetItem.setOnItemClickListener(object : SheetItemAdapter.onItemClickListener{
                    override fun onItemClick(position: Int) {


                        titleShelf = listAdd[position].getTitle().toString()
                        pos = listAdd[position].getID().toString()


                        Toast.makeText(this@BookDetails.context, "Bookshelf $titleShelf clicked", Toast.LENGTH_SHORT).show()
                    }
                })
                adapterSheetItem.notifyDataSetChanged()

                //adapterBookshelf.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

        // var readId = ""

        val btnSave: TextView
        btnSave = dialogView.findViewById(R.id.TVSave)
        btnSave.setOnClickListener {

            val selectedId: String = pos
            addBookToDatabase(title, subtitle, publisher, publishedDate, description, thumbnail, pageCount, authors, bid, selectedId, "noStars", mAuth.currentUser?.uid!!, "noReadDate")

            dialog.dismiss()

        }


        dialog.show()

    }

    private fun addBookToDatabase(
        title: String?,
        subtitle: String?,
        publisher: String?,
        publishedDate: String?,
        description: String?,
        thumbnail: String?,
        pageCount: String?,
        authors: String?,
        bid: String?,
        shelf: String?,
        stars: String?,
        uid: String?,
        readDate: String?
    ) {

        mDBRef = FirebaseDatabase.getInstance().getReference()
        mDBRef.child("Users").child(uid!!).child("Bookshelves").child(shelf!!).child("Book").child(bid!!).setValue(
            BookInfo(title, subtitle, authors, publisher, publishedDate, description, pageCount, thumbnail, bid, shelf, stars, uid, readDate)
        )
    }

    override fun onStart(){
        super.onStart()
        if(stars != "noStars")
            binding.RBBookDetail.rating = stars?.toFloat()!!
        else
            binding.RBBookDetail.rating = 0F
    }

@RequiresApi(Build.VERSION_CODES.O)
private fun checkDate(readDate: String?): Boolean{
    if(readDate == "noReadDate")
        return true
    if(LocalDate.parse(readDate,  DateTimeFormatter.ofPattern("dd/MM/yyyy")).isAfter(LocalDate.now()))
        return false
    return true
}

}
