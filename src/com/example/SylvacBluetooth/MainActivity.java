package com.example.SylvacBluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Author: Patrick
 * Date: 23-Nov-15
 * Description: Main class for the Slyvac Calipers application.
 * Notes: Handles the bluetooth adaptor, scanning and set up of a bond with Sylvac Calipers
 */
public class MainActivity extends Activity {

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int SCAN_PERIOD = 10000;
	BluetoothLeScanner btLeScanner;
	BluetoothManager btManager;
	BluetoothAdapter btAdapter;
	BluetoothGatt btGatt;
	Handler btHandler;
	BleGattCallback callback;
	BleReceiver bleReceiver;
	List<BluetoothDevice> discoveredDevices;
	List<String> discoveredDevicesString;
	ArrayAdapter<String> deviceListAdapter;
	ListView deviceListView;
	private TextView statusTextView;
	private TextView previuousTextView;
	private final String TAG = MainActivity.class.getSimpleName();
	private Button mRecordButton;

	final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	final UUID SERVICE_UUID = UUID.fromString("C1B25000-CAAF-6D0E-4C33-7DAE30052840");
	final UUID RX_CMD_TO_INSTRUMENT_UUID = UUID.fromString("C1B25012-CAAF-6D0E-4C33-7DAE30052840");
	final UUID TX_ANSWER_FROM_INSTRUMENT_UUID = UUID.fromString("C1B25013-CAAF-6D0E-4C33-7DAE30052840");
	final UUID TX_RECEIVED_DATA_UUID = UUID.fromString("C1B25010-CAAF-6D0E-4C33-7DAE30052840");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		btHandler = new Handler();
		discoveredDevices = new ArrayList<BluetoothDevice>();
		discoveredDevicesString = new ArrayList<String>();
		deviceListView = (ListView) findViewById(R.id.btDeviceList);
		statusTextView = (TextView) findViewById(R.id.textStatusValue);
		bleReceiver = new BleReceiver(this);
		mRecordButton = (Button) findViewById(R.id.buttonStartRecord);
		deviceListAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.single_device, discoveredDevicesString);
		btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		btAdapter = btManager.getAdapter();

		deviceListView.setAdapter(deviceListAdapter);
		deviceListView.setOnItemClickListener(selectDeviceListener);
		mRecordButton.setOnClickListener(mRecordButtonListener);

		LocalBroadcastManager.getInstance(this).registerReceiver(bleReceiver, makeGattUpdateIntentFilter());
		Log.i(TAG, "Adding bonded devices");
		for(BluetoothDevice bt : btAdapter.getBondedDevices()){
			discoveredDevices.add(bt);
			discoveredDevicesString.add(bt.getName()+ " " + bt.getAddress());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (btAdapter == null || !btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			Log.i(TAG, "Requesting enable bluetooth");
		} else {
			btLeScanner = btAdapter.getBluetoothLeScanner();
			scanDevices(true);
		}
	}

	@Override
	protected void onDestroy() {
		if (btGatt == null) {
			super.onDestroy();
			return;
		}
		btGatt.close();
		btGatt = null;
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "changed to state 'PAUSED'");
		if(btLeScanner != null)
			scanDevices(false);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "changed to state 'STOPPED'");
		if(btLeScanner != null)
			scanDevices(false);
	}

	private void scanDevices(final boolean enable){
		deviceListAdapter.notifyDataSetChanged();
		if(btAdapter.isEnabled()) {
			if (enable) {
				statusTextView.setText("Scanning for devices");
				Log.i(TAG, "Starting BluetoothLe scan");
				btLeScanner.startScan(null, new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback);
			} else {
				statusTextView.setText("Scanning stopped");
				Log.i(TAG, "Stopping BluetoothLe scan");
				btLeScanner.stopScan(scanCallback);
			}
		} else {
			statusTextView.setText("Bluetooth disabled");
		}
	}

	public void connectLeDevice(BluetoothDevice btDevice){
		scanDevices(false);
		statusTextView.setText("Connecting to device: " + btDevice.getName());
		this.callback = new BleGattCallback(this);
		Log.i(TAG,"Connecting to device " + btDevice.getName());
		btGatt = btDevice.connectGatt(this, false, this.callback);
	}

	public void enableNotification(BluetoothGatt gatt) {
		BluetoothGattService btService = gatt.getService(SERVICE_UUID);
		BluetoothGattCharacteristic btChar = btService.getCharacteristic(TX_ANSWER_FROM_INSTRUMENT_UUID);
		gatt.setCharacteristicNotification(btChar, true);

		BluetoothGattDescriptor btDes = btChar.getDescriptor(CCCD_UUID);
		btDes.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

		if(gatt.writeDescriptor(btDes)) Log.i(TAG, "Notification active!");
	}

	public void enableIndication(BluetoothGatt gatt) {
		// Enable indication
		BluetoothGattService btService = gatt.getService(SERVICE_UUID);
		BluetoothGattCharacteristic btChar = btService.getCharacteristic(TX_RECEIVED_DATA_UUID);
		gatt.setCharacteristicNotification(btChar,true);

		BluetoothGattDescriptor btDes = btChar.getDescriptor(CCCD_UUID);
		btDes.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);

		if(gatt.writeDescriptor(btDes)) Log.i(TAG, "Indication active!");
	}

	private static IntentFilter makeGattUpdateIntentFilter()
	{
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction("Connexion reussi");
		localIntentFilter.addAction("Deconnexion reussi ou inattendue");
		localIntentFilter.addAction("Services decouverts");
		localIntentFilter.addAction("Donnees transmises");
		return localIntentFilter;
	}

	final ScanCallback scanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			Log.i(TAG, "ScanResult: " + result.toString());
			if (!discoveredDevices.contains(result.getDevice())) {
				discoveredDevices.add(result.getDevice());
				discoveredDevicesString.add(result.getDevice().getName() + "  " + result.getDevice().getAddress());
				Log.i(TAG, "New device discovered! " + result.getDevice().getName());
			}
			deviceListAdapter.notifyDataSetChanged();
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			for(ScanResult singleResult : results)
				Log.i(TAG, "ScanResult - Results: " + singleResult.toString());
		}

		@Override
		public void onScanFailed(int errorCode) {
			statusTextView.setText("scanning failed :(");
			Log.e(TAG, "Scan failed with error code: " + errorCode);
		}
	};

	final AdapterView.OnItemClickListener selectDeviceListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
			BluetoothDevice btDevice = discoveredDevices.get(index);
			connectLeDevice(btDevice);
		}
	};

	final View.OnClickListener mRecordButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(new Intent(getBaseContext(), RecordActivity.class));
		}
	};
}
