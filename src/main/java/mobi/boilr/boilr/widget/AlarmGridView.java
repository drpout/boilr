package mobi.boilr.boilr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class AlarmGridView extends GridView implements Runnable {

	private static final long REFRESH_INTERVAL = 100;
	private long mCurrent;
	private View mView;

	public AlarmGridView(Context context) {
		super(context);
	}

	public AlarmGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlarmGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void run() {
		mCurrent = System.currentTimeMillis();
		for(int i = 0; i < getChildCount(); i++) {
			mView = getChildAt(i);
			if(mView.isShown()) {
				// post((Runnable) view);
				((AlarmLayout) mView).updateChildren(mCurrent);
			}
		}
		postDelayed(this, REFRESH_INTERVAL);
	}

	public void start(){
		post(this);
	}

	public void stop() {
		removeCallbacks(this);
	}
}