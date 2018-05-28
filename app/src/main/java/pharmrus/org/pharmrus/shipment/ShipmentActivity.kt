package pharmrus.org.pharmrus.shipment

import android.app.ApplicationErrorReport
import android.support.design.widget.TabLayout

import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_shipment.*
import kotlinx.android.synthetic.main.fragment_shipment.*
import pharmrus.org.pharmrus.PharmrusService
import pharmrus.org.pharmrus.R
import pharmrus.org.pharmrus.RemoteCartItem
import pharmrus.org.pharmrus.RemoteCartRequest
import pharmrus.org.pharmrus.localdb.dao.CartRepository
import pharmrus.org.pharmrus.localdb.entity.CartStatus
import pharmrus.org.pharmrus.localdb.entity.UserSettings

class ShipmentActivity : AppCompatActivity() {
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private lateinit var cartRepo: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipment)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        cartRepo = CartRepository(application)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_shipment, menu)
        return true
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, cartRepo)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 2
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        private var userNameValid:Boolean = false
        private var userPhoneValid:Boolean = false
        private lateinit var userInfo:UserSettings

        private var cartRepo:CartRepository? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_shipment, container, false)

            userNameValid = false
            userPhoneValid = false

            val userNameView:TextView = rootView.findViewById(R.id.userName)

            userNameView.setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    userNameValid = !userName.text.isNullOrEmpty()
                    if (!userNameValid)
                        userName.error = resources.getString(R.string.user_name_error)
                    else
                        userName.error = null

                    sendOrderButton.isEnabled = userNameValid && userPhoneValid
                }
            }

            val userPhoneView:TextView = rootView.findViewById(R.id.userPhone)
            userPhoneView.setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    val pat = Regex("^\\+*[0-9]{11,13}\$")
                    val phone = userPhone.text.toString()

                    userPhoneValid = !phone.isNullOrEmpty() && !pat.matchEntire(phone)?.value.isNullOrEmpty()
                    if (!userPhoneValid)
                        userPhone.error = resources.getString(R.string.user_phone_error)
                    else
                        userPhone.error = null

                    sendOrderButton.isEnabled = userNameValid && userPhoneValid
                }
            }

            cartRepo?.let { cr ->
                // Trying to get User Information
                cr.getUserInfo().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        userInfo = it
                        userName.setText(userInfo.contact, TextView.BufferType.EDITABLE)
                        userPhone.setText(userInfo.phone, TextView.BufferType.EDITABLE)
                        sendOrderButton.isEnabled = (!userInfo.contact.isNullOrEmpty() && !userInfo.phone.isNullOrEmpty())

                    }, { error ->
                        Log.w("loadData", error.toString(), error)
                    }
                )
            }

            val cartSendButton: Button = rootView.findViewById(R.id.sendOrderButton)
            cartSendButton.setOnClickListener {
                // First of all disable button
                sendOrderButton.isEnabled = false
                sendOrderButton.text = resources.getString(R.string.sending)

                cartRepo?.let { cr ->
                    Observable.fromCallable {
                        cr.cartItems()
                    }.flatMap {
                        Observable.fromCallable {
                            cr.updateUserInfo(userInfo.copy(
                                phone = userPhone.text.toString(),
                                mail = userMail.text.toString(),
                                contact = userName.text.toString()
                            ))

                            it
                        }
                    }.flatMap { cartItems ->
                        val rItems = cartItems.map {
                            RemoteCartItem(
                                drugId = it.id,
                                drugName = it.drugsFullName,
                                producer = it.producerShortName,
                                price = it.price,
                                num = it.countInCart.toInt(),
                                availableOnStock = it.availableOnStock)
                        }

                        val rq = RemoteCartRequest(
                            userMail = userMail.text.toString(),
                            userName = userName.text.toString(),
                            userPhone = userPhone.text.toString(),
                            userUuid = userInfo.uuid,
                            items = rItems,
                            comment = orderComment.text.toString()
                        )

                        PharmrusService.service.sendCard(rq)
                    }.flatMap { result ->
                        cr.setCartStatus(result.orderNo, CartStatus.SENT)
                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ order ->
                            sendOrderButton.isEnabled = false
                            userName.isEnabled = false
                            userPhone.isEnabled = false
                            userMail.isEnabled = false
                            sendOrderButton.text = resources.getString(R.string.sent)

                            orderNoTitle.visibility = View.VISIBLE
                            orderNo.text = order
                            orderNo.visibility = View.VISIBLE

                        }, { error ->
                            sendOrderButton.isEnabled = true
                            sendOrderButton.text = resources.getString(R.string.cart_send)
                            Log.w("loadData", error.toString(), error)
                        })
                }
            }

            userNameView.isFocusableInTouchMode = true;
            userNameView.requestFocus();

            return rootView
        }

        companion object {
            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int, cartRepository: CartRepository): Fragment {
                return when (sectionNumber) {
                    1 -> {
                        val p = PlaceholderFragment()
                        p.cartRepo = cartRepository
                        p
                    }
                    2 -> BlankFragment()
                    else -> PlaceholderFragment()
                }

//                val fragment = PlaceholderFragment()
//                val args = Bundle()
//                args.putInt("aa", cartRepository)
//                fragment.arguments = args
//                return fragment
            }
        }
    }
}
