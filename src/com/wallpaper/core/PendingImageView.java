package com.wallpaper.core;

import com.wallpaper.core.uk.co.senab.photoview.PhotoView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class PendingImageView extends PhotoView {

	private OnDisplayImage mCaller;

	public static interface OnDisplayImage {
		public void onImageDisplayImage(boolean success);
	}

	public void setOnImageDisplayListener(OnDisplayImage caller) {
		this.mCaller = caller;
	}

	public PendingImageView(Context context) {
		super(context);
		if (context instanceof OnDisplayImage) {
			this.mCaller = (OnDisplayImage) context;
		}
	}

	public PendingImageView(Context context, OnDisplayImage caller) {
		super(context);
		this.mCaller = caller;
	}

	public PendingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		if (this.mCaller != null) {
			mCaller.onImageDisplayImage((drawable != null));
		}

		super.setImageDrawable(drawable);
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		if (this.mCaller != null) {
			mCaller.onImageDisplayImage((bitmap != null));
		}
		super.setImageBitmap(bitmap);
	}

	public Bitmap getBitmap() {
		final Drawable drawable = super.getDrawable();
		if (drawable == null) {
			return null;
		}

		return ((BitmapDrawable) drawable).getBitmap();
	}

}
