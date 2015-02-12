package mobi.boilr.boilr.widget;

import mobi.boilr.boilr.R;
import mobi.boilr.boilr.utils.Conversions;
import mobi.boilr.boilr.utils.Themer;
import mobi.boilr.libpricealarm.Alarm;
import mobi.boilr.libpricealarm.Alarm.Direction;
import mobi.boilr.libpricealarm.RollingPriceChangeAlarm;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlarmLayout extends LinearLayout implements Runnable {
	private static final int[] ATTRS = new int[] { R.attr.progress_circle_color_on, R.attr.progress_circle_color_off, android.R.attr.textColorPrimary };
	private static final int COLORON = 0;
	private static final int COLOROFF = 1;
	private static final int PRIMARYCOLOR = 2;
	private static final float APLHA_OFF = 0.5f;

	private Alarm mAlarm;
	private TextView mLastValueView;
	private TypedArray mColorsArray;
	private TextView mUpperLimitView;
	private TextView mLowerLimitView;
	private TextView mVarianceView;
	private TextView mBaseValueView;
	private ProgressCircle mLastUpdateProgress;
	private FixedCircle mFixedCircle;
	private ImageView mBar;
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
		mFixedCircle = (FixedCircle) findViewById(R.id.fixed_circle);
		mBar = (ImageView) findViewById(R.id.bar);
	}

	public void updateChildren(long currentTime) {

		if(mAlarm.isOn()) {
			setAlpha(1);
		} else {
			setAlpha(APLHA_OFF);
		}
		if(mAlarm.getDirection() == Direction.UP) {
			mLastValueView.setTextColor(getResources().getColor(R.color.tickergreen));
		} else if(mAlarm.getDirection() == Direction.DOWN) {
			mLastValueView.setTextColor(getResources().getColor(R.color.tickerred));
		} else {
			mLastValueView.setTextColor(mColorsArray.getColor(PRIMARYCOLOR, Color.GRAY));
		}
		mLastValueView.setText(Conversions.format8SignificantDigits(mAlarm.getLastValue()));
		mUpperLimitView.setText(Conversions.format8SignificantDigits(mAlarm.getUpperLimit()));
		mLowerLimitView.setText(Conversions.format8SignificantDigits(mAlarm.getLowerLimit()));
		mFixedCircle.setColor(mColorsArray.getColor(COLOROFF, Color.LTGRAY));
		progress = mAlarm.getPeriod();
		mLastUpdateProgress.setMax(progress);
		if(mAlarm.getLastUpdateTimestamp() != null) {
			progress = progress - (currentTime - mAlarm.getLastUpdateTimestamp().getTime());
		}
		if(mAlarm.isOn()) {
			mLastUpdateProgress.setColor(mColorsArray.getColor(COLORON, Color.LTGRAY));
			mBar.setImageDrawable(getResources().getDrawable(R.drawable.onbar));
		} else {
			mLastUpdateProgress.setColor(mColorsArray.getColor(COLOROFF, Color.LTGRAY));
			if(Themer.getCurTheme().equals(Themer.Theme.light)) {
				mBar.setImageDrawable(getResources().getDrawable(R.drawable.ltoffbar));
			} else {
				mBar.setImageDrawable(getResources().getDrawable(R.drawable.dkoffbar));
			}
		}
		mLastUpdateProgress.setProgress(progress);
		mFixedCircle.invalidate();
		if(mAlarm instanceof RollingPriceChangeAlarm) {
			RollingPriceChangeAlarm priceChangeAlarm = (RollingPriceChangeAlarm) mAlarm;
			mVarianceView.setVisibility(VISIBLE);
			if(priceChangeAlarm.isPercent()) {
				mVarianceView.setText(Conversions.format2DecimalPlaces(priceChangeAlarm.getPercent()) + "%");
			} else {
				mVarianceView.setText(Conversions.format8SignificantDigits(priceChangeAlarm.getChange()));
			}
			mBaseValueView.setVisibility(VISIBLE);
			mBaseValueView.setText(Conversions.format8SignificantDigits(priceChangeAlarm.getBaseValue()));
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
