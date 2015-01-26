package mobi.boilr.boilr.widget;

import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.CTX_RESTRICT_SCOPE;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.preference.SearchableListPreference;
import mobi.boilr.boilr.utils.Log;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchableListAdapter<T> extends ListAdapter<T> implements OnTouchListener, OnClickListener {

	protected final CharSequence SEARCH;
	protected SearchableListPreference searchableListPreference;
	protected CharSequence search;
	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			SearchableListAdapter.this.search = s;
			SearchableListAdapter.this.getFilter().filter(s);
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
			if(event.getAction() == MotionEvent.ACTION_UP) {
				if(search.equals(SEARCH))
					((EditText) v).setText("");
				searchableListPreference.getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				v.requestFocus();
			}
			return false;
		}
	};
	//private OnTouchListener closeListener = new OnTou

	public SearchableListAdapter(Context context, List<T> list,
			SearchableListPreference searchableListPreference) {
		super(context, list);
		this.searchableListPreference = searchableListPreference;
		SEARCH = search = context.getString(R.string.action_search);
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {

		if( position == 0 ){
			convertView = getInflater().inflate(R.layout.list_preference_search, parent, false);
			convertView.setTag(SEARCH);
			EditText editText = (EditText) convertView.findViewById(R.id.searchable_edit_text);
			editText.setText(search);
			editText.setOnTouchListener(clearListener);
			editText.addTextChangedListener(watcher);
			searchableListPreference.getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
			searchableListPreference.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_‌​VISIBLE);
			
			Button btnClose = (Button) convertView.findViewById(R.id.searchable_close_button);
			TypedArray ta = getContext().obtainStyledAttributes(new int[]{R.attr.ic_action_remove});
			btnClose.setBackgroundDrawable(ta.getDrawable(0));
			ta.recycle();
			
			btnClose.setOnClickListener(this);
			
			if(!search.equals(SEARCH) && !search.equals("")){
				editText.requestFocus();
			}
		}else{
			if(convertView == null || SEARCH.equals(convertView.getTag())){
				convertView = getInflater().inflate(R.layout.list_preference_row, parent, false);
			}
			CharSequence pair = (CharSequence) mList.get(position-1);
			convertView.setTag(position-1);
			convertView.setOnTouchListener(this);
			TextView textView = (TextView) convertView.findViewById(R.id.searchable_text_view);
			textView.setText(pair);
			RadioButton pairRadiobutton = (RadioButton) convertView.findViewById(R.id.searchable_radio_button);
			if(this.searchableListPreference.getEntry().equals(pair)){
				pairRadiobutton.setChecked(true);
			}else{
				pairRadiobutton.setChecked(false);
			}
			pairRadiobutton.setClickable(false);
		}
		return convertView;
	}

	@Override
	public void remove(int position) {
		synchronized (mLock) {
			if(mOriginalList != null) {
				mOriginalList.remove(position + 1);
			} else {
				mList.remove(position + 1);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// Used to determinate the number of rows to display
		return mList.size() + 1;
	}

	@Override
	public T getItem(int position) {
		return mList.get(position + 1);
	}

	@Override
	public long getItemId(int position) {
		return position - 1;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				RadioButton radioButton = (RadioButton)view.findViewById(R.id.searchable_radio_button);
				radioButton.setChecked(true);
				
				view.setBackgroundColor(searchableListPreference.getContext().getResources().getColor(R.color.highlightblue));
				
				TextView textView = (TextView) view.findViewById(R.id.searchable_text_view);
				CharSequence[] values = searchableListPreference.getEntryValues();

				CharSequence[] entries = searchableListPreference.getEntries();
				CharSequence value = null;
				for (int i = 0; i < entries.length; i++) {
					if(entries[i].equals(textView.getText())) {
						value = values[i];
						break;
					}
				}
				searchableListPreference.setValue((String) value);
				searchableListPreference.getOnPreferenceChangeListener().onPreferenceChange(searchableListPreference, value);
				searchableListPreference.getDialog().dismiss();
				break;
			case MotionEvent.ACTION_MOVE:
				InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				break;
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		((EditText) ((View) v.getParent()).findViewById(R.id.searchable_edit_text)).setText("");
		}
}
