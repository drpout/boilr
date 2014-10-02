package mobi.boilr.boilr.views.fragments;

import java.util.List;

import mobi.boilr.boilr.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Creates a Dialog with information about the app.
 * Based on similar dialog on org.fdroid.fdroid.FDroid.java
 */
public class AboutDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity currentActivity = getActivity();
		LayoutInflater li = LayoutInflater.from(currentActivity);
		View view = li.inflate(R.layout.about, null);

		// Fill in the version...
		try {
			PackageInfo pi = currentActivity.getPackageManager().getPackageInfo(currentActivity.getApplicationContext().getPackageName(), 0);
			((TextView) view.findViewById(R.id.version)).setText(pi.versionName);
		} catch (Exception e) {
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
		builder.setView(view)
				.setIcon(R.drawable.ic_launcher)
				.setTitle(R.string.about_title)
				.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});

		// The following texts have links specified by putting <a> tags in the string
		// resource. The following makes these links respond to user clicks.
		// Found by Richard and posted on Stack Overflow http://stackoverflow.com/a/2746708
		TextView[] devs = { (TextView) view.findViewById(R.id.devs_andre),
				(TextView) view.findViewById(R.id.devs_david),
				(TextView) view.findViewById(R.id.devs_ricardo) };
		for (TextView dev : devs)
			dev.setMovementMethod(LinkMovementMethod.getInstance());

		((TextView) view.findViewById(R.id.btc_addr)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String btcLink = currentActivity.getString(R.string.btc_link);
				Intent btcIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(btcLink));
				PackageManager packageManager = currentActivity.getPackageManager();
				List<ResolveInfo> activities = packageManager.queryIntentActivities(btcIntent, 0);
				boolean isIntentSafe = activities.size() > 0;
				if(isIntentSafe) {
					startActivity(btcIntent);
				}
			}
		});

		return builder.create();
	}
}
