package com.example.SylvacBluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;

import java.util.List;

/**
 * Author: Patrick
 * Date: 23-Nov-15
 * Description: Gatt Callback for receiveing notifications from a Gatt bluetooth device
 * Notes:
 */
public class BleGattCallback extends BluetoothGattCallback {
	private final String TAG = BleGattCallback.class.getSimpleName();
	private MainActivity parentActivity;
	private Handler handler = new Handler(Looper.getMainLooper());
	private TextView statusView;


	public BleGattCallback(MainActivity pa){
		this.parentActivity = pa;
		statusView = (TextView) parentActivity.findViewById(R.id.textStatusValue);
	}

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		Log.i(TAG, "Connection state changed: " + status + " to " + newState);
		if(newState == 2) {
			if (status == 0) {
				Log.i(TAG, "Connection successful, starting service discovery");

				gatt.discoverServices(); //start service discovery
			}
		} else if(newState == 0){
			handler.post(new Runnable() {
				@Override
				public void run() {
					statusView.setText("Disconnected from device");
				}
			});
		}
	}

	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		List<BluetoothGattService> deviceServices = gatt.getServices();
		Log.i(TAG, "Services discovered: " + deviceServices.toString());
		parentActivity.enableIndication(gatt);
		parentActivity.enableNotification(gatt);
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		Log.i(TAG, "Characteristics Read: " + characteristic.getUuid());
		//gatt.disconnect();
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		//Log.i(TAG, "Characteristic changed: " + characteristic.getUuid());
		if(characteristic.getUuid().equals(parentActivity.TX_ANSWER_FROM_INSTRUMENT_UUID) || characteristic.getUuid().equals(parentActivity.TX_RECEIVED_DATA_UUID)){
			final byte[] data = characteristic.getValue();
			//Log.i(TAG, "Characterstic had value: " + new String(data));
			Intent intent = new Intent("Data received");
			intent.putExtra("NUM_VALUE", data);
			LocalBroadcastManager.getInstance(parentActivity).sendBroadcast(intent);
		}
	}

	@Override
	public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
		if(status == 0){
			Log.i(TAG, "Notification actually active");
			handler.post(new Runnable() {
				@Override
				public void run() {
					statusView.setText("Successfully connected to " + gatt.getDevice().getName());

				}
			});
		}
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		gatt.writeDescriptor(descriptor);
	}
}
