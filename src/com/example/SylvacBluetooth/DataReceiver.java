package com.example.SylvacBluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Patrick
 * Date: 25-Nov-15.
 * Description: receives the values from BleGattCallback to process inside the RecordActivity
 * Notes:
 */
public class DataReceiver extends BroadcastReceiver {

	private final String TAG = DataReceiver.class.getSimpleName();
	private final RecordActivity mParentActivity;
	private final String tab = "    ";
	private TextView mCurrentRecord;
	private TextView mCurrentEntryID;
	private ListView mHistory;
	private Button mAddRecord;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private int mIdCount = 1;
	private int mMeasurementCount = 0;
	private boolean AUTO_ADD = true;
	private int measurementsPerRecord = 3;
	private List<Entry> listRecords;
	private EntryAdapter listRecordsAdapter;

	private SharedPreferences mSharedPrefs;

	public DataReceiver(RecordActivity pRecordActivity){
		this.mParentActivity = pRecordActivity;
		mCurrentRecord = (TextView) mParentActivity.findViewById(R.id.textCurrentMeasurements);
		mCurrentEntryID = (TextView) mParentActivity.findViewById(R.id.textEntryID);
		listRecords = new ArrayList<>();
		listRecordsAdapter = new EntryAdapter(mParentActivity, R.layout.single_entry, listRecords);
		mHistory = (ListView) mParentActivity.findViewById(R.id.listHistory);

		mCurrentEntryID.setText(String.valueOf(mIdCount++));

		mHistory.setAdapter(listRecordsAdapter);
		listRecordsAdapter.notifyDataSetChanged();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String data = new String(intent.getByteArrayExtra("NUM_VALUE")).trim();
		switch (action){
			case "Data received":{
				if(mMeasurementCount >= measurementsPerRecord){
					// TODO
					// extract current record and insert into the history list
					// notify dataset changed...
					Entry newEntry = new Entry(mCurrentEntryID.getText().toString(), mCurrentRecord.getText().toString());
					listRecords.add(newEntry);
					listRecordsAdapter.notifyDataSetChanged();
					scrollList();

					Log.i(TAG, "'" + String.format("%1$-3s", String.valueOf(mIdCount)) + "'" + data + "'");
					mMeasurementCount = 0;
					mCurrentEntryID.setText(String.valueOf(mIdCount++));
					mCurrentRecord.setText("");
				}
				mCurrentRecord.append(data+"   ");
				mMeasurementCount++;
				break;
			}
			default:
				Log.i(TAG, "Action received: " + action);
		}
	}

	public boolean setAutoAdd(boolean setValue){
		this.AUTO_ADD = setValue;
		if(AUTO_ADD == setValue) return true;

		return false;
	}

	public void setNumMeasurements(int setValue){
		this.measurementsPerRecord = setValue;
	}

	public void setDefaultPreferences(SharedPreferences mSharedPrefs){
		mSharedPrefs.edit().putBoolean("auto_add", true).apply();
		mSharedPrefs.edit().putBoolean("enable_edit", true).apply();
		mSharedPrefs.edit().putInt("num_entries", 3).apply();

		AUTO_ADD = mSharedPrefs.getBoolean("auto_add", false);
		measurementsPerRecord = mSharedPrefs.getInt("num_entries", 0);
		//Log.i(TAG, "prefs set to " + mSharedPrefs.getBoolean("auto_add", false) + " and " + mSharedPrefs.getInt("num_entries", 0));
	}

	private void scrollList(){
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mHistory.setSelection(listRecords.size() - 1);
			}
		});
	}
}
