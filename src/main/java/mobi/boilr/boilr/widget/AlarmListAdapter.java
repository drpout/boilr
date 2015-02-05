package mobi.boilr.boilr.widget;

import java.util.List;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.activities.AlarmSettingsActivity;
import mobi.boilr.libpricealarm.Alarm;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AlarmListAdapter extends ListAdapter<Alarm> {

	private AlarmListActivity mAlarmListActivity;
	private Alarm mAlarm;
	private AlarmLayout mAlarmLayout;
	private TextView mExchange;
	private TextView mPair;

	private class Reference<T> {
		T reference;

		public Reference(T t) {
			reference = t;
		}
	}

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

			// convertView.setOnDragListener(new OnDragListener() {
			//
			// // private View mSrcView;
			// private Reference<View> ref;
			// private View mDstView;
			// private Handler mHandler = new Handler();
			// private Runnable scrollTask = new Runnable() {
			// @Override
			// public void run() {
			// final View mSrcView = ref.reference;
			// GridView grid = (GridView) mSrcView.getParent();
			// int current = grid.getPositionForView(mSrcView);
			// Log.d("cur " + current);
			// Log.d("Frist " + ((Alarm)
			// grid.getItemAtPosition(grid.getFirstVisiblePosition())).getExchangeCode());
			// Log.d(" " + grid.getFirstVisiblePosition());
			// Log.d("Last " + ((Alarm)
			// grid.getItemAtPosition(grid.getLastVisiblePosition())).getExchangeCode());
			// Log.d(" " + grid.getLastVisiblePosition());
			// if(grid.getFirstVisiblePosition() >= current - 1) {
			// mDstView = grid.getChildAt(current -
			// grid.getFirstVisiblePosition());
			// grid.smoothScrollToPosition(grid.getFirstVisiblePosition() - 1);
			// mSrcView.setVisibility(View.VISIBLE);
			// mDstView.setVisibility(View.INVISIBLE);
			// AlarmListAdapter.this.moveTo(((AlarmLayout) mSrcView).getAlarm(),
			// ((AlarmLayout) mDstView).getAlarm());
			// ref.reference = mDstView;
			// } else if(grid.getLastVisiblePosition() <= current + 1) {
			// mDstView = grid.getChildAt(current -
			// grid.getFirstVisiblePosition());
			// grid.smoothScrollToPosition(grid.getLastVisiblePosition() + 1);
			// mSrcView.setVisibility(View.VISIBLE);
			// mDstView.setVisibility(View.INVISIBLE);
			// AlarmListAdapter.this.moveTo(((AlarmLayout) mSrcView).getAlarm(),
			// ((AlarmLayout) mDstView).getAlarm());
			// ref.reference = mDstView;
			// }
			// mSrcView.postDelayed(this, 500);
			// }
			// };
			//
			// @Override
			// public boolean onDrag(View dstView, DragEvent event) {
			// mDstView = dstView;
			// // Reference<View> ref = (Reference<View>)
			// // event.getLocalState();
			// ref = (Reference<View>) event.getLocalState();
			// final View mView = ref.reference;
			//
			// switch (event.getAction()) {
			// case DragEvent.ACTION_DRAG_STARTED:
			//
			// // scrollTask = new ScrollTask();
			// mView.post(scrollTask);
			// break;
			// case DragEvent.ACTION_DRAG_ENTERED:
			// mView.setVisibility(View.VISIBLE);
			// dstView.setVisibility(View.INVISIBLE);
			// AlarmListAdapter.this.moveTo(((AlarmLayout) mView).getAlarm(),
			// ((AlarmLayout) dstView).getAlarm());
			// ref.reference = dstView;
			// case DragEvent.ACTION_DRAG_ENDED:
			// Log.d("Drag Ended");
			// mView.removeCallbacks(scrollTask);
			// mView.post(new Runnable() {
			// @Override
			// public void run() {
			// mView.setVisibility(View.VISIBLE);
			// }
			// });
			// break;
			// }
			// return true;
			// }
			// });
			//
			// convertView.setOnTouchListener(new OnTouchListener() {
			// private float mSwipeSlop =
			// ViewConfiguration.get(mAlarmListActivity).getScaledTouchSlop() *
			// 3;
			// private float mDownX;
			// private float mDownY;
			//
			// private final LongClickTask longClickTask = new LongClickTask();
			// private float deltaX = 100;
			// private float deltaY = 100;
			// private boolean mDragging = false;
			//
			// final class LongClickTask implements Runnable {
			// private View view;
			//
			// @Override
			// public void run() {
			// Log.d("Long Click");
			// view.post(new Runnable() {
			// @Override
			// public void run() {
			// mDragging = true;
			// Log.d("STARTING");
			// ClipData data = ClipData.newPlainText("", "");
			// DragShadowBuilder sb = new View.DragShadowBuilder(view);
			// view.startDrag(data, sb, new Reference<View>(view), 0);
			// view.setVisibility(View.INVISIBLE);
			// }
			// });
			// }
			//
			// public void setView(View view) {
			// this.view = view;
			// }
			// }
			//
			// @Override
			// public boolean onTouch(View view, MotionEvent motionEvent) {
			// if(mSwipeSlop < 0) {
			// mSwipeSlop =
			// ViewConfiguration.get(getContext()).getScaledTouchSlop();
			// }
			// switch(motionEvent.getAction()) {
			// case MotionEvent.ACTION_DOWN:
			// Log.d("ODown");
			// mDragging = false;
			// mDownX = motionEvent.getX();
			// mDownY = motionEvent.getY();
			// longClickTask.setView(view);
			// view.postDelayed(longClickTask, 1000);
			// return true;
			// case MotionEvent.ACTION_MOVE:
			// if(mSwipeSlop > Math.abs(mDownX - motionEvent.getX()) && deltaY >
			// Math.abs(mSwipeSlop - motionEvent.getY())) {
			// Log.d("Small Movement");
			// return false;
			// } else {
			// Log.d("big Mobement");
			// }
			// break;
			// case MotionEvent.ACTION_UP:
			// Log.d("OUp");
			// if(!mDragging) {
			// Log.d("CLICK");
			// mAlarmListActivity.getStorageAndControlService().toggleAlarm(((AlarmLayout)
			// view).getAlarm().getId());
			// }
			// break;
			// }
			// view.removeCallbacks(longClickTask);
			// return false;
			// }
			// });

			// convertView.setOnClickListener(new View.OnClickListener() {
			// @Override
			// public void onClick(View view) {
			// Log.d("CLICK");
			// mAlarmListActivity.getStorageAndControlService().toggleAlarm(((AlarmLayout)
			// view).getAlarm().getId());
			// }
			// });

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

	@Override
	public void moveTo(Alarm A1, Alarm A2) {
		super.moveTo(A1, A2);
		mAlarmListActivity.getStorageAndControlService().updateAlarmPosition(A1, A2);
	}
}