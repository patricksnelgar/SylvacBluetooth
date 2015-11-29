package com.example.SylvacBluetooth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

/**
 * Author: Patrick
 * Date: 26-Nov-15.
 * Description:
 * Notes:
 */
public class RecordPreference extends PreferenceFragment {

	private final String TAG = RecordPreference.class.getSimpleName();
	private ListView mHistory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.record_preferences);
	}
}
