package pharmrus.org.pharmrus

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import pharmrus.org.pharmrus.localdb.dao.CartRepository
import pharmrus.org.pharmrus.prefs.SettingsActivity
import android.widget.TextView
import android.util.Log
import kotlinx.android.synthetic.main.cart_item_view.view.*

/**
 * Add glide functional for download images
 */
@GlideModule
class MyAppGlideModule : AppGlideModule()

const val QUERY_KEY = "com.pharmrus.query"
const val USER_UUID_KEY = "com.pharmrus.userUuid"
const val MY_PERMISSIONS_REQUEST_CALL_PHONE = 1001

class MainActivity : AppCompatActivity() {
    lateinit var searchDrugs: SearchView
    lateinit var cartCounter:TextView
    lateinit var recyclerView: RecyclerView
    lateinit var cartRepo: CartRepository
    lateinit var queryString: String

//    val androidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
//    var selectedColor: Int = -1
//    lateinit var selectedView: View

    private var disposable: Disposable? = null

    /**
     * Here we store information about current query
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(QUERY_KEY, queryString)
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    /**
     * High order function variable: sets count in cart badge
     */
    private val setBadgeCount =  { count:Int ->
        if (count == 0) {
            cartCounter.visibility = View.GONE
        }
        else {
            cartCounter.visibility = View.VISIBLE
        }

        cartCounter.text = count.toString()
    }

    /**
     * Function makes request for drugs and pass result into main view
     */
    private fun requestDrugs (query: String) {
        queryString = query
        val call = SearchService.service.search(SearchRequest(-1, 0, 100, query))
        disposable?.dispose()
        disposable = call.switchMap {
                // Merge with count in cart from local database
                result -> Observable.fromCallable {
                    val items = cartRepo.cartItems()
                    val res = result.copy(rows = result.rows.map {it.copy(countInCart = items?.find { item -> item.id == it.product.id }?.countInCart ?: 0.0)})
                    Pair(res, items)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                (result, cartItems) ->
                    val viewManager = LinearLayoutManager(this)
                    recyclerView.apply {
                        layoutManager = viewManager
                        setHasFixedSize(true)
                        adapter = ProductAdapter(result, cartRepo, setBadgeCount)
                    }

                    // Set initial count for cart badge
                    val totalCount = cartItems?.fold (0.0, { acc, cartItem ->  acc + cartItem.countInCart}) ?: 0.0
                    setBadgeCount.invoke(totalCount.toInt())
            }, {
                error -> Snackbar.make(recyclerView, "Error occured when load from network: ${error}", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

                Log.w("loadData", error.toString(), error)

                setBadgeCount.invoke(0)
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        /*
         * Call button. In android API >= 23 its needs to ask user for permission therefore this function so long
         */
        callPharmacy.setOnClickListener { view ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                    Snackbar.make(view, "Need info", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), MY_PERMISSIONS_REQUEST_CALL_PHONE)
                }
            } else {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:" + resources.getString(R.string.pharmacy_phone))

                // Make call
                try {
                    startActivity(intent)
                } catch (ex: SecurityException) {
                    Snackbar.make(view, "Insufficient privileges to make call", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                }
            }
        }

        recyclerView = findViewById(R.id.productRecView)
        cartRepo = CartRepository(application)

        // First fill drugs list (recommended or from saved state)
        requestDrugs(savedInstanceState?.getString(QUERY_KEY) ?: "")
    }

    /**
     * Setups cart menu item with badge and action
     */
    private fun setupCartAction (menu: Menu) {
        val cartActionItem = menu.findItem(R.id.cartAction)
        val cartActionView = cartActionItem.actionView
        cartCounter = cartActionView.findViewById(R.id.cart_badge)

        cartActionView.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Function creates menu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        setupCartAction(menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.searchDrugs)

        searchDrugs = searchItem.actionView as SearchView
        searchDrugs.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchDrugs.isIconified = true

        // Get the search close button image view
        val searchCloseButtonId = searchDrugs.context.resources.getIdentifier("android:id/search_close_btn", null, null);
        val closeButton = searchDrugs.findViewById(searchCloseButtonId) as ImageView? ?: searchDrugs.findViewById(R.id.search_close_btn) as ImageView?
        closeButton?.setOnClickListener{
            if (queryString.isEmpty() == false) {
                queryString = ""
                doSearch(queryString)
                searchDrugs.setQuery(queryString, false)
            }
        }

        if (!searchDrugs.query.toString().equals(queryString, true))
            searchDrugs.setQuery(queryString, false)

        val settings = menu.findItem(R.id.action_settings)
        settings.setOnMenuItemClickListener {
            val intent = Intent(this, SettingsActivity::class.java).apply {
                putExtra(QUERY_KEY, queryString)
            }
            startActivity(intent)
             true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * In debug mode using emulator this method would be called twice. Somebody thinks this is seems like a emulator bug
     * TODO check if this method called twice on android device
     * https://stackoverflow.com/questions/15633282/searchable-activity-being-called-twice?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     */
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val newSearch = intent.getStringExtra(SearchManager.QUERY)
            queryString = newSearch
            if (!searchDrugs.query.toString().equals(queryString, true))
                searchDrugs.setQuery(queryString, false);

            doSearch(queryString)
        }
    }

    private fun doSearch(query: String?): Boolean {
        requestDrugs(query?.toString() ?: "")
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CALL_PHONE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    val intent = Intent(Intent.ACTION_CALL)
                    intent.data = Uri.parse("tel:" + resources.getString(R.string.pharmacy_phone))

                    // Make call
                    try {
                        startActivity(intent)
                    } catch (ex:SecurityException) {
                        Log.e("error", "When trying to call", ex)
                    }

                } else {
                    Snackbar.make(recyclerView, "Permission denied for make calls", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                    Log.w("warning", "Permission denied for make calls")
                }

                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        // set actual count in cart
        requestDrugs(queryString)
    }
}
