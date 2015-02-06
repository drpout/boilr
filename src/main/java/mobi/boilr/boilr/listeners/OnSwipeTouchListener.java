package mobi.boilr.boilr.listeners;

import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.boilr.widget.AlarmListAdapter;
import mobi.boilr.libpricealarm.Alarm;
import android.content.ClipData;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.GridView;

public class OnSwipeTouchListener implements OnTouchListener {

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
					view.setVisibility(View.INVISIBLE);

				}
			});
		}

		public void setView(View view) {
			this.view = view;
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

	public OnSwipeTouchListener(AlarmListActivity ctx) {
		mActivity = ctx;
		mView = ctx.getGridView();
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
			mSwipeSlopX = mSwipeSlop * 3;
		}

		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(mItemPressed) {
				// Multi-item swipes not handled
				return false;
			}
			mLongClickTask.setView(childView);
			mHandler.postDelayed(mLongClickTask, 1000);
			mItemPressed = true;
			mDownX = event.getX();
			mDownY = event.getY();
			return false;

		case MotionEvent.ACTION_CANCEL:
			Log.d("Cancel");
			childView.setAlpha(1);
			childView.setTranslationX(0);
			mHandler.removeCallbacks(mLongClickTask);
			mPointToPosition = -1;
			mItemPressed = false;
			mSwiping = false;
			return false;

		case MotionEvent.ACTION_MOVE: {
			Log.d("Move");
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
			Log.d("UP");
			// User let go - figure out whether to animate the view out, or back into place
			if(mSwiping) {
				float x = event.getX() + view.getTranslationX();
				float deltaX = x - mDownX;
				float deltaXAbs = Math.abs(deltaX);
				final boolean remove = deltaXAbs > childView.getWidth() * REMOVE_THRESHOLD;
				mView.setEnabled(false);
				if(remove) {
					// remove view
					AlarmListAdapter adapter = mActivity.getAdapter();
					Alarm alarm = adapter.getItem(mPointToPosition);
					if(mActivity.ismBound()) {
						mActivity.getStorageAndControlService().deleteAlarm(alarm);
						adapter.remove(mPointToPosition);
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