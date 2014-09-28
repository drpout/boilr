package mobi.boilr.boilr.listeners;

import mobi.boilr.boilr.activities.AlarmListActivity;
import mobi.boilr.boilr.adapters.AlarmListAdapter;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.Alarm;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.ListView;

public class OnSwipeTouchListener implements OnTouchListener {
	private AlarmListActivity enclosingActivity;
	private static final int SWIPE_DURATION = 250;
	private static final double REMOVE_THRESHOLD = 0.5;
	private static final long DURATION = 500;

	private Integer pointToPosition = null;
	private float mDownX;
	private int mSwipeSlop = -1;
	private boolean mItemPressed = false;
	private boolean mSwiping = false;
	private ListView mListView;


	public OnSwipeTouchListener(AlarmListActivity ctx) {
		enclosingActivity = ctx;
		mListView = ctx.getListView();
		// gestureDetector = new GestureDetector(ctx, new GestureListener(ctx));
	}

	@Override
	public boolean onTouch(final View view, MotionEvent event) {

		pointToPosition = pointToPosition == null ? enclosingActivity.getListView()
				.pointToPosition((int) event.getX(), (int) event.getY()) : pointToPosition;

				View childView = mListView.getChildAt(pointToPosition);

				// When no row is selected, do nothing
				if (childView == null) {
					
					return false;
				}

				if (mSwipeSlop < 0) {
					mSwipeSlop = ViewConfiguration.get(enclosingActivity).getScaledTouchSlop();
				}

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						if (mItemPressed) {
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
						if (!mSwiping) {
							if (deltaXAbs > mSwipeSlop) {
								mSwiping = true;
								mListView.requestDisallowInterceptTouchEvent(true);
								// mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
							}
						}
						if (mSwiping) {
							childView.setTranslationX((x - mDownX));
							// Set fade to be almost invisible when when threshold to remove is achieved.
							childView.setAlpha(1 - deltaXAbs / view.getWidth());
						}
					}
					break;
					case MotionEvent.ACTION_UP: {
						// User let go - figure out whether to animate the view out, or back into place
						if (mSwiping) {
							float x = event.getX() + view.getTranslationX();
							float deltaX = x - mDownX;
							float deltaXAbs = Math.abs(deltaX);
							float fractionCovered;
							float endX;
							float endAlpha;
							final boolean remove;
							if (deltaXAbs > view.getWidth() * REMOVE_THRESHOLD) {
								// Greater than a quarter of the width - animate it out
								fractionCovered = deltaXAbs / childView.getWidth();
								endX = deltaX < 0 ? -view.getWidth() : view.getWidth();
								endAlpha = 0;
								remove = true;
							} else {
								// Not far enough - animate it back
								fractionCovered = 1 - (deltaXAbs / view.getWidth());
								endX = 0;
								endAlpha = 1;
								remove = false;
							}

							long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
							mListView.setEnabled(false);

							if (remove) {
								AlarmListAdapter adapter = (AlarmListAdapter) enclosingActivity
										.getListAdapter();
								Alarm alarm = adapter.getItem(pointToPosition);
								if (enclosingActivity.ismBound()) {
									enclosingActivity.getmStorageAndControlService().deleteAlarm(alarm);
									adapter.remove(pointToPosition);
								} else {
									Log.d("Couldn't remove alarm " + alarm.getId());
								}
								mSwiping = false;
								mListView.setEnabled(true);
							} else {
								mSwiping = false;
								mListView.setEnabled(true);
								childView.setAlpha(1);
								childView.setTranslationX(0);
							}
						} else {
							// It's not a swipe, it most be a click, return false to signal it
							return false;
						}
					}
					pointToPosition = null;
					mItemPressed = false;
					break;
					default:
						return false;
				}

				return true;
	}
}