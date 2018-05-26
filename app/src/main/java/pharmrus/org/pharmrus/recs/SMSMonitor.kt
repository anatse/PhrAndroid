package pharmrus.org.pharmrus.recs

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log

class SMSMonitor(handler:Handler, private val context: Context) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)

        val uriMS = Uri.parse("content://sms")
        val cur = context.contentResolver.query(uriMS, null, null, null, null)
        cur?.let {
            val proto = cur.getString(cur.getColumnIndex("protocol"))
            if (cur.moveToFirst()) {
                if (proto == null) {
                    val type = cur.getInt(cur.getColumnIndex("type"))
                    if(type == 2){
                        Log.e("Info","Id : " + cur.getString(cur.getColumnIndex("_id")))
                        Log.e("Info","Thread Id : " + cur.getString(cur.getColumnIndex("thread_id")))
                        Log.e("Info","Address : " + cur.getString(cur.getColumnIndex("address")))
                        Log.e("Info","Person : " + cur.getString(cur.getColumnIndex("person")))
                        Log.e("Info","Date : " + cur.getLong(cur.getColumnIndex("date")))
                        Log.e("Info","Read : " + cur.getString(cur.getColumnIndex("read")))
                        Log.e("Info","Status : " + cur.getString(cur.getColumnIndex("status")))
                        Log.e("Info","Type : " + cur.getString(cur.getColumnIndex("type")))
                        Log.e("Info","Rep Path Present : " + cur.getString(cur.getColumnIndex("reply_path_present")))
                        Log.e("Info","Subject : " + cur.getString(cur.getColumnIndex("subject")))
                        Log.e("Info","Body : " + cur.getString(cur.getColumnIndex("body")))
                        Log.e("Info","Err Code : " + cur.getString(cur.getColumnIndex("error_code")))

                        val smsBodyStr = cur.getString(cur.getColumnIndex("body")).trim()
                        val phoneNoStr = cur.getString(cur.getColumnIndex("address")).trim()
                        val smsDatTime = cur.getLong(cur.getColumnIndex("date"))

                        Log.e("Info", "SMS Content : $smsBodyStr")
                        Log.e("Info", "SMS Phone No : $phoneNoStr")
                        Log.e("Info", "SMS Time : $smsDatTime")
                    }
                }
            }
        }

    }
}