package com.example.bookmark

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.bookmark.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class Home : AppCompatActivity() , Communicator{

    private lateinit var bnBottomNav: BottomNavigationView
    private lateinit var frameFloat : FrameLayout
    private lateinit var fabUser : FloatingActionButton
    private lateinit var fabBooksh : FloatingActionButton
    var fabVisibility = false

    val userFragment = UserProfile()
    val userEditFragment = UserProfileEdit()
    val homeFragment = HomeFragment()
    val bookDetails = BookDetails()
    val bookshelvesFragment = Bookshelves()
    val singleBookshelf = SingleBookshelf()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bnBottomNav = findViewById(R.id.bnBottomNav)
        fabUser = findViewById(R.id.fab_User)
        fabBooksh = findViewById(R.id.fab_Bookshelves)

        fabVisibility = false

        val homeFragment = HomeFragment()
        val readingGoalFragment = ReadingGoalFragment()
        val userFragment = UserProfile()
        val userEditFragment = UserProfileEdit()

        supportFragmentManager.addOnBackStackChangedListener {
            val stackHeight = supportFragmentManager.backStackEntryCount
            if (stackHeight > 0) { // if we have something on the stack (doesn't include the current shown fragment)
                supportActionBar!!.setHomeButtonEnabled(true)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                supportActionBar!!.setHomeButtonEnabled(false)
            }
        }

        makeCurrentFragment(homeFragment)

        bnBottomNav.setOnItemSelectedListener {
            when(it.itemId)
            {
                R.id.home -> {
                    clearStack()
                    makeCurrentFragment(homeFragment)}

                R.id.reading_goal -> {
                    clearStack()
                    makeCurrentFragment(readingGoalFragment)}

                R.id.more -> { if (fabVisibility == false) {

                    // if its false we are displaying home fab
                    // and settings fab by changing their
                    // visibility to visible.
                    fabUser.show()
                    fabBooksh.show()

                    // on below line we are setting
                    // their visibility to visible.
                    fabUser.isVisible
                    fabBooksh.isVisible

                    // on below line we are changing
                    // fab visible to true
                    fabVisibility = true

                    fabUser.setOnClickListener{
                        Toast.makeText(this, "User button clicked", Toast.LENGTH_SHORT).show()
                        fabUser.hide()
                        fabBooksh.hide()
                        clearStack()
                        makeCurrentFragment(userFragment)
                    }

                    fabBooksh.setOnClickListener{
                        Toast.makeText(this, "User button clicked", Toast.LENGTH_SHORT).show()
                        fabUser.hide()
                        fabBooksh.hide()
                        clearStack()
                        makeCurrentFragment(bookshelvesFragment)
                    }
                } else {

                    fabUser.hide()
                    fabBooksh.hide()

                    // on below line we are changing the
                    // visibility of home and settings fab
                    fabUser.isGone
                    fabBooksh.isGone


                    // on below line we are changing
                    // fab visible to false.
                    fabVisibility = false
                }
                }


            }
            true
        }
    }

    fun clearStack(){
        val count: Int = supportFragmentManager.getBackStackEntryCount()
        for (i in 0 until count) {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
        private fun makeCurrentFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.flWrapper, fragment)
                commit()
            }
        }

    fun getUserFragment(): Fragment{
        return userFragment
    }

    fun getUserEditFragment(): Fragment{
        return userEditFragment
    }

    fun getHomeFragment() :Fragment{
        return homeFragment
    }

    fun getBookshelvesFragment() : Fragment {
        return bookshelvesFragment
    }

    override fun passShelf(
        id: String?,
        title: String?,
        timestamp: Long?,
        uid: String?
    ) {
        val bundle = Bundle()
        bundle.putString("id",id)
        bundle.putString("title", title)
        bundle.putString("timestamp", timestamp.toString())
        bundle.putString("uid", uid)

        singleBookshelf.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flWrapper, singleBookshelf)
            addToBackStack(null)
            commit()
        }

    }

    override fun passSearchBook(
        title: String?,
        subTitle: String?,
        authors: String?,
        publisher: String?,
        publishedDate: String?,
        description: String?,
        pageCount: String?,
        thumbnail: String?,
        shelf: String?,
        bid: String?,
        stars: String?,
        readDate: String?
    ) {
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("authors", authors)
        bundle.putString("subTitle", subTitle)
        bundle.putString("publisher", publisher)
        bundle.putString("publishedDate", publishedDate)
        bundle.putString("description", description)
        bundle.putString("pageCount", pageCount)
        bundle.putString("thumbnail", thumbnail)
        bundle.putString("shelf", shelf)
        bundle.putString("bid", bid)
        bundle.putString("stars", stars)
        bundle.putString("readDate", readDate)

        bookDetails.arguments = bundle
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flWrapper, bookDetails)
            addToBackStack(null)
            commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            supportFragmentManager.popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    }


