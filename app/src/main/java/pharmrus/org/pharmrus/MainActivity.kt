package pharmrus.org.pharmrus

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.BitmapFactory
import android.graphics.Point
import java.net.URL
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import android.widget.EditText
import com.bumptech.glide.load.engine.executor.GlideExecutor.UncaughtThrowableStrategy.LOG



/**
 * Add glide functional for download images
 */
@GlideModule
class MyAppGlideModule : AppGlideModule()

const val QUERY_KEY = "query"
const val IMAGE_SIZE = 200

class MainActivity : AppCompatActivity() {
//    lateinit var table: TableLayout
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

//    fun onTableSelection (view: View): Unit {
//        if (selectedColor != -1) {
//            selectedView.setBackgroundColor(selectedColor)
//            selectedView = view;
//            selectedColor = if (view.id % 2 == 0) Color.parseColor("#f0ccf0") else Color.parseColor("#f0f0f0")
//        }
//        else {
//            selectedView = view;
//            selectedColor = if (view.id % 2 == 0) Color.parseColor("#f0ccf0") else Color.parseColor("#f0f0f0")
//        }
//
//        view.setBackgroundColor(Color.CYAN)
//
//        Snackbar.make(view, "Выбран элемент: ${view.id}", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//    }
//
//    private fun createTableRow (rowData: SearchRow): TableRow {
//        val row = TableRow(this)
//        val lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
//        row.layoutParams = lp
//
//        row.setOnClickListener {view -> onTableSelection(view)}
//        val imageView = ImageView(this)
//
//        // Load image from URL
//        if (!rowData.product.drugImage?.isEmpty())
//            GlideApp
//                .with(this)
//                .load(rowData.product.drugImage)
//                .fitCenter()
//                .circleCrop()
//                .error(R.drawable.abc_ic_clear_material)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(imageView)
//
//        imageView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
//        imageView.layoutParams.width = IMAGE_SIZE
//        imageView.layoutParams.height = IMAGE_SIZE
//
//        val addToCard = ImageButton(this)
//        addToCard.setImageResource(android.R.drawable.btn_plus)
//
//        val delFromCard = ImageButton(this)
//        delFromCard.setImageResource(android.R.drawable.btn_minus)
//
//        // Vertical layout
//        val layout = LinearLayout(this)
//        layout.orientation = LinearLayout.VERTICAL
//
//        val point: Point = Point()
//        val display = windowManager.defaultDisplay
//        display.getSize(point)
//
//        // Drug full name as title
//        val fullNameText = TextView(this)
//        fullNameText.setTextColor(Color.BLACK)
//        fullNameText.textSize = 18F
//        fullNameText.text = rowData.product.drugsFullName
//        fullNameText.gravity = Gravity.LEFT
//        fullNameText.layoutParams = TableRow.LayoutParams(point.x - IMAGE_SIZE, TableRow.LayoutParams.WRAP_CONTENT)
//        layout.addView(fullNameText)
//
//        // Producer
//        val producerText = TextView(this)
//        producerText.setTextColor(Color.GRAY)
//        producerText.textSize = 14F
//        producerText.text = rowData.product.producerShortName
//        producerText.gravity = Gravity.LEFT
//        producerText.layoutParams = TableRow.LayoutParams(point.x - IMAGE_SIZE, TableRow.LayoutParams.WRAP_CONTENT)
//        producerText.maxWidth = point.x - 100
//        layout.addView(producerText)
//
//        // MNN
//        val mnnText = TextView(this)
//        mnnText.setTextColor(Color.GRAY)
//        mnnText.setTextSize(14F)
//        mnnText.setText(rowData.product.mnn)
//        mnnText.gravity = Gravity.LEFT
//        mnnText.layoutParams = TableRow.LayoutParams(point.x - IMAGE_SIZE, TableRow.LayoutParams.WRAP_CONTENT)
//        layout.addView(mnnText)
//
//        // Remains in stock
//        val existsInStok = TextView(this)
//        existsInStok.setTextColor(Color.GRAY)
//        existsInStok.setTextSize(14F)
//        existsInStok.setText("${resources.getString (R.string.available_on_stock)}: ${if (rowData.product.ost > 0) resources.getString(R.string.yes) else resources.getString(R.string.no) }")
//        existsInStok.gravity = Gravity.LEFT
//        layout.addView(existsInStok)
//
//        // Price
//        val priceText = TextView (this)
//        priceText.setTextColor(Color.BLUE)
//        priceText.setTextSize(18F)
//        priceText.setText("${resources.getString(R.string.price)}: ${rowData.product.retailPrice} ${resources.getString(R.string.currency)}")
//        priceText.gravity = Gravity.RIGHT
//        layout.addView(priceText)
//
//        row.addView(imageView)
//        row.addView(layout)
//
//        return row
//    }
//
//    private fun fillProductTable (result: SearchResult) {
//        for (rowData in result.rows) {
//            table.addView(createTableRow(rowData))
//        }
//    }

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
        //val query = intent.getStringExtra(SearchManager.QUERY)
//        table.removeAllViews()
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

        clearCart.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

//        table = findViewById(R.id.productTable)
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

//        searchDrugs.setOnQueryTextListener (object: SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                table.removeAllViews()
//                requestDrugs(query!!)
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                table.removeAllViews()
//                requestDrugs(newText!!)
//                return true
//            }
//        })

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
