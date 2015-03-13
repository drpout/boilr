package mobi.boilr.boilr.listeners;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.widget.AlarmListAdapter;
import mobi.boilr.libpricealarm.Alarm;
import android.content.ClipData;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.GridView;

import com.cocosw.undobar.UndoBarController.AdvancedUndoListener;
import com.cocosw.undobar.UndoBarController.UndoBar;

public class SwipeAndMoveTouchListener implements OnTouchListener {

	public class Reference<T> {
		private T reference;

		public Reference(T t) {
			reference = t;
		}

		public T getReference() {
			return reference;
		}

		public void setReference(T reference) {
			this.reference = reference;
		}
	}

	private class LongClickTask implements Runnable {
		private View view;

		@Override
		public void run() {
			 view.post(new Runnable() {
			 @Override
			 public void run() {
					mPointToPosition = -1;
					mSwiping = false;
					mItemPressed = false;
					ClipData data = ClipData.newPlainText("", "");
					DragShadowBuilder sb = new View.DragShadowBuilder(view);
					view.startDrag(data, sb, new Reference<View>(view), 0);
				}
			});
		}

		public void setView(View view) {
			this.view = view;
		}
	}
	
	private class UndoDeleteListener implements AdvancedUndoListener {
		@Override
		public void onUndo(Parcelable token) {
			if(mActivity.ismBound()) {
				Bundle b = (Bundle) token;
				int alarmID = b.getInt("alarmID");
				int filteredPos = b.getInt("filteredPos");
				int originalPos = b.getInt("originalPos");
				Alarm alarm = mActivity.getStorageAndControlService().getAlarm(alarmID);
				AlarmListAdapter adapter = mActivity.getAdapter();
				adapter.add(alarm, filteredPos, originalPos);
			} else {
				Log.e(mActivity.getString(R.string.not_bound, "SwipeAndMoveTouchListener"));
			}
		}

		@Override
		public void onHide(Parcelable token) {
			Bundle b = (Bundle) token;
			int alarmID = b.getInt("alarmID");
			if(mActivity.ismBound()) {
				mActivity.getStorageAndControlService().deleteAlarm(alarmID);
			} else {
				Log.e(mActivity.getString(R.string.not_bound, "SwipeAndMoveTouchListener"));
			}
		}

		@Override
		public void onClear(Parcelable[] tokens) {
			for(Parcelable p : tokens) {
				onHide(p);
			}
		}
	}

	private AlarmListActivity mActivity;
	private static final double REMOVE_THRESHOLD = 0.5;
	private int mPointToPosition = -1;
	private float mDownX;
	private float mDownY;
	private int mSwipeSlop = -1;
	private int mSwipeSlopX = -1;
	private boolean mItemPressed = false;
	private boolean mSwiping = false;
	private GridView mView;
	private Handler mHandler = new Handler();
	private final LongClickTask mLongClickTask = new LongClickTask();
	private final UndoDeleteListener mUndoListener = new UndoDeleteListener();
	private final UndoBar mUndoBar;

	public SwipeAndMoveTouchListener(AlarmListActivity listActivity) {
		mActivity = listActivity;
		mView = listActivity.getGridView();
		mUndoBar = new UndoBar(listActivity).listener(mUndoListener);
	}

	@Override
	public boolean onTouch(final View view, MotionEvent event) {
		// pointToPosition returns the absolute position of the view in the list.
		mPointToPosition = mPointToPosition == -1 ? mView.pointToPosition((int) event.getX(), (int) event.getY()) : mPointToPosition;
		// getChildAt returns the the n visible view
		View childView = mView.getChildAt(mPointToPosition - mView.getFirstVisiblePosition());
		// When no row is selected, do nothing
		if(childView == null) {
			// Touching empty space.
			mPointToPosition = -1;
			mSwiping = false;
			mItemPressed = false;
			return false;
		}

		if(mSwipeSlop < 0) {
			mSwipeSlop = ViewConfiguration.get(mActivity).getScaledTouchSlop();
			mSwipeSlopX = mSwipeSlop * 7;
		}

		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(mItemPressed) {
				// Multi-item swipes not handled
				return false;
			}
			mLongClickTask.setView(childView);
			mHandler.postDelayed(mLongClickTask, ViewConfiguration.getLongPressTimeout());
			mItemPressed = true;
			mDownX = event.getX();
			mDownY = event.getY();
			return false;

		case MotionEvent.ACTION_CANCEL:
			childView.setAlpha(1);
			childView.setTranslationX(0);
			mHandler.removeCallbacks(mLongClickTask);
			mPointToPosition = -1;
			mItemPressed = false;
			mSwiping = false;
			return false;

		case MotionEvent.ACTION_MOVE: {
			float x = event.getX() + view.getTranslationX();
			float y = event.getY() + view.getTranslationY();
			float deltaX = x - mDownX;
			float deltaY = y - mDownY;
			float deltaXAbs = Math.abs(deltaX);
			float deltaYAbs = Math.abs(deltaY);
			if(!mSwiping) {
				if(deltaXAbs > mSwipeSlopX) {
					mSwiping = true;
					mView.requestDisallowInterceptTouchEvent(true);
					mHandler.removeCallbacks(mLongClickTask);
				} else if(deltaYAbs > mSwipeSlop) {
					// It's not an horizontal swipe, it most something else, let another listener handle it
					mPointToPosition = -1;
					mSwiping = false;
					mItemPressed = false;
					mHandler.removeCallbacks(mLongClickTask);
					return false;
				}
			}
			if(mSwiping) {
				childView.setTranslationX((x - mDownX));
				// Set fade to be almost invisible when when threshold to remove is achieved.
				childView.setAlpha(1 - deltaXAbs / view.getWidth());
			}
		}
			break;

		case MotionEvent.ACTION_UP: {
			// User let go - figure out whether to animate the view out, or back into place
			if(mSwiping) {
				float x = event.getX() + view.getTranslationX();
				float deltaX = x - mDownX;
				float deltaXAbs = Math.abs(deltaX);
				final boolean remove = deltaXAbs > childView.getWidth() * REMOVE_THRESHOLD;
				mView.setEnabled(false);
				if(remove) {
					AlarmListAdapter adapter = mActivity.getAdapter();
					Alarm alarm = adapter.getItem(mPointToPosition);
					Bundle b = new Bundle(3);
					b.putInt("alarmID", alarm.getId());
					b.putInt("filteredPos", mPointToPosition);
					b.putInt("originalPos", adapter.originalIndexOf(alarm));
					adapter.remove(alarm);
					mUndoBar.clear();
					mUndoBar.message(R.string.alarm_deleted).token(b).show();
				} else {
					// back into place
					childView.setAlpha(1);
					childView.setTranslationX(0);
				}
				mView.setEnabled(true);
				mSwiping = false;
			} else {
				// It's not an horizontal swipe, let another listener handle it.
				mPointToPosition = -1;
				mItemPressed = false;
				mHandler.removeCallbacks(mLongClickTask);
				return false;
			}
			mPointToPosition = -1;
			mItemPressed = false;
			mHandler.removeCallbacks(mLongClickTask);
			return false;
		}

		default:
			return false;
		}

		return true;
	}
}