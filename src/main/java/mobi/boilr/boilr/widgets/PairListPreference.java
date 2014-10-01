package mobi.boilr.boilr.widgets;

import mobi.boilr.boilr.adapters.PairListAdapter;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class PairListPreference extends ListPreference {

	public PairListPreference(Context context) {
		super(context);
	}

	public PairListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		PairListAdapter adapter = new PairListAdapter(getContext(), getEntries(), this);
		builder.setAdapter(adapter, this);
		super.onPrepareDialogBuilder(builder);
	}
}
