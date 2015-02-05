package mobi.boilr.boilr.listeners;

import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.widget.AlarmListAdapter;
import mobi.boilr.libpricealarm.Alarm;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.GridView;

public class OnSwipeTouchListener implements OnTouchListener {
	private AlarmListActivity mActivity;
	private static final double REMOVE_THRESHOLD = 0.5;

	private int mPointToPosition = -1;
	private float mDownX;
	private int mSwipeSlop = -1;
	private boolean mItemPressed = false;
	private boolean mSwiping = false;
	private GridView mView;

	public OnSwipeTouchListener(AlarmListActivity ctx) {
		mActivity = ctx;
		mView = ctx.getGridView();
	}

	@Override
	public boolean onTouch(final View view, MotionEvent event) {
		//pointToPosition returns the absolute position of the view in the list.
		mPointToPosition = mPointToPosition == -1 ? mView.pointToPosition((int) event.getX(), (int) event.getY()) : mPointToPosition;
		//getChildAt returns the the n visible view
		View childView = mView.getChildAt(mPointToPosition - mView.getFirstVisiblePosition());
		// When no row is selected, do nothing
		if(childView == null) {
			//Touching empty space.
			mPointToPosition = -1;
			mSwiping = false;
			mItemPressed = false;
			return false;
		}

		if(mSwipeSlop < 0) {
			mSwipeSlop = ViewConfiguration.get(mActivity).getScaledTouchSlop() * 3;
		}

		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d("Down");
			if(mItemPressed) {
				// Multi-item swipes not handled
				return false;
			}

			mItemPressed = true;
			mDownX = event.getX();
			break;

		case MotionEvent.ACTION_CANCEL:
			childView.setAlpha(1);
			childView.setTranslationX(0);
			mItemPressed = false;
			break;

		case MotionEvent.ACTION_MOVE: {
			float x = event.getX() + view.getTranslationX();
			float deltaX = x - mDownX;
			float deltaXAbs = Math.abs(deltaX);
			if(!mSwiping) {
				if(deltaXAbs > mSwipeSlop) {
					mSwiping = true;
					mView.requestDisallowInterceptTouchEvent(true);
				} else {
					// It's not an horizontal swipe, it most something else, let
					// another listener handle it
					mPointToPosition = -1;
					mSwiping = false;
					mItemPressed = false;
					return false;
				}
			}
			if(mSwiping) {
				childView.setTranslationX((x - mDownX));
				// Set fade to be almost invisible when when threshold to remove
				// is achieved.
				childView.setAlpha(1 - deltaXAbs / view.getWidth());
			}
		}
			break;

		case MotionEvent.ACTION_UP: {
			// User let go - figure out whether to animate the view out, or back
			// into place
			if(mSwiping) {
				float x = event.getX() + view.getTranslationX();
				float deltaX = x - mDownX;
				float deltaXAbs = Math.abs(deltaX);
				final boolean remove = deltaXAbs > view.getWidth() * REMOVE_THRESHOLD;
				mView.setEnabled(false);
				if(remove) {
					// remove view
					AlarmListAdapter adapter = mActivity.getAdapter();
					Alarm alarm = adapter.getItem(mPointToPosition);
					if(mActivity.ismBound()) {
						mActivity.getStorageAndControlService().deleteAlarm(alarm);
						adapter.remove(mPointToPosition);
						// >>>>>>> d2d3e8d... Rearranged information on the
						// alarms' rows. Fixes #46.
					} else {
						Log.d("Couldn't remove alarm " + alarm.getId());
					}
				} else {
					// back into place
					childView.setAlpha(1);
					childView.setTranslationX(0);
				}
				mView.setEnabled(true);
				mSwiping = false;
			} else {
				// It's not an horizontal swipe, let another listener handle it.
				mSwiping = false;
				mPointToPosition = -1;
				mItemPressed = false;
				return false;
			}
			mPointToPosition = -1;
			mItemPressed = false;
		}
			break;
		default:
			return false;
		}
		return true;
	}
}