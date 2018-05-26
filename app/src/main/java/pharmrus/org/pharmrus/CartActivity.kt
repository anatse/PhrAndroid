package pharmrus.org.pharmrus

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.View
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pharmrus.org.pharmrus.localdb.dao.CartRepository

class CartActivity : AppCompatActivity() {
    lateinit var cartRepo: CartRepository
    lateinit var recyclerView: RecyclerView
    lateinit var totalPrice: TextView

    /**
     * High order function variable: sets count in cart badge
     */
    private val setTotalPrice =  { total:Double ->
        totalPrice.text = total.money()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.cartRecView)
        totalPrice = findViewById(R.id.totalPrice)

        // Update data grid
        cartRepo = CartRepository(application)

        Observable.fromCallable {
            cartRepo.cartItems()
        }.subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe({
            result ->
            val viewManager = LinearLayoutManager(this)
            recyclerView.apply {
                layoutManager = viewManager
                setHasFixedSize(true)
                adapter = CartAdapter(result ?: listOf(), cartRepo, setTotalPrice)
            }
        }, {
            error -> Snackbar.make(recyclerView, "Error db: ${error}", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cart, menu)
        return true
    }
}
