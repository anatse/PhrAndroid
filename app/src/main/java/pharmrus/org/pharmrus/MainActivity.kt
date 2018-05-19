package pharmrus.org.pharmrus

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Add glide functional for download images
 */
@GlideModule
class MyAppGlideModule : AppGlideModule()

const val QUERY_KEY = "com.pharmrus.query"

class MainActivity : AppCompatActivity() {
    lateinit var searchDrugs: SearchView
    lateinit var recyclerView: RecyclerView

    var selectedColor: Int = -1
    lateinit var selectedView: View
    var disposable: Disposable? = null

    lateinit var queryString: String

    override fun onStart() {
        super.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(QUERY_KEY, queryString)
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    private fun requestDrugs (query: String) {
        queryString = query
        val call = SearchService.service.search(SearchRequest(-1, 0, 100, query))
        disposable = call.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                result ->
                val viewManager = LinearLayoutManager(this)
                recyclerView.apply {
                    layoutManager = viewManager
                    setHasFixedSize(true)
                    adapter = ProductAdapter(result)
                }
            }, {
                error -> Snackbar.make(recyclerView, "Error occured when load from network: ${error}", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            })
    }

    fun doSearch(query: String?): Boolean {
        requestDrugs(query?.toString() ?: "")
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        checkoutCart.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        recyclerView = findViewById(R.id.productRecView)

        // First fill drugs list (recommended or from saved state)
        requestDrugs(savedInstanceState?.getString(QUERY_KEY) ?: "")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.searchDrugs)

        searchDrugs = searchItem.getActionView() as SearchView
        searchDrugs.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchDrugs.setIconifiedByDefault(false)
        if (!searchDrugs.query.toString().equals(queryString, true))
            searchDrugs.setQuery(queryString, false);

        val settings = menu.findItem(R.id.action_settings)
        settings.setOnMenuItemClickListener {
            val intent = Intent(this, CartActivity::class.java).apply {
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

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            queryString = intent.getStringExtra(SearchManager.QUERY)
            if (!searchDrugs.query.toString().equals(queryString, true))
                searchDrugs.setQuery(queryString, false);

            doSearch(queryString)
        }
    }
}
