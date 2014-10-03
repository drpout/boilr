package mobi.boilr.boilr.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmListAdapter extends BaseAdapter implements Filterable {

	private List<Alarm> mAlarms;
	private List<Alarm> mOriginalAlarms;
	private Context mContext;
	private Filter mFilter;
	private LayoutInflater mInflater;
	private final Object mLock = new Object();

	public AlarmListAdapter(Context context, List<Alarm> alarms) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mAlarms = alarms;
	}

	@Override
	// TODO If needed optimize with http://www.piwai.info/android-adapter-good-practices
	public View getView(int position, View convertView, ViewGroup parent) {

		boolean isLandScape = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		Alarm alarm = mAlarms.get(position);
		View rowView = mInflater.inflate(R.layout.alarm_list_row, parent, false);

		ToggleButton toggleButton = (ToggleButton) rowView.findViewById(R.id.toggle_button);
		TextView exchange = (TextView) rowView.findViewById(R.id.exchange);
		TextView lastCheck = (TextView) rowView.findViewById(R.id.last_check);
		TextView pair = (TextView) rowView.findViewById(R.id.pair);
		TextView lastValue = (TextView) rowView.findViewById(R.id.last_value);

		LinearLayout linearLayout;
		String pairExchange = alarm.getPair().getExchange();
		if(isLandScape) {
			// Adjust Dimension to allow more content
			linearLayout = (LinearLayout) rowView.findViewById(R.id.exchange_layout);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.5f));
			linearLayout = (LinearLayout) rowView.findViewById(R.id.pair_layout);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
			linearLayout = (LinearLayout) rowView.findViewById(R.id.options_layout);
			linearLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2));
			//
			lastValue.setText(Conversions.formatMaxDecimalPlaces(alarm.getLastValue()) + " " + pairExchange);
		} else {
			lastValue.setText(Conversions.formatEngNotation(alarm.getLastValue()));
		}

		TextView changeUpperBound = (TextView) rowView.findViewById(R.id.change_upper_bound);
		TextView periodLowerBound = (TextView) rowView.findViewById(R.id.period_lower_bound);

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
			rowView.setBackgroundColor(Color.TRANSPARENT);
		} else {
			rowView.setBackgroundColor(Color.DKGRAY);
		}

		exchange.setText(alarm.getExchange().getName());

		if(alarm.getLastUpdateTimestamp() != null) {
			lastCheck.setText(Conversions.formatMilis(System.currentTimeMillis() - alarm.getLastUpdateTimestamp().getTime()));
		}

		pair.setText(alarm.getPair().toString());

		return rowView;
	}

	private class AlarmFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if(mOriginalAlarms == null) {
				synchronized (mLock) {
					mOriginalAlarms = new ArrayList<Alarm>(mAlarms);
				}
			}
			if(constraint == null || constraint.length() == 0) {
				ArrayList<Alarm> list;
				synchronized (mLock) {
					list = new ArrayList<Alarm>(mOriginalAlarms);
				}
				results.values = list;
				results.count = list.size();
			} else {
				List<Alarm> originalList;
				synchronized (mLock) {
					originalList = new ArrayList<Alarm>(mOriginalAlarms);
				}
				String[] filterStrings = constraint.toString().toLowerCase().split("\\s+");
				int filtersCount = filterStrings.length;
				List<Alarm> newList = new ArrayList<Alarm>();
				Alarm filterableAlarm;
				boolean containsAllFilters;
				int count = originalList.size();
				for (int i = 0; i < count; i++) {
					containsAllFilters = true;
					filterableAlarm = originalList.get(i);
					for (int j = 0; j < filtersCount; j++) {
						if(!filterableAlarm.toString().toLowerCase().contains(filterStrings[j])) {
							containsAllFilters = false;
							break;
						}
					}
					if(containsAllFilters)
						newList.add(filterableAlarm);
				}
				results.values = newList;
				results.count = newList.size();
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mAlarms = (List<Alarm>) results.values;
			if(results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

	}

	public void add(Alarm alarm) {
		synchronized (mLock) {
			if(mOriginalAlarms != null) {
				mOriginalAlarms.add(alarm);
			} else {
				mAlarms.add(alarm);
			}
		}
		notifyDataSetChanged();
	}

	public void addAll(Collection<? extends Alarm> collection) {
		synchronized (mLock) {
			if(mOriginalAlarms != null) {
				mOriginalAlarms.addAll(collection);
			} else {
				mAlarms.addAll(collection);
			}
		}
		notifyDataSetChanged();
	}

	public void remove(Alarm alarm) {
		synchronized (mLock) {
			if(mOriginalAlarms != null) {
				mOriginalAlarms.remove(alarm);
			} else {
				mAlarms.remove(alarm);
			}
		}
		notifyDataSetChanged();
	}

	public void remove(int position) {
		synchronized (mLock) {
			if(mOriginalAlarms != null) {
				mOriginalAlarms.remove(position);
			} else {
				mAlarms.remove(position);
			}
		}
		notifyDataSetChanged();
	}

	public void clear() {
		synchronized (mLock) {
			if(mOriginalAlarms != null) {
				mOriginalAlarms.clear();
			} else {
				mAlarms.clear();
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mAlarms.size();
	}

	@Override
	public Alarm getItem(int position) {
		return mAlarms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Filter getFilter() {
		if(mFilter == null) {
			mFilter = new AlarmFilter();
		}
		return mFilter;
	}
}