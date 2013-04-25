package com.firstpeople.paintpad.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.firstpeople.paintpad.utils.PaintConstants.PATH;

public class SDCardFiles {
	public static List<File> getPaintPadPicFiles() {
		List<File> imageFiles = new ArrayList<File>();
		File f = new File(PATH.SAVE_PATH);
		// 如果当前路径不存在，则创建路径
		if (!f.exists()) {
			if (!f.mkdirs()) {
				Log.d("debug", "in getPaintPadPicFiles");
			}
		}
		File[] files = f.listFiles();
		// 如果文件存在
		if (files.length != 0) {
			/* 将所有文件存入ArrayList中 */
			for (int i = 0; i < files.length; i++) {
				// 如果是图片文件，则加入file
				if (isImageFile(files[i].getPath())) {
					imageFiles.add(files[i]);
				}
			}
		}
		return imageFiles;
	}

	public static List<String> getPaintPadPicPaths() {
		List<File> imageFiles = getPaintPadPicFiles();
		List<String> filePathList = new ArrayList<String>();
		for (File file : imageFiles) {
			filePathList.add(file.getPath());
		}
		return filePathList;
	}

	public static List<String> getPaintPadPicNames() {
		List<String> nameList = new ArrayList<String>();
		List<File> imageFiles = getPaintPadPicFiles();
		for (File file : imageFiles) {
			nameList.add(file.getName());
		}
		return nameList;
	}

	public static boolean fileNameExists(String name) {
		List<String> nameList = getPaintPadPicNames();
		if (nameList.contains(name)) {
			return true;
		}
		return false;
	}

	private static boolean isImageFile(String fName) {
		boolean imageExist;

		/* 取得扩展名 */
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		/* 按扩展名的类型决定MimeType */
		if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			imageExist = true;
		} else {
			imageExist = false;
		}
		return imageExist;
	}
}
