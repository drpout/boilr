package com.github.andrefbsantos.boilr.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.andrefbsantos.boilr.R;
import com.github.andrefbsantos.libpricealarm.Alarm;
import com.github.andrefbsantos.libpricealarm.PriceHitAlarm;
import com.github.andrefbsantos.libpricealarm.PriceVarAlarm;

public class AlarmListAdapter extends ArrayAdapter<Alarm> {

	private Context context;
	private List<Alarm> alarms;

	public AlarmListAdapter(Context context, int textViewResourceId, List<Alarm> alarms) {
		super(context, textViewResourceId, alarms);
		this.context = context;
		this.alarms = alarms;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Alarm alarm = alarms.get(position);

		if (alarm instanceof PriceHitAlarm) {
			View rowView = inflater.inflate(R.layout.price_hit_alarm_row, viewGroup, false);

			TextView exchange = (TextView) rowView.findViewById(R.id.exchange);
			exchange.setText(alarm.getExchange().getClass().getSimpleName());

			TextView lastCheck = (TextView) rowView.findViewById(R.id.last_check);
			// lastCheck.setText(alarm.get)

			TextView lastValue = (TextView) rowView.findViewById(R.id.last_value);
			lastValue.setText("" + alarm.getLastValue());

			TextView pair = (TextView) rowView.findViewById(R.id.pair);
			pair.setText(alarm.getPair().toString());

			TextView upperBound = (TextView) rowView.findViewById(R.id.upper_bound);
			upperBound.setText("" + ((PriceHitAlarm) alarm).getLowerBound());

			TextView lowerBound = (TextView) rowView.findViewById(R.id.lower_bound);
			lowerBound.setText("" + ((PriceHitAlarm) alarm).getLowerBound());

			return rowView;

		} else if (alarm instanceof PriceVarAlarm) {
			View rowView = inflater.inflate(R.layout.price_var_alarm_row, viewGroup, false);
			TextView pair = (TextView) rowView.findViewById(R.id.pair);
			pair.setText(alarms.get(position).getPair().toString());
			return rowView;
		}
		return null;
	}
}