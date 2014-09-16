package mobi.boilr.boilr.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import mobi.boilr.boilr.R;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import mobi.boilr.libpricealarm.PriceVarAlarm;

public class AlarmListAdapter extends ArrayAdapter<Alarm> {

	private Context context;
	private List<Alarm> alarms;

	public AlarmListAdapter(Context context, int textViewResourceId,
			List<Alarm> collection) {
		super(context, textViewResourceId, collection);
		this.context = context;
		alarms = collection;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Alarm alarm = alarms.get(position);
		View rowView = null;

		if(alarm instanceof PriceHitAlarm) {

			rowView = inflater.inflate(R.layout.price_hit_alarm_row, viewGroup, false);
			PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;

			TextView upperBound = (TextView) rowView.findViewById(R.id.upper_bound);
			upperBound.setText(String.valueOf(priceHitAlarm.getUpperBound()));

			TextView lowerBound = (TextView) rowView.findViewById(R.id.lower_bound);
			lowerBound.setText(String.valueOf(priceHitAlarm.getLowerBound()));

		} else if(alarm instanceof PriceVarAlarm) {

			rowView = inflater.inflate(R.layout.price_var_alarm_row, viewGroup, false);
			PriceVarAlarm priceVarAlarm = (PriceVarAlarm) alarm;

			TextView period = (TextView) rowView.findViewById(R.id.period);
			period.setText(String.valueOf(priceVarAlarm.getPeriod()));

			TextView variance = (TextView) rowView.findViewById(R.id.variance);

			if(priceVarAlarm.isPercent()) {
				variance.setText(String.valueOf(priceVarAlarm.getPercent()));
			} else {
				variance.setText(String.valueOf(priceVarAlarm.getVariation()));
			}

		}

		// hidden tag to identify the row where the button was clicked
		ToggleButton toggleButton = (ToggleButton) rowView.findViewById(R.id.toggle_button);
		toggleButton.setTag(alarm.getId());
		toggleButton.setChecked(alarm.isOn());

		TextView exchange = (TextView) rowView.findViewById(R.id.exchange);
		exchange.setText(alarm.getExchange().getName());

		TextView lastCheck = (TextView) rowView.findViewById(R.id.last_check);
		// lastCheck.setText(String.valueOf(alarm.getLastUpdateTimestamp()));

		TextView pair = (TextView) rowView.findViewById(R.id.pair);
		pair.setText(alarm.getPair().toString());

		TextView lastValue = (TextView) rowView.findViewById(R.id.last_value);
		// lastValue.setText(String.valueOf(alarm.getLastValue()));

		return rowView;

	}
}