package com.example.SylvacBluetooth;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;

/**
 * Author: Patrick
 * Date: 25-Nov-15.
 * Description:
 * Notes:
 */
public class RecordActivity extends Activity{

	private final String TAG = RecordActivity.class.getSimpleName();
	private Toolbar mToolbar;
	private ListView mHistoryWindow;
	private DataReceiver mReceiver;
	private final String PREFS_NAME = "RecordPrefs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_data);

		mReceiver = new DataReceiver(this);
		mHistoryWindow = (ListView) findViewById(R.id.listHistory);

		mHistoryWindow.setOnItemClickListener(mHistoryListener);
		mReceiver.setDefaultPreferences(getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, makeReceiverFilter());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.record_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem mItemClicked){
		if(mItemClicked.getItemId() == R.id.settings_preferences){
			Intent intent = new Intent();
			intent.setClass(RecordActivity.this, SetRecordPreferenceActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "Activity result code: " + resultCode);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private IntentFilter makeReceiverFilter(){
		IntentFilter _if = new IntentFilter();
		_if.addAction("Data received");
		_if.addAction("Test");
		return _if;
	}

	final AdapterView.OnItemClickListener mHistoryListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
			Log.i(TAG, "Index selected was " + index);
		}
	};
}
