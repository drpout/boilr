package mobi.boilr.boilr.widget;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.listeners.SwipeAndMoveTouchListener.Reference;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.Alarm;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class AlarmListAdapter extends ListAdapter<Alarm> {
	private boolean started = false;
	private AlarmListActivity mAlarmListActivity;
	private Alarm mAlarm;
	private AlarmLayout mAlarmLayout;
	private TextView mExchange;
	private TextView mPair;

	// private final Map<Integer, Float> alphas;
	
	public AlarmListAdapter(AlarmListActivity alarmListActivity, List<Alarm> alarms) {
		super(alarmListActivity, alarms);
		this.mAlarmListActivity = alarmListActivity;
		// alphas = new TreeMap<Integer, Float>();
		//
		// alphas.put(1, 0.95f);
		// alphas.put(2, 0.90f);
		// alphas.put(3, 0.85f);
		// alphas.put(4, 0.80f);
		// alphas.put(5, 0.75f);
		// alphas.put(6, 0.50f);
		// alphas.put(7, 0.35f);
		// alphas.put(8, 0.25f);
		
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
			View progressCircle = convertView.findViewById(R.id.progress_update_layout);

			progressCircle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAlarmListActivity.getStorageAndControlService().toggleAlarm(((AlarmLayout) v.getParent().getParent()).getAlarm().getId());
				}
			});
			convertView.setOnDragListener(new OnDragListener() {
				@SuppressWarnings("unchecked")
				@Override
				public boolean onDrag(View dstView, DragEvent event) {
					Reference<View> ref = (Reference<View>) event.getLocalState();
					final View mView = ref.getReference();
					switch(event.getAction()) {
					case DragEvent.ACTION_DRAG_STARTED:
						if(!started) {
							mView.setVisibility(View.INVISIBLE);
							started = true;
						}
						break;
					case DragEvent.ACTION_DRAG_ENTERED:
						mView.setVisibility(View.VISIBLE);
						dstView.setVisibility(View.INVISIBLE);
						AlarmListAdapter.this.moveTo(((AlarmLayout) mView).getAlarm(), ((AlarmLayout) dstView).getAlarm());
						ref.setReference(dstView);
						break;
					case DragEvent.ACTION_DRAG_ENDED:
						mView.post(new Runnable() {
							@Override
							public void run() {
								started = false;
								mView.setVisibility(View.VISIBLE);
							}
						});
						break;
					}
					return true;
				}
			});
		} else {
			// Recycled views retain the alpha and translation from when they were removed.
			if(convertView.getAlpha() != 1)
				convertView.setAlpha(1);
			if(convertView.getTranslationX() != 0)
				convertView.setTranslationX(0);
		}

		// if(alphas.containsKey(position)) {
		// convertView.setAlpha(alphas.get(position));
		// }

		mExchange = (TextView) convertView.findViewById(R.id.exchange);
		mExchange.setText(mAlarm.getExchange().getName());
		mPair = (TextView) convertView.findViewById(R.id.pair);
		mPair.setText(mAlarm.getPair().toString());
		mAlarmLayout = ((AlarmLayout) convertView);
		mAlarmLayout.setAlarm(mAlarm);
		mAlarmLayout.updateChildren(System.currentTimeMillis());
		return convertView;
	}

	@Override
	public void moveTo(Alarm A1, Alarm A2) {
		super.moveTo(A1, A2);
		mAlarmListActivity.getStorageAndControlService().updateAlarmPosition(A1, A2);
	}
}