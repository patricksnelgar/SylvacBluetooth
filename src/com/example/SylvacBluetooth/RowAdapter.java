package com.example.SylvacBluetooth;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Author: Patrick
 * Date: 27-Nov-15.
 * Description:
 * Notes:
 */
public class RowAdapter extends ArrayAdapter<String> {

	private int layout_id;
	private Context context;
	private List<String> data;

	public RowAdapter(Context context, int resource, List<String> data) {
		super(context, resource, data);
		this.context = context;
		this.layout_id = resource;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

	}
}
