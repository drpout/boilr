/*
 * ProgressCircle
 * 
 * Copyright (C) 2013  Nathaniel McCallum <npmccallum@redhat.com>, Red Hat (licensed under APLv2)
 * 			 (C) 2015  André Santos <andrefilipebrazsantos@gmail.com>, Boilr (minor edit, relicensed under GPLv3) 
 */

package mobi.boilr.boilr.widget;

import mobi.boilr.boilr.R;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ProgressCircle extends View {
	private Paint   mPaint;
	private RectF   mRectF;
	private Rect    mRect;
	private long    mProgress;
	private long    mMax;
	private boolean mHollow;
	private float   mPadding;
	private float   mStrokeWidth;
	private int mColor;

	public ProgressCircle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context, attrs);
	}

	public ProgressCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context, attrs);
	}

	public ProgressCircle(Context context) {
		super(context);
		setup(context, null);
	}

	private void setup(Context context, AttributeSet attrs) {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, dm);
		mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm);

		mRectF = new RectF();
		mRect = new Rect();

		mPaint = new Paint();
		mPaint.setColor(mColor);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.BUTT);

		if (attrs != null) {
			Theme t = context.getTheme();
			TypedArray a = t.obtainStyledAttributes(attrs, R.styleable.ProgressCircle, 0, 0);
			try {
				setMax(a.getInteger(R.styleable.ProgressCircle_max, 100));
				setHollow(a.getBoolean(R.styleable.ProgressCircle_hollow, false));
			} finally {
				a.recycle();
			}
		}
	}

	public void setMax(long max) {
		this.mMax = max;
	}

	public long getMax() {
		return mMax;
	}

	public void setHollow(boolean hollow) {
		mHollow = hollow;
		mPaint.setStyle(hollow ? Style.STROKE : Style.FILL);
		mPaint.setStrokeWidth(hollow ? mStrokeWidth : 0);
	}

	public boolean getHollow() {
		return mHollow;
	}

	public void setProgress(long progress) {
		mProgress = progress;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		getDrawingRect(mRect);
		mRect.left += getPaddingLeft() + mPadding;
		mRect.top += getPaddingTop() + mPadding;
		mRect.right -= getPaddingRight() + mPadding;
		mRect.bottom -= getPaddingBottom() + mPadding;
		mRectF.set(mRect);

		mPaint.setAntiAlias(true);
		mPaint.setStrokeCap(Paint.Cap.BUTT);
		mPaint.setColor(mColor);

		canvas.drawArc(mRectF, -90, mProgress < 0 ? getMax() : mProgress * 360 / getMax(), !mHollow, mPaint);
	}

	public void setColor(int color) {
		mColor = color;

	}
}