package mobi.boilr.boilr.listeners;

import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.widget.AlarmListAdapter;
import mobi.boilr.libpricealarm.Alarm;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.ListView;

public class OnSwipeTouchListener implements OnTouchListener {
	private AlarmListActivity enclosingActivity;
	private static final double REMOVE_THRESHOLD = 0.5;

	private int mPointToPosition = -1;
	private float mDownX;
	private int mSwipeSlop = -1;
	private boolean mItemPressed = false;
	private boolean mSwiping = false;
	private ListView mListView;

	public OnSwipeTouchListener(AlarmListActivity ctx) {
		enclosingActivity = ctx;
		mListView = ctx.getListView();
	}

	@Override
	public boolean onTouch(final View view, MotionEvent event) {
		//pointToPosition returns the absolute position of the view in the list.
		mPointToPosition = mPointToPosition == -1 ? mListView.pointToPosition((int) event.getX(), (int) event.getY()) : mPointToPosition;

		//getChildAt returns the the n visible view
		View childView = mListView.getChildAt(mPointToPosition - (mListView.getFirstVisiblePosition() - mListView.getHeaderViewsCount()));

		// When no row is selected, do nothing
		if(childView == null) {
			//Touching empty space.
			mPointToPosition = -1;
			mSwiping = false;
			mItemPressed = false;
			return false;
		}

		if(mSwipeSlop < 0) {
			mSwipeSlop = ViewConfiguration.get(enclosingActivity).getScaledTouchSlop()*3;
		}

		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
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
						mListView.requestDisallowInterceptTouchEvent(true);
					}else{
						//It's not an horizontal swipe, it most something else, let another listener handle it
						mPointToPosition = -1;
						mSwiping = false;
						mItemPressed = false;
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
					final boolean remove = deltaXAbs > view.getWidth() * REMOVE_THRESHOLD;
					mListView.setEnabled(false);
					if(remove) {
						//remove view
						AlarmListAdapter adapter = (AlarmListAdapter) enclosingActivity.getListAdapter();
						Alarm alarm = adapter.getItem(mPointToPosition);
						if(enclosingActivity.ismBound()) {
							enclosingActivity.getmStorageAndControlService().deleteAlarm(alarm);
							adapter.remove(mPointToPosition);
						} else {
							Log.d("Couldn't remove alarm " + alarm.getId());
						}						
					} else {
						//back into place
						childView.setAlpha(1);
						childView.setTranslationX(0);
					}
					mListView.setEnabled(true);
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