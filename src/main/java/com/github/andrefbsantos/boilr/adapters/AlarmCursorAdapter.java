package com.github.andrefbsantos.boilr.adapters;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.boilr.database.Serializer;
import com.github.andrefbsantos.libpricealarm.Alarm;

public class AlarmCursorAdapter extends ResourceCursorAdapter {

	public AlarmCursorAdapter(Context context, int layout, Cursor c) {
		super(context, layout, c, ResourceCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		try {
			byte[] blob = cursor.getBlob(cursor.getColumnIndex("bytes"));
			Alarm object = (Alarm) Serializer.deserializeObject(blob);
			TextView pair = (TextView) view.findViewById(R.id.pair);
			pair.setText(object.getPair().toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
