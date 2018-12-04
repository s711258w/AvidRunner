package edu.uw.s711258w.avidrunner

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_run_detail.*
import kotlinx.android.synthetic.main.fragment_run_detail.view.*

class RunDetailFragment: Fragment() {
    private val TAG = "RunDetailFragment"

    companion object {
        val RUN_PARCEL_KEY = "run_parcel"

        fun newInstance(run: RunHistoryData): RunDetailFragment {
            val args = Bundle().apply {
                putParcelable(RUN_PARCEL_KEY, run)
            }
            val fragment = RunDetailFragment().apply {
                arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.v(TAG, "try to inflate")
        val rootView = inflater.inflate(R.layout.fragment_run_detail, container, false)

        Log.v(TAG, "inside oncreate")
        arguments?.let {
            val runData = it.getParcelable<RunHistoryData>(RUN_PARCEL_KEY)
            runData?.let {
                Log.v(TAG, "rundata date was ${runData.runDate}")
                rootView.text_detail_date.text = "Date: ${runData.runDate}"
                rootView.text_detail_distance.text = "Distance: ${runData.miles}"
                rootView.text_detail_time.text = "Time: ${runData.runTime}"
                rootView.text_detail_pace.text = "Pace: ${runData.milePace}"
            }
            Log.v(TAG, "Route data is ${runData.geoJSON}")
        }
        return rootView
    }
}