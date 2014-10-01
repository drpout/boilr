package mobi.boilr.boilr.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.widgets.PairListPreference;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PairListAdapter extends BaseAdapter implements OnClickListener,Filterable{

	private PairListPreference pairListPreference;
	private List<CharSequence> mPairs;
	private Context mContext;

	private LayoutInflater mInflater;

	private ArrayList<CharSequence> mOriginalPairs;
	private Filter mFilter;
	private final Object mLock = new Object();
	private int selectedItem = 0;
	protected CharSequence search = "Search...";

	private OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			PairListAdapter.this.getFilter().filter(v.getText());
			PairListAdapter.this.search = v.getText();
			return false;
		}
	};

	public PairListAdapter(Context context, CharSequence[] pairs, PairListPreference pairListPreference) {
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPairs = Arrays.asList(pairs);
		this.pairListPreference = pairListPreference;
		mOriginalPairs = new ArrayList<CharSequence>(Arrays.asList(pairs));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row;
		if(position == 0 ){
			row = mInflater.inflate(R.layout.pair_list_preference_search, parent, false);
			EditText editText = (EditText) row.findViewById(R.id.action_search_pair);
			editText.setText(search);
			editText.setOnEditorActionListener(onEditorActionListener);
			pairListPreference.getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			}else{
				CharSequence pair = mPairs.get(position-1);
				row = mInflater.inflate(R.layout.pair_list_preference_row, parent, false);
				row.setTag(position-1);
				row.setOnClickListener(this);

				TextView pairName = (TextView) row.findViewById(R.id.pair_name);
				pairName.setText(pair);
				RadioButton pairRadiobutton = (RadioButton) row.findViewById(R.id.pair_radio_button);
				if(this.pairListPreference.getValue().equals(String.valueOf(position-1))){
					pairRadiobutton.setChecked(true);
				}else{
					pairRadiobutton.setChecked(false);
				}

				pairRadiobutton.setClickable(false);
			}

			return row;
	}

	@Override
	public void onClick(View view) {
		TextView pairName = (TextView) view.findViewById(R.id.pair_name);
		String entryValue = String.valueOf(mOriginalPairs.indexOf(pairName.getText()));
		pairListPreference.setValue(entryValue);
		pairListPreference.getOnPreferenceChangeListener().onPreferenceChange(pairListPreference, entryValue);
		pairListPreference.getDialog().dismiss();
	}

	private class AlarmFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if(mOriginalPairs==null) {
				synchronized (mLock) {
					mOriginalPairs = new ArrayList<CharSequence>(mPairs);
				}
			}
			if(constraint == null || constraint.length() == 0) {
				ArrayList<CharSequence> list;
				synchronized (mLock) {
					list = new ArrayList<CharSequence>(mOriginalPairs);
				}
				results.values = list;
				results.count = list.size();
			} else {
				List<CharSequence> originalList;
				synchronized (mLock) {
					originalList = new ArrayList<CharSequence>(mOriginalPairs);
				}
				String[] filterStrings = constraint.toString().toLowerCase().split("\\s+");
				int filtersCount = filterStrings.length;
				List<CharSequence> newList = new ArrayList<CharSequence>();
				CharSequence filterableAlarm;
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
			mPairs = (List<CharSequence>) results.values;
			if(results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

	}

	public void add(CharSequence pair) {
		synchronized (mLock) {
			if(mOriginalPairs != null) {
				mOriginalPairs.add(pair);
			} else {
				mPairs.add(pair);
			}
		}
		notifyDataSetChanged();
	}

	public void addAll(Collection<? extends CharSequence> collection) {
		synchronized (mLock) {
			if(mOriginalPairs != null) {
				mOriginalPairs.addAll(collection);
			} else {
				mPairs.addAll(collection);
			}
		}
		notifyDataSetChanged();
	}

	public void remove(CharSequence pair) {
		synchronized (mLock) {
			if(mOriginalPairs != null) {
				mOriginalPairs.remove(pair);
			} else {
				mPairs.remove(pair);
			}
		}
		notifyDataSetChanged();
	}

	public void remove(int position) {
		synchronized (mLock) {
			if(mOriginalPairs != null) {
				mOriginalPairs.remove(position);
			} else {
				mPairs.remove(position);
			}
		}
		notifyDataSetChanged();
	}

	public void clear() {
		synchronized (mLock) {
			if(mOriginalPairs != null) {
				mOriginalPairs.clear();
			} else {
				mPairs.clear();
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mPairs.size() + 1;
	}

	@Override
	public CharSequence getItem(int position) {
		return mPairs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position+1;
	}

	public int getSelectedItem() {
		return selectedItem ; 
	}

	@Override
	public Filter getFilter() {
		if(mFilter == null) {
			mFilter = new AlarmFilter();
		}
		return mFilter;
	}
}
