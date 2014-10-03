package mobi.boilr.boilr.widget;

import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmListAdapter extends ListAdapter<Alarm> {

	private static final int[] attrs = new int[] { R.attr.off_alarm_row_color /* index 0 */};

	public AlarmListAdapter(Context context, List<Alarm> alarms) {
		super(context,alarms);
	}

	@Override
	// TODO If needed optimize with http://www.piwai.info/android-adapter-good-practices
	public View getView(int position, View convertView, ViewGroup parent) {

		boolean isLandScape = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		Alarm alarm = mList.get(position);

		if(convertView == null)
			convertView = getInflater().inflate(R.layout.alarm_list_row, parent, false);

		ToggleButton toggleButton = (ToggleButton) convertView.findViewById(R.id.toggle_button);
		TextView exchange = (TextView) convertView.findViewById(R.id.exchange);
		TextView lastCheck = (TextView) convertView.findViewById(R.id.last_check);
		TextView pair = (TextView) convertView.findViewById(R.id.pair);
		TextView lastValue = (TextView) convertView.findViewById(R.id.last_value);

		LinearLayout linearLayout;
		String pairExchange = alarm.getPair().getExchange();
		if(isLandScape) {
			// Adjust Dimension to allow more content
			linearLayout = (LinearLayout) convertView.findViewById(R.id.exchange_layout);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.5f));
			linearLayout = (LinearLayout) convertView.findViewById(R.id.pair_layout);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
			linearLayout = (LinearLayout) convertView.findViewById(R.id.options_layout);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
			//
			lastValue.setText(Conversions.formatMaxDecimalPlaces(alarm.getLastValue()) + " " + pairExchange);
		} else {
			lastValue.setText(Conversions.formatEngNotation(alarm.getLastValue()));
		}

		TextView changeUpperBound = (TextView) convertView.findViewById(R.id.change_upper_bound);
		TextView periodLowerBound = (TextView) convertView.findViewById(R.id.period_lower_bound);

		if(alarm instanceof PriceHitAlarm) {
			PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;

			if(isLandScape) {
				// Display full number on landscape
				changeUpperBound.setText(Conversions.formatMaxDecimalPlaces(priceHitAlarm.getUpperBound()) + " " + pairExchange);
				periodLowerBound.setText(Conversions.formatMaxDecimalPlaces(priceHitAlarm.getLowerBound()) + " " + pairExchange);

			} else {
				changeUpperBound.setText(Conversions.formatEngNotation(priceHitAlarm.getUpperBound()));
				periodLowerBound.setText(Conversions.formatEngNotation(priceHitAlarm.getLowerBound()));
			}

		} else if(alarm instanceof PriceChangeAlarm) {
			PriceChangeAlarm priceChangeAlarm = (PriceChangeAlarm) alarm;

			if(priceChangeAlarm.isPercent()) {
				changeUpperBound.setText(Conversions.format2DecimalPlaces(priceChangeAlarm.getPercent()) + "%");
			} else if(isLandScape) {
				changeUpperBound.setText(Conversions.formatMaxDecimalPlaces(priceChangeAlarm.getChange()) + " " + pairExchange);
			} else {
				changeUpperBound.setText(Conversions.formatEngNotation(priceChangeAlarm.getChange()));
			}

			periodLowerBound.setText(Conversions.formatMilis(priceChangeAlarm.getPeriod()));
		}

		// hidden tag to identify the alarm
		toggleButton.setTag(alarm.getId());
		toggleButton.setChecked(alarm.isOn());

		if(alarm.isOn()) {
			convertView.setBackgroundColor(Color.TRANSPARENT);
		} else {
			TypedArray ta = getContext().obtainStyledAttributes(attrs);
			convertView.setBackgroundColor(ta.getColor(0, Color.DKGRAY));
			ta.recycle();
		}

		exchange.setText(alarm.getExchange().getName());

		if(alarm.getLastUpdateTimestamp() != null) {
			lastCheck.setText(Conversions.formatMilis(System.currentTimeMillis() - alarm.getLastUpdateTimestamp().getTime()));
		}

		pair.setText(alarm.getPair().toString());

		return convertView;
	}
}