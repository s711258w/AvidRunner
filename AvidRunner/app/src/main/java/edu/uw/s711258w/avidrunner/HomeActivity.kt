package edu.uw.s711258w.avidrunner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.*
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity: AppCompatActivity() {
    private val TAG = "HomeActivity"
    private val RUN_DETAIL_FRAGMENT_TAG = "RunDetailFragment"

    private lateinit var adapter: RunDataAdapter

    private val mContext: Context = this@HomeActivity

    private val runDataList: MutableList<RunData>  = mutableListOf()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        button_calendar.setOnClickListener {
            Log.v(TAG, "calendar clicked")
            startActivity(Intent(this, MainActivity::class.java))
        }
        button_music.setOnClickListener {
            Log.v(TAG, "music clicked")
            startActivity(Intent(this, PlaylistActivity::class.java))
        }
        button_run.setOnClickListener {
            Log.v(TAG, "run clicked")
            startActivity(Intent(this, MapsActivity::class.java))
        }

        var listView = list_run_history
        listView.setOnItemClickListener { parent, view, position, id ->
            val data = adapter.getItem(position)

            val detailFragment: RunDetailFragment = RunDetailFragment.newInstance(data)

            supportFragmentManager.beginTransaction().
                replace(R.id.home_content_container, detailFragment)
                    .addToBackStack(null).
                commit()


//            val intent = Intent(this, MapsActivity::class.java)
//            startActivity(intent)
        }

        // Initialize adapter
        adapter = RunDataAdapter(mContext, R.layout.item_history, runDataList)

        // Set Adapter for listview
        (listView as AdapterView<Adapter>).adapter = adapter

        // Load run data
        getRunData()
    }

    // Gets the run data
    // TODO: Need data from running activity (MapsActivity)
    fun getRunData() {
        runDataList.clear()
        adapter.notifyDataSetChanged()

        // TODO: Need data from running activity (MapsActivity) to be saved

        // using dummy data for now
        val list = getDummyData()
        for (item in list) {
            runDataList.add(item)
            adapter.notifyDataSetChanged()
        }
    }

    // generates a list of dummy run data
    fun getDummyData(): List<RunData> {
        val result = mutableListOf<RunData>()
        for(i in 0..30) {
            result.add(RunData("date:$i", "distance:$i", "time:$i", "routeData:$i"))
        }
        return result.toList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.preferences -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.playlist -> {
                startActivity(Intent(this, PlaylistActivity::class.java))
                true
            }
            R.id.start_run -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

open class RunDataAdapter(context: Context,
                          resource: Int,
                          runDataList: MutableList<RunData>): ArrayAdapter<RunData>(context, resource, runDataList){
    private val TAG: String = "RunDataAdapter"

    private lateinit var viewHolder: ViewHolder

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var inflatedView: View? = convertView
        if (inflatedView == null) {
            inflatedView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
        }

        viewHolder = ViewHolder(
            inflatedView!!.findViewById(R.id.text_history_date),
            inflatedView.findViewById(R.id.text_history_distance),
            inflatedView.findViewById(R.id.text_history_time)
        )

        inflatedView.tag = viewHolder


        // Get run data
        val runDataAtIndex: RunData = getItem(position)
        val runDate = runDataAtIndex.date
        val runDistance = runDataAtIndex.distance
        val runTime = runDataAtIndex.time

        // Set view holder fields to data
        viewHolder.textViewRunDate.text = runDate
        viewHolder.textViewRunDistance.text = runDistance
        viewHolder.textViewRunTime.text = runTime


        return inflatedView
    }

    private data class ViewHolder @JvmOverloads constructor(
        var textViewRunDate: TextView,
        var textViewRunDistance: TextView,
        var textViewRunTime: TextView
    )
}

data class RunData(val date: String, val distance: String, val time: String, val routeData: Any):Parcelable {
    constructor(parcel: Parcel): this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RunData> {
        override fun createFromParcel(parcel: Parcel): RunData {
            return RunData(parcel)
        }

        override fun newArray(size: Int): Array<RunData?> {
            return arrayOfNulls(size)
        }
    }

}
