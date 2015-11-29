package com.example.SylvacBluetooth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Author: Patrick
 * Date: 24-Nov-15.
 * Description: Broadcast Receiver class that handles the measurements sent by the Sylvac Calipers.
 * Notes:
 */
public class BleReceiver extends BroadcastReceiver {

	String TAG = BleReceiver.class.getSimpleName();
	private Activity parentActivity;

	public BleReceiver(Activity main){
		this.parentActivity = main;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String extra = (String)intent.getCharSequenceExtra("DEVICE_ADDRESS");
		Log.i(TAG, "Action received: " + action + " from: " + extra);
		switch (action){
			case "Donnees transmises":
				String extraData = new String(intent.getByteArrayExtra("EXTRA_DATA"));
				String canal = (String)intent.getCharSequenceExtra("NUM_CANAL");
				Log.i(TAG, "Extra data: " + extraData + " Canal: " + canal);
				break;
			default:
				break;
		}
	}
}
