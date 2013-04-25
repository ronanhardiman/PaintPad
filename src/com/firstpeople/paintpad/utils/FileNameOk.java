package com.firstpeople.paintpad.utils;

import java.io.File;
import java.util.regex.Pattern;

public class FileNameOk {
	public static boolean isFileNameOk(File file) {
		return Pattern.compile("[\\w%+,/=_-]+").matcher(file.getPath())
				.matches();
	}
}

