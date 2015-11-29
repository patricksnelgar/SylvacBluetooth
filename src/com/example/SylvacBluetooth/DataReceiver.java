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
	private ListView mHistory;
	private Button mAddRecord;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private int mIdCount = 9;
	private int mMeasurementCount = 0;
	private boolean AUTO_ADD = true;
	private int measurementsPerRecord = 0;
	private List<String> listRecords;
	private ArrayAdapter<String> listRecordsAdapter;

	private SharedPreferences mSharedPrefs;

	public DataReceiver(RecordActivity pRecordActivity){
		this.mParentActivity = pRecordActivity;
		mCurrentRecord = (TextView) mParentActivity.findViewById(R.id.textCurrent);
		listRecords = new ArrayList<>();
		listRecordsAdapter = new ArrayAdapter<String>(mParentActivity, R.layout.data_entry, listRecords);
		mHistory = (ListView) mParentActivity.findViewById(R.id.listHistory);


		//mCurrentRecord.setText(String.valueOf(mIdCount++)+"  ");
		mCurrentRecord.setText("9      +0000.05    +0000.05    +0000.05\n" +
				               "10     +0000.05    +0000.05    +0000.05");
		mHistory.setAdapter(listRecordsAdapter);

		listRecords.add("9      +0000.05    +0000.05    +0000.05");
		listRecords.add("10     +0000.05    +0000.05    +0000.05");
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
					listRecords.add(mCurrentRecord.getText().toString());
					listRecordsAdapter.notifyDataSetChanged();

					scrollList();
					if(mIdCount < 10){
						mCurrentRecord.setText(String.valueOf(mIdCount++)+"  ");
					} else if(mIdCount < 100){
						mCurrentRecord.setText(String.valueOf(mIdCount++)+" ");
					} else {
						mCurrentRecord.setText(String.valueOf(mIdCount++));
					}


					Log.i(TAG, "'" + String.format("%1$-3s", String.valueOf(mIdCount)) + "'" + data + "'");
					mMeasurementCount = 0;
				}
				mCurrentRecord.append(String.format("%1$12s",data));
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
