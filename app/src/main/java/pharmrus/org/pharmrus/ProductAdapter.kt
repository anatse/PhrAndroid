package pharmrus.org.pharmrus

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.product_item_view.view.*
import java.text.NumberFormat

class ViewHolder(val layout: ViewGroup) : RecyclerView.ViewHolder(layout)

class ProductAdapter(private val dataSet: SearchResult) : RecyclerView.Adapter<ViewHolder>() {
    private fun createImage (rowData: SearchRow, imageView: ImageView) {
        // Load image from URL
        if (rowData.product.drugImage?.isEmpty() == false) {
            GlideApp
                .with(imageView.context)
                .load(rowData.product.drugImage)
                .fitCenter()
                .circleCrop()
                .error(R.drawable.abc_ic_clear_material)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item_view, parent, false) as ViewGroup
        return ViewHolder(view)
    }

    override fun getItemCount() = dataSet.rows.size

    /**
     * Function adds money function to Double type wit purpose to format any double values using currency for current locale
     */
    fun Double.money() = NumberFormat.getInstance().format(this)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = dataSet.rows[position]
        val product = row.product
        val resources = holder.layout.context.resources

        // Add image
        createImage(row, holder.layout.drugImage)

        // Add drug title (full name)
        holder.layout.drugFullName.text = product.drugsFullName
        // Producer
        holder.layout.drugProducer.text = product.producerShortName
        // MNN
        holder.layout.drugMnn.text = product.mnn
        // Availability on stock
        holder.layout.drugOnStockAvailability.text = "${resources.getString(R.string.available_on_stock)}: ${if (product.ost > 0) resources.getString(R.string.yes) else resources.getString(R.string.no)}"
        // Price
        holder.layout.drugPrice.text = "${product.retailPrice.money()} ${resources.getString(R.string.currency)}"
        // Count in cart
        holder.layout.drugCountInCart.text = row.countInCart.money()
    }
}