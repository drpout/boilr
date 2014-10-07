package mobi.boilr.boilr.preference;

import java.util.Arrays;

import mobi.boilr.boilr.widget.ListAdapter;
import mobi.boilr.boilr.widget.SearchableListAdapter;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class SearchableListPreference extends ListPreference {
	ListAdapter<CharSequence> adapter;

	public SearchableListPreference(Context context) {
		super(context);
	}

	public SearchableListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		if(adapter == null){
			adapter = new SearchableListAdapter<CharSequence>(getContext(), Arrays.asList(getEntries()), this);
		}else{
			adapter.clear();
			adapter.addAll(Arrays.asList(getEntries()));
		}
		builder.setAdapter(adapter, this);
		super.onPrepareDialogBuilder(builder);
	}
}
