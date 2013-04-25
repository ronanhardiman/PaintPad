package com.firstpeople.paintpad.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.provider.MediaStore;

public class BitMapUtils {

	public static void saveToSdCard(String path, Bitmap bitmap) {
		if (null != bitmap && null != path && !path.equalsIgnoreCase("")) {
			try {
				File file = new File(path);
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.PNG, 30, bos);
				bos.flush();
				bos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
	}

	public static Bitmap duplicateBitmap(Bitmap bmpSrc, int width, int height) {
		if (null == bmpSrc) {
			return null;
		}

		int bmpSrcWidth = bmpSrc.getWidth();
		int bmpSrcHeight = bmpSrc.getHeight();

		Bitmap bmpDest = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		if (null != bmpDest) {
			Canvas canvas = new Canvas(bmpDest);
			Rect viewRect = new Rect();
			final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);
			if (bmpSrcWidth <= width && bmpSrcHeight <= height) {
				viewRect.set(rect);
			} else if (bmpSrcHeight > height && bmpSrcWidth <= width) {
				viewRect.set(0, 0, bmpSrcWidth, height);
			} else if (bmpSrcHeight <= height && bmpSrcWidth > width) {
				viewRect.set(0, 0, width, bmpSrcWidth);
			} else if (bmpSrcHeight > height && bmpSrcWidth > width) {
				viewRect.set(0, 0, width, height);
			}
			canvas.drawBitmap(bmpSrc, rect, viewRect, null);
		}

		return bmpDest;
	}

	public static Bitmap duplicateBitmap(Bitmap bmpSrc) {
		if (null == bmpSrc) {
			return null;
		}

		int bmpSrcWidth = bmpSrc.getWidth();
		int bmpSrcHeight = bmpSrc.getHeight();

		Bitmap bmpDest = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight,
				Config.ARGB_8888);
		if (null != bmpDest) {
			Canvas canvas = new Canvas(bmpDest);
			final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);

			canvas.drawBitmap(bmpSrc, rect, rect, null);
		}

		return bmpDest;
	}

	public static Bitmap loadFromSdCard(String filePath) {
		File file = new File(filePath);
		Bitmap bmp = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			bmp = BitmapFactory.decodeStream(fis);
			if (bmp != null) {
				return bmp;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Cursor queryImages(Activity context, String selection,
			String[] selectionArgs) {
		String[] columns = new String[] { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DISPLAY_NAME };
		return context.managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
				selection, selectionArgs,
				MediaStore.Images.Media.DEFAULT_SORT_ORDER);
	}

	public static Bitmap decodeBitmap(String path, int displayWidth,
			int displayHeight) {
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(path, op); // 获取尺寸信息
		// 获取比例大小
		int wRatio = (int) Math.ceil(op.outWidth / (float) displayWidth);
		int hRatio = (int) Math.ceil(op.outHeight / (float) displayHeight);
		// 如果超出指定大小，则缩小相应的比例
		if (wRatio > 1 && hRatio > 1) {
			if (wRatio > hRatio) {
				op.inSampleSize = wRatio;
			} else {
				op.inSampleSize = hRatio;
			}
		}
		op.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(path, op);
		return Bitmap
				.createScaledBitmap(bmp, displayWidth, displayHeight, true);
	}

	public static Bitmap decodeBitmap(String path, int maxImageSize) {
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(path, op); // 获取尺寸信息
		int scale = 1;
		if (op.outWidth > maxImageSize || op.outHeight > maxImageSize) {
			scale = (int) Math.pow(
					2,
					(int) Math.round(Math.log(maxImageSize
							/ (double) Math.max(op.outWidth, op.outHeight))
							/ Math.log(0.5)));
		}
		op.inJustDecodeBounds = false;
		op.inSampleSize = scale;
		bmp = BitmapFactory.decodeFile(path, op);
		return bmp;
	}

	public static Bitmap queryImageById(Activity context, int imageId) {
		String selection = MediaStore.Images.Media._ID + "=?";
		String[] selectionArgs = new String[] { imageId + "" };
		Cursor cursor = BitMapUtils.queryImages(context, selection,
				selectionArgs);
		if (cursor.moveToFirst()) {
			String path = cursor.getString(cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
			cursor.close();
			// return BitmapUtils.decodeBitmap(path, 260, 260);
			return BitMapUtils.decodeBitmap(path, 220); // 看看和上面这种方式的差别,看了，差不多
		} else {
			cursor.close();
			return null;
		}
	}

	public static Cursor queryThumbnails(Activity context, String selection,
			String[] selectionArgs) {
		String[] columns = new String[] { MediaStore.Images.Thumbnails.DATA,
				MediaStore.Images.Thumbnails._ID,
				MediaStore.Images.Thumbnails.IMAGE_ID };
		return context.managedQuery(
				MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, columns,
				selection, selectionArgs,
				MediaStore.Images.Thumbnails.DEFAULT_SORT_ORDER);
	}

	public static Bitmap queryImageByThumbnailId(Activity context,
			Integer thumbId) {
		String selection = MediaStore.Images.Thumbnails._ID + " = ?";
		String[] selectionArgs = new String[] { thumbId + "" };
		Cursor cursor = BitMapUtils.queryThumbnails(context, selection,
				selectionArgs);

		if (cursor.moveToFirst()) {
			int imageId = cursor
					.getInt(cursor
							.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
			cursor.close();
			return BitMapUtils.queryImageById(context, imageId);
		} else {
			cursor.close();
			return null;
		}
	}

	public static byte[] bitampToByteArray(Bitmap bitmap) {
		byte[] array = null;
		try {
			if (null != bitmap) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
				array = os.toByteArray();
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return array;
	}

	public static Bitmap byteArrayToBitmap(byte[] array) {
		if (null == array) {
			return null;
		}

		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}
}
