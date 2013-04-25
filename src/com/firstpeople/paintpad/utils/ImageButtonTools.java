package com.firstpeople.paintpad.utils;

import android.graphics.ColorMatrixColorFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;

public class ImageButtonTools {

	private ImageButtonTools() {

	}

	/**
	 * 按下这个按钮进行的颜色过滤
	 */
	public final static float[] BT_SELECTED = new float[] {//
		2, 0, 0, 0, 2,//
			0, 2, 0, 0, 2, //
			0, 0, 2, 0, 2, //
			0, 0, 0, 1, 0 };//
	/**
	 * 按钮恢复原状的颜色过滤
	 */
	public final static float[] BT_NOT_SELECTED = new float[] { //
	1, 0, 0, 0, 0,//
			0, 1, 0, 0, 0,//
			0, 0, 1, 0, 0,//
			0, 0, 0, 1, 0 };//

	/**
	 * 按钮焦点改变
	 */
	public final static OnFocusChangeListener buttonOnFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				v.getBackground().setColorFilter(
						new ColorMatrixColorFilter(BT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());
			} else {
				v.getBackground().setColorFilter(
						new ColorMatrixColorFilter(BT_NOT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());
			}
		}

	};

	/**
	 * 按钮触碰按下效果
	 */
	public final static OnTouchListener buttonOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				v.getBackground().setColorFilter(
						new ColorMatrixColorFilter(BT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				v.getBackground().setColorFilter(
						new ColorMatrixColorFilter(BT_NOT_SELECTED));
				v.setBackgroundDrawable(v.getBackground());
			}
			return false;
		}

	};

	public final static void setButtonFocusChanged(View inView) {
		inView.setOnTouchListener(buttonOnTouchListener);
		inView.setOnFocusChangeListener(buttonOnFocusChangeListener);
	}

}
