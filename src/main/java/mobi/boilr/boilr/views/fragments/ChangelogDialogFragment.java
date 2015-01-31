package mobi.boilr.boilr.views.fragments;

import it.gmariotti.changelibs.library.view.ChangeLogListView;
import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.VersionTracker;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

public class ChangelogDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ChangeLogListView chgList = (ChangeLogListView) layoutInflater.inflate(R.layout.changelog_view, null);
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.release_notes).setView(chgList)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						VersionTracker.updateVersionInPreferences();
						dialog.dismiss();
					}
				}).create();
	}

}
