package mobi.boilr.boilr.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.preference.PairListPreference;
import android.content.Context;
import android.drm.DrmStore.Action;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PairListAdapter extends ListAdapter<CharSequence> implements OnClickListener{

	private static final String SEARCH = "Search...";
	private PairListPreference pairListPreference;
	protected CharSequence search = SEARCH;

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			PairListAdapter.this.search = s;
			PairListAdapter.this.getFilter().filter(s);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	private OnTouchListener clearListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_UP){
				if(search.equals(SEARCH))
					((EditText) v).setText("");
				pairListPreference.getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				v.requestFocus();
			}
			return false;
		}
	};

	private View searchView;
	private final ArrayList<CharSequence> mFixedList;

	public PairListAdapter(Context context, List<CharSequence> pairs, PairListPreference pairListPreference) {
		super(context,pairs);
		this.mFixedList = new ArrayList<CharSequence>(pairs);
		this.pairListPreference = pairListPreference;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if( position == 0 ){
			convertView = searchView = getInflater().inflate(R.layout.pair_list_preference_search, parent, false);
			convertView.setTag(SEARCH);
			EditText editText = (EditText) convertView.findViewById(R.id.action_search_pair);
			editText.setText(search);
			editText.setOnTouchListener(clearListener);
			editText.addTextChangedListener(watcher);
			pairListPreference.getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			pairListPreference.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_‌​VISIBLE);
			if(!search.equals(SEARCH) && !search.equals("")){
				editText.requestFocus();
			}
			
		}else{
			if(convertView == null || SEARCH.equals(convertView.getTag())){
				convertView = getInflater().inflate(R.layout.pair_list_preference_row, parent, false);	
			}

			CharSequence pair = mList.get(position-1);
			convertView.setTag(position-1);
			convertView.setOnClickListener(this);
			TextView pairName = (TextView) convertView.findViewById(R.id.pair_name);
			pairName.setText(pair);
			RadioButton pairRadiobutton = (RadioButton) convertView.findViewById(R.id.pair_radio_button);
			if(this.pairListPreference.getValue().equals(String.valueOf(position-1))){
				pairRadiobutton.setChecked(true);
			}else{
				pairRadiobutton.setChecked(false);
			}
			pairRadiobutton.setClickable(false);
		}
		return convertView;
	}

	@Override
	public void onClick(View view) {
		TextView pairName = (TextView) view.findViewById(R.id.pair_name);
		String entryValue = String.valueOf(mFixedList.indexOf(pairName.getText()));
		pairListPreference.setValue(entryValue);
		pairListPreference.getOnPreferenceChangeListener().onPreferenceChange(pairListPreference, entryValue);
		pairListPreference.getDialog().dismiss();
	}

	@Override
	public void remove(int position) {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.remove(position+1);
			} else {
				mList.remove(position+1);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		//Used to determinate the number of rows to display
		return mList.size() + 1;
	}

	@Override
	public CharSequence getItem(int position) {
		return mList.get(position + 1);
	}

	@Override
	public long getItemId(int position) {
		return position - 1;
	}
}