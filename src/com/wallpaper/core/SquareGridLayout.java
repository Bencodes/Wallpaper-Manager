package com.wallpaper.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class SquareGridLayout extends ViewGroup {

	private int mSize = 1;

	private int mSquareDimensions;

	public SquareGridLayout (Context context) {
		super(context);
	}

	public SquareGridLayout (Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {

		// breakdown specs
		final int mw = MeasureSpec.getMode(widthMeasureSpec);
		final int mh = MeasureSpec.getMode(heightMeasureSpec);
		final int sw = MeasureSpec.getSize(widthMeasureSpec);
		final int sh = MeasureSpec.getSize(heightMeasureSpec);

		// compute padding
		final int pw = getPaddingLeft() + getPaddingRight();
		final int ph = getPaddingTop() + getPaddingBottom();

		// compute largest size of square (both with and without padding)
		final int s;
		final int sp;
		if (mw == MeasureSpec.UNSPECIFIED && mh == MeasureSpec.UNSPECIFIED) {
			throw new IllegalArgumentException("Layout must be constrained on at least one axis");
		} else if (mw == MeasureSpec.UNSPECIFIED) {
			s = sh;
			sp = s - ph;
		} else if (mh == MeasureSpec.UNSPECIFIED) {
			s = sw;
			sp = s - pw;
		} else {
			if (sw - pw < sh - ph) {
				s = sw;
				sp = s - pw;
			} else {
				s = sh;
				sp = s - ph;
			}
		}

		// guard against giving the children a -ve measure spec due to excessive
		// padding
		final int spp = Math.max(sp, 0);

		// pass on our rigid dimensions to our children
		final int size = mSize;
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				final View child = getChildAt(y * size + x);
				if (child == null)
					continue;
				// measure each child
				// we could try to accommodate oversized children, but we don't
				measureChildWithMargins(child, MeasureSpec.makeMeasureSpec((spp + x) / size, MeasureSpec.EXACTLY), 0, MeasureSpec.makeMeasureSpec((spp + y) / size, MeasureSpec.EXACTLY), 0);
			}
		}

		// record our dimensions
		setMeasuredDimension(mw == MeasureSpec.EXACTLY ? sw : sp + pw, mh == MeasureSpec.EXACTLY ? sh : sp + ph);
		mSquareDimensions = sp;
	}

	// ViewGroup methods

	@Override
	protected LayoutParams generateDefaultLayoutParams () {
		return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected LayoutParams generateLayoutParams (LayoutParams p) {
		return new MarginLayoutParams(p);
	}

	@Override
	public LayoutParams generateLayoutParams (AttributeSet attrs) {
		return new MarginLayoutParams(getContext(), attrs);
	}

	@Override
	protected void onLayout (boolean changed, int l, int t, int r, int b) {

		// recover the previously computed square dimensions for efficiency
		final int s = mSquareDimensions;

		{
			// adjust for our padding
			final int pl = getPaddingLeft();
			final int pt = getPaddingTop();
			final int pr = getPaddingRight();
			final int pb = getPaddingBottom();

			// allocate any extra spare space evenly
			l = pl + (r - pr - l - pl - s) / 2;
			t = pt + (b - pb - t - pb - s) / 2;
		}

		final int size = mSize;
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				View child = getChildAt(y * mSize + x);
				// optimization: we are moving through the children in order
				// when we hit null, there are no more children to layout so
				// return
				if (child == null)
					return;
				// get the child's layout parameters so that we can honour their
				// margins
				MarginLayoutParams lps = (MarginLayoutParams) child.getLayoutParams();
				// we don't support gravity, so the arithmetic is simplified
				child.layout(l + (s * x) / size + lps.leftMargin, t + (s * y) / size + lps.topMargin, l + (s * (x + 1)) / size - lps.rightMargin, t + (s * (y + 1)) / size - lps.bottomMargin);
			}
		}
	}

}