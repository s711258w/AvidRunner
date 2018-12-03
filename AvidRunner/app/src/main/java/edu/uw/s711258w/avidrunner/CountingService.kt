package edu.uw.s711258w.avidrunner

import android.app.IntentService
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.widget.Toast

/**
 * A service to count
 */
class CountingService : IntentService("CountingService") {

    private val TAG = "CountingService"

    private var count: Int = 0
    private var end: Boolean = false
    private lateinit var mHandler: Handler

    override fun onCreate() {
        mHandler = Handler()
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {


        count = 1
        while (!end) {
            Log.v(TAG, "Count: $count")

            mHandler.post {
                Toast.makeText(this@CountingService, "Count: $count", Toast.LENGTH_SHORT).show()
                Log.v(TAG, "" + count)
            }

            try {
                Thread.sleep(1000) //sleep for 2 seconds
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            count++
        }
    }

    override fun onDestroy() {
        end = true
        super.onDestroy()
    }
}