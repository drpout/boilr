/*
 * ProgressCircle
 * 
 * Copyright (C) 2013  Nathaniel McCallum <npmccallum@redhat.com>, Red Hat (licensed under APLv2)
 * 			 (C) 2015  André Santos <andrefilipebrazsantos@gmail.com>, Boilr (minor edit, relicensed under GPLv3) 
 */

package mobi.boilr.boilr.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class FixedCircle extends ProgressCircle {

	public FixedCircle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context, attrs, 2);
	}

	public FixedCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context, attrs, 2);
	}

	public FixedCircle(Context context) {
		super(context);
		setup(context, null, 2);
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
		canvas.drawArc(mRectF, -60, 300, !mHollow, mPaint);
	}
}