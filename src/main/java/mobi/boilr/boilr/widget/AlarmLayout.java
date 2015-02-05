package mobi.boilr.boilr.widget;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Log;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.Alarm.Direction;
import mobi.boilr.libpricealarm.PriceChangeAlarm;
import mobi.boilr.libpricealarm.PriceSpikeAlarm;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlarmLayout extends LinearLayout implements Runnable {
	private static final int[] ATTRS = new int[] { R.attr.progress_circle_color_on, R.attr.progress_circle_color_off, android.R.attr.textColorPrimary };
	private static final int COLORON = 0;
	private static final int COLOROFF = 1;
	private static final int PRIMARYCOLOR = 2;

	private Alarm mAlarm;
	private TextView mLastValueView;
	private TypedArray mColorsArray;
	private TextView mUpperLimitView;
	private TextView mLowerLimitView;
	private TextView mVarianceView;
	private TextView mBaseValueView;
	private ProgressCircle mLastUpdateProgress;
	private ProgressCircle mTimeFrameProgress;
	private long progress;

	public AlarmLayout(Context context) {
		super(context);
	}

	public AlarmLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlarmLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public Alarm getAlarm() {
		return mAlarm;
	}

	public void setAlarm(Alarm alarm){
		this.mAlarm = alarm;
	}

	public void start() {
		mColorsArray = getContext().obtainStyledAttributes(ATTRS);
		mLastValueView = (TextView) findViewById(R.id.last_value);
		mUpperLimitView = (TextView) findViewById(R.id.upper_limit);
		mLowerLimitView = (TextView) findViewById(R.id.lower_limit);
		mVarianceView = (TextView) findViewById(R.id.variance);
		mBaseValueView = (TextView) findViewById(R.id.base_value);
		mLastUpdateProgress = (ProgressCircle) findViewById(R.id.progress_update);
		mTimeFrameProgress = (ProgressCircle) findViewById(R.id.progress_time_frame);
	}

	public void updateChildren(long currentTime) {
		if(mAlarm.getDirection() == Direction.UP) {
			mLastValueView.setTextColor(getResources().getColor(R.color.tickergreen));
		} else if(mAlarm.getDirection() == Direction.DOWN) {
			mLastValueView.setTextColor(getResources().getColor(R.color.tickerred));
		} else {
			mLastValueView.setTextColor(mColorsArray.getColor(PRIMARYCOLOR, Color.RED));
		}
		mLastValueView.setText(Conversions.formatMaxDecimalPlaces(mAlarm.getLastValue()));
		mUpperLimitView.setText(Conversions.formatMaxDecimalPlaces(mAlarm.getUpperLimit()));
		mLowerLimitView.setText(Conversions.formatMaxDecimalPlaces(mAlarm.getLowerLimit()));
		progress = mAlarm.getPeriod();
		mLastUpdateProgress.setMax(progress);
		if(mAlarm.getLastUpdateTimestamp() != null) {
			progress = progress - (currentTime - mAlarm.getLastUpdateTimestamp().getTime());
		}
		if(mAlarm.isOn()) {
			mLastUpdateProgress.setColor(mColorsArray.getColor(COLORON, Color.LTGRAY));
		} else {
			mLastUpdateProgress.setColor(mColorsArray.getColor(COLOROFF, Color.LTGRAY));
		}
		mLastUpdateProgress.setProgress(progress);
		if(mAlarm instanceof PriceChangeAlarm) {
			PriceChangeAlarm priceChangeAlarm = (PriceChangeAlarm) mAlarm;
			if(priceChangeAlarm.isPercent()) {
				mVarianceView.setText(Conversions.format2DecimalPlaces(priceChangeAlarm.getPercent()) + "%");
			} else {
				mVarianceView.setText(Conversions.formatMaxDecimalPlaces(priceChangeAlarm.getChange()));
			}
			mVarianceView.setVisibility(VISIBLE);
			if(mAlarm instanceof PriceSpikeAlarm) {
				PriceSpikeAlarm priceSpikeAlarm = (PriceSpikeAlarm) mAlarm;
				progress = priceSpikeAlarm.getTimeFrame();
				mTimeFrameProgress.setMax(progress);
				if(mAlarm.isOn()) {
					mTimeFrameProgress.setColor(mColorsArray.getColor(COLORON, Color.LTGRAY));
				} else {
					mTimeFrameProgress.setColor(mColorsArray.getColor(COLOROFF, Color.LTGRAY));
				}
				if(mAlarm.getLastUpdateTimestamp() != null) {
					progress = progress - (currentTime - priceSpikeAlarm.getLastUpdateTimestamp().getTime());
				}

				Log.d("progress " + progress);
				mTimeFrameProgress.setProgress(progress);
				mBaseValueView.setVisibility(VISIBLE);
				mBaseValueView.setVisibility(VISIBLE);
				mBaseValueView.setText(Conversions.formatMaxDecimalPlaces(priceSpikeAlarm.getBaseValue()));
			} else {
				mBaseValueView.setVisibility(GONE);
			}
		} else {
			mBaseValueView.setVisibility(GONE);
			mVarianceView.setVisibility(GONE);
		}
	}

	@Override
	public void run() {
		updateChildren(System.currentTimeMillis());
	}
}