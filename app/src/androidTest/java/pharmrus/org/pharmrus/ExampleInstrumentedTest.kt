package pharmrus.org.pharmrus

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
//    val row = TableRow(this)
//    val lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
//    row.layoutParams = lp
//
//    val imageView = ImageView(this)
//    imageView.setImageResource(R.drawable.tank)
//    imageView.layoutParams = lp
//    imageView.layoutParams.width = 200
//    imageView.layoutParams.height = 200
//
//    val layout = LinearLayout(this)
//    layout.orientation = LinearLayout.VERTICAL
//    layout.setBackgroundColor(Color.BLUE)
//
//    val caption = TextView(this)
//    caption.text = "Caption: ${i}"
//    caption.setTextColor(Color.RED)
//    caption.textSize = 30F
//
//    val priceText = TextView (this)
//    priceText.text = "Price: ${i * 10}"
//    priceText.setTextColor(Color.YELLOW)
//    priceText.textSize = 80F
//
////            layout.addView(caption)
////            layout.addView(priceText)
//
//    row.addView(imageView)
////            row.addView(layout)
//    row.addView(priceText)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("pharmrus.org.pharmrus", appContext.packageName)
    }
}
