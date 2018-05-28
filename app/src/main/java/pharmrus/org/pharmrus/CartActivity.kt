package pharmrus.org.pharmrus

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_cart.*
import pharmrus.org.pharmrus.localdb.dao.CartRepository
import pharmrus.org.pharmrus.shipment.ShipmentActivity
import pharmrus.org.pharmrus.utils.CallUtility
import pharmrus.org.pharmrus.utils.MY_PERMISSIONS_REQUEST_CALL_PHONE

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

        callPharmacyCart.setOnClickListener { view ->
            CallUtility.callPharmacyListener(view, this)
        }

        Observable.fromCallable {
            cartRepo.cartItems()
        }.subscribeOn(Schedulers.io())
         .observeOn(AndroidSchedulers.mainThread())
         .subscribe({ result ->
             result?.let { cartItems ->
                 val viewManager = LinearLayoutManager(this)
                 recyclerView.apply {
                     layoutManager = viewManager
                     setHasFixedSize(true)
                     adapter = CartAdapter(cartItems, cartRepo, setTotalPrice)
                 }
             } ?: run {
                 finish()
             }
        }, {
            error ->
             Log.e("DBError", error.toString(), error)
             finish()
             Snackbar.make(recyclerView, "Error db: ${error}", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cart, menu)
        val shipmentItem = menu.findItem(R.id.cartShipmentAction)
        shipmentItem.setOnMenuItemClickListener { _ ->
            val intent = Intent(this, ShipmentActivity::class.java).apply {
                // TODO add additional info if applicable
            }
            startActivity(intent)
            true
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CALL_PHONE -> {
                CallUtility.processPermissionCallRequest(this, grantResults)
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
