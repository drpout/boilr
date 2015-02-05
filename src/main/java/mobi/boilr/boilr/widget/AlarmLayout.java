package mobi.boilr.boilr.widget;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
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

public class AlarmLayout extends LinearLayout {
	private static final int[] ATTRS = new int[] { R.attr.progress_circle_color_on, R.attr.progress_circle_color_off, android.R.attr.textColorPrimary };
	private static final int COLORON = 0;
	private static final int COLOROFF = 1;
	private static final int PRIMARYCOLOR = 2;

	private Alarm mAlarm;
	private TextView mLastValueView;
	private long mLastValue;
	private ProgressCircle mProgressUpdate;
	private TypedArray mColorsArray;
	private TextView mUpperLimit;
	private TextView mLowerLimit;
	private TextView mVariance;
	private TextView mBaseValue;

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
		mUpperLimit = (TextView) findViewById(R.id.upper_limit);
		mLowerLimit = (TextView) findViewById(R.id.lower_limit);
		mVariance = (TextView) findViewById(R.id.variance);
		mBaseValue = (TextView) findViewById(R.id.base_value);
		mProgressUpdate = (ProgressCircle) findViewById(R.id.progress_update);
	}

	public void updateChildren(long currentTime) {

		if(mLastValue != mAlarm.getLastValue()) {
			if(mAlarm.getDirection() == Direction.UP) {
				mLastValueView.setTextColor(getResources().getColor(R.color.tickergreen));
			} else if(mAlarm.getDirection() == Direction.DOWN) {
				mLastValueView.setTextColor(getResources().getColor(R.color.tickerred));
			} else {
				mLastValueView.setTextColor(mColorsArray.getColor(PRIMARYCOLOR, Color.RED));
			}
			mLastValueView.setText(Conversions.formatMaxDecimalPlaces(mAlarm.getLastValue()));
		}

		mUpperLimit.setText(Conversions.formatMaxDecimalPlaces(mAlarm.getUpperLimit()));

		mLowerLimit.setText(Conversions.formatMaxDecimalPlaces(mAlarm.getLowerLimit()));

		mProgressUpdate.setMax(mAlarm.getPeriod());
		long progress = mAlarm.getPeriod();
		if(mAlarm.getLastUpdateTimestamp() != null) {
			progress = progress - (currentTime - mAlarm.getLastUpdateTimestamp().getTime());
		}
		mProgressUpdate.setProgress(progress);
		if(mAlarm.isOn()) {
			mProgressUpdate.setColor(mColorsArray.getColor(COLORON, Color.LTGRAY));
		} else {
			mProgressUpdate.setColor(mColorsArray.getColor(COLOROFF, Color.LTGRAY));
		}

		if(mAlarm instanceof PriceChangeAlarm) {
			mVariance.setVisibility(VISIBLE);
			PriceChangeAlarm priceChangeAlarm = (PriceChangeAlarm) mAlarm;
			if(priceChangeAlarm.isPercent()) {
				mVariance.setText(Conversions.format2DecimalPlaces(priceChangeAlarm.getPercent()) + "%");
			} else {
				mVariance.setText(Conversions.formatMaxDecimalPlaces(priceChangeAlarm.getChange()));
			}
			if(mAlarm instanceof PriceSpikeAlarm) {
				PriceSpikeAlarm priceSpikeAlarm = (PriceSpikeAlarm) mAlarm;
				(findViewById(R.id.base_value)).setVisibility(VISIBLE);
				mBaseValue.setVisibility(VISIBLE);
				mBaseValue.setText(Conversions.formatMaxDecimalPlaces(priceSpikeAlarm.getBaseValue()));
			} else {
				(findViewById(R.id.base_value)).setVisibility(GONE);
			}
		} else {
			mBaseValue.setVisibility(GONE);
			mVariance.setVisibility(GONE);
		}
	}
}