package com.example.SylvacBluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.preference.SwitchPreference;

/**
 * Author: Patrick
 * Date: 27-Nov-15.
 * Description:
 * Notes:
 */
public class SetRecordPreferenceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new RecordPreference()).commit();
	}
}
