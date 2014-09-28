package mobi.boilr.boilr.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceHitAlarm;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
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
		Alarm alarm = mAlarms.get(position);
		View rowView = null;

		if(alarm instanceof PriceHitAlarm) {
			rowView = mInflater.inflate(R.layout.price_hit_alarm_row, parent, false);
			PriceHitAlarm priceHitAlarm = (PriceHitAlarm) alarm;

			TextView upperBound = (TextView) rowView.findViewById(R.id.upper_bound);
			upperBound.setText(Conversions.formatEngNotation(priceHitAlarm.getUpperBound()));

			TextView lowerBound = (TextView) rowView.findViewById(R.id.lower_bound);
			lowerBound.setText(Conversions.formatEngNotation(priceHitAlarm.getLowerBound()));
		} else if(alarm instanceof PriceChangeAlarm) {
			rowView = mInflater.inflate(R.layout.price_change_alarm_row, parent, false);
			PriceChangeAlarm priceChangeAlarm = (PriceChangeAlarm) alarm;

			TextView periodTextView = (TextView) rowView.findViewById(R.id.period);
			periodTextView.setText(Conversions.formatMilis(priceChangeAlarm.getPeriod()));

			TextView change = (TextView) rowView.findViewById(R.id.change);
			if(priceChangeAlarm.isPercent()) {
				change.setText(Conversions.format2DecimalPlaces(priceChangeAlarm.getPercent()) + "%");
			} else {
				change.setText(Conversions.formatEngNotation(priceChangeAlarm.getChange()));
			}
		}

		ToggleButton toggleButton = (ToggleButton) rowView.findViewById(R.id.toggle_button);
		// hidden tag to identify the alarm
		toggleButton.setTag(alarm.getId());
		toggleButton.setChecked(alarm.isOn());

		if(alarm.isOn()) {
			rowView.setBackgroundColor(Color.TRANSPARENT);
		} else {
			rowView.setBackgroundColor(Color.DKGRAY);
		}

		TextView exchange = (TextView) rowView.findViewById(R.id.exchange);
		exchange.setText(alarm.getExchange().getName());

		TextView lastCheck = (TextView) rowView.findViewById(R.id.last_check);
		if (alarm.getLastUpdateTimestamp() != null) {
			lastCheck.setText(Conversions.formatMilis(System.currentTimeMillis() - alarm.getLastUpdateTimestamp().getTime()));
		}

		TextView pair = (TextView) rowView.findViewById(R.id.pair);
		pair.setText(alarm.getPair().toString());

		TextView lastValue = (TextView) rowView.findViewById(R.id.last_value);
		lastValue.setText(Conversions.formatEngNotation(alarm.getLastValue()));

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