package pharmrus.org.pharmrus

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.cart_item_view.view.*
import kotlinx.android.synthetic.main.product_item_view.view.*
import pharmrus.org.pharmrus.localdb.dao.CartRepository
import pharmrus.org.pharmrus.localdb.entity.CartItem

class CartViewHolder(val layout: ViewGroup) : RecyclerView.ViewHolder(layout)

class CartAdapter(private val dataSet: List<CartItem>, private val cartRepo: CartRepository, private val setTotalPrice:((price:Double) -> Unit)? = null) : RecyclerView.Adapter<CartViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_view, parent, false) as ViewGroup
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val row = dataSet[position]
        val resources = holder.layout.context.resources

        // Add drug title (full name)
        holder.layout.cartDrugFullName.text = row.drugsFullName
        // Producer
        holder.layout.cartDrugProducer.text = row.producerShortName
        // Availability on stock
        holder.layout.cartDrugOnStockAvailability.text = "${resources.getString(R.string.available_on_stock)}: ${if (row.availableOnStock > 0) resources.getString(R.string.yes) else resources.getString(R.string.no)}"
        // Price
        holder.layout.cartDrugPrice.text = "${row.price.money()}/${(row.price * row.countInCart).money()} ${resources.getString(R.string.currency)}"
        // Count in cart
        holder.layout.cartDrugCountInCart.text = row.countInCart.money()

        // Set total price
        setTotalPrice?.invoke(dataSet.fold (0.0, { acc, cartItem ->  acc + (cartItem.countInCart * cartItem.price)}))

        // Add listeners
        holder.layout.cartDrugAddToCart.setOnClickListener {
            cartRepo.addToCart(row).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    val item = result?.itemList?.find {it.id == row.id }
                    val countInCart = item?.countInCart?.toInt() ?: 0

                    holder.layout.cartDrugCountInCart.text = countInCart.toString()
                    holder.layout.cartDrugPrice.text = "${row.price.money()}/${(row.price * countInCart).money()} ${resources.getString(R.string.currency)}"

                    val totalCount = result?.itemList?.fold (0.0, { acc, cartItem ->  acc + (cartItem.countInCart * cartItem.price)})
                    setTotalPrice?.invoke(totalCount ?: 0.0)
                }, {
                    error -> Snackbar.make(it, "Error save to database: ${error}", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }
            )
        }

        // Add listeners
        holder.layout.cartDrugRemoveFromCart.setOnClickListener {
            cartRepo.removeFromCart(row.id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    val item = result?.itemList?.find {it.id == row.id }
                    val countInCart = item?.countInCart?.toInt() ?: 0

                    holder.layout.cartDrugCountInCart.text = countInCart.toString()
                    holder.layout.cartDrugPrice.text = "${row.price.money()}/${(row.price * countInCart).money()} ${resources.getString(R.string.currency)}"

                    val totalCount = result?.itemList?.fold (0.0, { acc, cartItem ->  acc + (cartItem.countInCart * cartItem.price)})
                    setTotalPrice?.invoke(totalCount ?: 0.0)
                }, { error ->
                    Snackbar.make(it, "Error save to database: ${error}", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                }
            )
        }
    }

}