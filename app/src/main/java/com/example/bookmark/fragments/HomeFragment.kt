package com.example.bookmark.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookmark.book.BookAdapter
import com.example.bookmark.Communicator
import com.example.bookmark.Home
import com.example.bookmark.book.BookInfo
import com.example.bookmark.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONException
import org.json.JSONObject



class HomeFragment : Fragment() {

    private var mRequestQueue: RequestQueue? = null
    private var bookInfoArrayList: ArrayList<BookInfo>? = null
    private var progressBar: ProgressBar? = null
    private var searchEdt: EditText? = null
    private var searchBtn: ImageButton? = null
    private lateinit var binding: FragmentHomeBinding
    private lateinit var searchBookRecyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var communicator: Communicator
    private lateinit var mAuth: FirebaseAuth
    private var shelf: String? = null
    private var uid: String? = null
    private var stars: String = "noStars"
    private var readDate: String = "noReadDate"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookInfoArrayList = ArrayList()
        adapter = BookAdapter(activity as Home, bookInfoArrayList!!)
        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser?.uid!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root
        communicator = activity as Communicator

        searchBookRecyclerView = binding.idRVBooks
        searchBookRecyclerView.layoutManager = LinearLayoutManager(activity as Home, RecyclerView.VERTICAL, false)
        searchBookRecyclerView.adapter = adapter


        progressBar = binding.idLoadingPB
        searchEdt = binding.idEdtSearchBooks
        searchBtn = binding.idBtnSearch

        searchBtn!!.setOnClickListener(View.OnClickListener {
            progressBar!!.visibility = View.VISIBLE

            for (i in 0 until bookInfoArrayList!!.size)
                bookInfoArrayList!!.removeLast()

            if (searchEdt!!.text.toString().isEmpty()) {
                searchEdt!!.error = "Please enter search query"
                return@OnClickListener
            }
            getBooksInfo(searchEdt!!.text.toString())
            adapter.notifyDataSetChanged()
        })

        adapter.setOnItemClickListener(object : BookAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                communicator.passSearchBook(
                    bookInfoArrayList!![position].getTitle(), bookInfoArrayList!![position].getSubtitle(), bookInfoArrayList!![position].getAuthors()!!.toString(),
                    bookInfoArrayList!![position].getPublisher(),  bookInfoArrayList!![position].getPublishedDate(),  bookInfoArrayList!![position].getDescription(),
                    bookInfoArrayList!![position].getPageCount(),  bookInfoArrayList!![position].getThumbnail(), "noShelf",
                    bookInfoArrayList!![position].getBid(), bookInfoArrayList!![position].getStars(), bookInfoArrayList!![position].getReadDate())
            }
        })
        adapter.notifyDataSetChanged()
        return view
    }


    private fun getBooksInfo(query: String) {
        mRequestQueue = Volley.newRequestQueue(activity as Home)
        mRequestQueue!!.cache.clear()
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query"
        val queue: RequestQueue = Volley.newRequestQueue(activity as Home)

        val booksObjrequest = JsonObjectRequest(Request.Method.GET, url, null, object: Response.Listener<JSONObject?> {
            override fun onResponse(response: JSONObject?) {
                progressBar!!.visibility = View.GONE
                try {
                    val itemsArray = response?.getJSONArray("items")
                    for (i in 0 until itemsArray!!.length()) {
                        val itemsObj = itemsArray.getJSONObject(i)
                        val volumeObj = itemsObj.getJSONObject("volumeInfo")
                        val title = volumeObj.optString("title")
                        val subtitle = volumeObj.optString("subtitle")
                        val authorsArray = volumeObj.getJSONArray("authors")
                        val publisher = volumeObj.optString("publisher")
                        val publishedDate = volumeObj.optString("publishedDate")
                        val description = volumeObj.optString("description")
                        val pageCount = volumeObj.optInt("pageCount")
                        val imageLinks = volumeObj.optJSONObject("imageLinks")
                        val thumbnail = imageLinks?.optString("thumbnail")
                        val bid = itemsObj.optString("id")
                        val authorsArrayList: ArrayList<String> = ArrayList()
                        if (authorsArray.length() != 0) {
                            for (j in 0 until authorsArray.length()) {
                                authorsArrayList.add(authorsArray.optString(i))
                            }
                        }
                        val bookInfo = BookInfo(
                            title,
                            subtitle,
                            authorsArrayList.toString(),
                            publisher,
                            publishedDate,
                            description,
                            pageCount.toString(),
                            thumbnail,
                            bid,
                            shelf,
                            stars,
                            uid,
                            readDate
                        )
                        bookInfoArrayList!!.add(bookInfo)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }, object : Response.ErrorListener {
            override fun onErrorResponse(error: VolleyError) {
                Toast.makeText(activity as Home, "An error occurred", Toast.LENGTH_SHORT)
                    .show()
            }
        })
        queue.add(booksObjrequest)
    }
}















