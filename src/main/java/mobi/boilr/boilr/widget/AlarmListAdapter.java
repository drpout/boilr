package mobi.boilr.boilr.widget;

import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.Alarm;
import android.content.Intent;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class AlarmListAdapter extends ListAdapter<Alarm> {

	private AlarmListActivity mAlarmListActivity;
	private Alarm mAlarm;
	private AlarmLayout mAlarmLayout;
	private TextView mExchange;
	private TextView mPair;

	public AlarmListAdapter(AlarmListActivity alarmListActivity, List<Alarm> alarms) {
		super(alarmListActivity, alarms);
		this.mAlarmListActivity = alarmListActivity;
	}

	@Override
	// TODO If needed optimize with http://www.piwai.info/android-adapter-good-practices
	public View getView(int position, View convertView, ViewGroup parent) {
		mAlarm = mList.get(position);

		// View recycling
		if(convertView == null){
			convertView = getInflater().inflate(R.layout.alarm_list_row, parent, false);
			mAlarmLayout = ((AlarmLayout) convertView);
			mAlarmLayout.start();
			View menu = convertView.findViewById(R.id.menu);
			menu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Alarm alarm = ((AlarmLayout) v.getParent()).getAlarm();
					Intent alarmSettingsIntent = new Intent(getContext(), AlarmSettingsActivity.class);
					alarmSettingsIntent.putExtra(AlarmSettingsActivity.alarmID, alarm.getId());
					alarmSettingsIntent.putExtra(AlarmSettingsActivity.alarmType, alarm.getClass().getSimpleName());
					getContext().startActivity(alarmSettingsIntent);
				}
			});

			convertView.setOnDragListener(new OnDragListener() {
				private View srcView ;
				@Override
				public boolean onDrag(View dstView, DragEvent event) {
					srcView = (View) event.getLocalState();

					switch (event.getAction()) {
					case DragEvent.ACTION_DRAG_ENTERED:
						srcView.setVisibility(View.VISIBLE);
						dstView.setVisibility(View.INVISIBLE);
						AlarmListAdapter.this.moveTo(((AlarmLayout)srcView).getAlarm(), ((AlarmLayout)dstView).getAlarm());
						srcView = dstView;
						break;

					case DragEvent.ACTION_DRAG_ENDED:
						srcView.post(new Runnable() {
							@Override
							public void run() {
								srcView.setVisibility(View.VISIBLE);
							}
						});
						break;
					}

					return true;
				}
			});


			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Log.d("LONGCLICKS");
					return true;
				}
			});

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Log.d("CLICK");
					mAlarmListActivity.getStorageAndControlService().toggleAlarm(((AlarmLayout) view).getAlarm().getId());
				}
			});

		} else {
			// Recycled views retain the alpha and translation from when they
			// were removed.
			if(convertView.getAlpha() != 1)
				convertView.setAlpha(1);
			if(convertView.getTranslationX() != 0)
				convertView.setTranslationX(0);
		}
		mExchange = (TextView) convertView.findViewById(R.id.exchange);
		mExchange.setText(mAlarm.getExchange().getName());
		mPair = (TextView) convertView.findViewById(R.id.pair);
		mPair.setText(mAlarm.getPair().toString());
		mAlarmLayout = ((AlarmLayout) convertView);
		mAlarmLayout.setAlarm(mAlarm);
		mAlarmLayout.updateChildren(System.currentTimeMillis());

		return convertView;
	}
}