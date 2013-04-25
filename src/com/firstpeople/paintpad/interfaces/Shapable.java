package com.firstpeople.paintpad.interfaces;

import android.graphics.Path;

import com.firstpeople.paintpad.painttools.FirstCurrentPosition;

public interface Shapable {
	public Path getPath();

	public FirstCurrentPosition getFirstLastPoint();

	void setShap(ShapesInterface shape);
}
