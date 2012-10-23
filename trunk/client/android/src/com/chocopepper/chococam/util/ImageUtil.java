package com.chocopepper.chococam.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

public class ImageUtil {
	private static final String TAG = Logger.makeLogTag(ImageUtil.class);
	
	public static void recycleBitmap(ImageView iv) {
		Drawable d = iv.getDrawable();
		if (d instanceof BitmapDrawable) {
			Bitmap b = ((BitmapDrawable)d).getBitmap();
			b.recycle();
		}
		// 현재로서는 BitmapDrawable 이외의 drawable 들에 대한 직접적인 메모리 해제는 불가능하다.
		d.setCallback(null);
	}
	
	public static Bitmap getPic(String filename) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(filename);
		} catch(Exception e){
			Logger.e(TAG, "Bitmap load error",e);
		}
		
		return bitmap;
	}
	
	public static int getSampliSize(Activity activity, int width, int height) {
		// 화면 크기 취득
		Logger.w(TAG, "width :" + width + ", height :" + height);
//		Display currentDisplay = activity.getWindowManager().getDefaultDisplay();
		 
		float dw = 120;
		float dh = 120;
		
		// 가로/세로 축소 비율 취득 
		int widthtRatio = (int) Math.ceil(width / dw);
		int heightRatio = (int) Math.ceil(height / dh);

		Logger.w(TAG, "widthtRatio :" + widthtRatio + ", heightRatio :" + heightRatio);
		// 초기 리사이즈 비율
		int sampleSize = 1;

		// 가로 세로 비율이 화면보다 큰경우에만 처리
		if (widthtRatio > 1 && height > 1) {
			if (widthtRatio > heightRatio) {
				// 	가로 축소 비율이 큰 경우
				sampleSize = widthtRatio;
			} else {
				// 	세로 축소 비율이 큰 경우
				sampleSize = heightRatio;
			}
		}

		Logger.w(TAG, "sampleSize :" + sampleSize);
		return sampleSize;
	}

	public static int getExifOrientation(String filepath) {
		long start = System.currentTimeMillis();
		int degree = 0;
		ExifInterface exif = null;

		try {
			exif = new ExifInterface(filepath);
		} catch (Exception e) {
			Logger.e(TAG, "ExifInterface error", e);
		}

		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			if (orientation != -1) { 
				switch(orientation) { 
					case ExifInterface.ORIENTATION_ROTATE_90:
						degree = 90;
						break;
		     
					case ExifInterface.ORIENTATION_ROTATE_180:
						degree = 180;
						break;
		     
					case ExifInterface.ORIENTATION_ROTATE_270:
						degree = 270;
						break;
				}
			}
		}
		long end = System.currentTimeMillis();
		Logger.d(TAG, "degree get time :" + (end - start) + "ms");
		return degree;
	}
	
	public static Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) {
		long start = System.currentTimeMillis();
		if (degrees != 0 && bitmap != null) { 
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

			try {
				Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
		   
				if (bitmap != b2) {
					bitmap.recycle();
					bitmap = b2;
				}
			} catch (OutOfMemoryError e) {
				Logger.e(TAG, "outofMemory!!!!!!!!!!!!!!!!!!!", e);
			}
		}

		long end = System.currentTimeMillis();
		Logger.d(TAG, "rotate time :" + (end - start) + "ms");
		return bitmap;
	}
	
	public static String saveImage(String savedir, String filename, Bitmap image) {
		long start = System.currentTimeMillis();
		File imgfile = new File(savedir, filename);
		try {
			File dir = new File(savedir);
			if (!dir.exists()) {
				dir.mkdir();
			}
			
			if (!imgfile.exists()) {
				imgfile.createNewFile();
			}

			FileOutputStream fos = new FileOutputStream(imgfile);
			image.compress(CompressFormat.PNG, 100 , fos);
			fos.flush();
			fos.close();

			if (imgfile.exists()) {
				Logger.e(TAG, "Thumbnail File exists");
			} else {
				Logger.e(TAG, "Thumbnail File not exists");
			}
		} catch (Exception e) {
			Logger.e(TAG, "save tmpFile failed, file:" + imgfile.getAbsolutePath(), e);
		}

		long end = System.currentTimeMillis();
		Logger.d(TAG, "image save time :" + (end - start) + "ms");
		return imgfile.getAbsolutePath();
	}
	
	public static Bitmap cropBitmap(Bitmap bmp) {
		long start = System.currentTimeMillis();
		int h = bmp.getHeight();
		int w = bmp.getWidth();
		int wh = h;
		int x = 0;
		int y = 0;
		
		if (h > w) {
			int d = h - w;
			y = d / 2;
			wh = w;
		} else if (w > h) {
			int d = w - h;
			x = d / 2;
			wh = h;
		}
		
		Bitmap thumb = Bitmap.createBitmap(bmp, x, y, wh, wh);
		

		long end = System.currentTimeMillis();
		Logger.d(TAG, "bitmap crop time :" + (end - start) + "ms");
		return thumb;
	}

	
    /**
     * 선택된 uri의 사진 Path를 가져온다. 
     * uri 가 null 경우 마지막에 저장된 사진을 가져온다. 
     * @param uri 
     * @return File ImageFile
     */    
	public static File getImageFile(ContentResolver cr, Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		if (uri == null) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}
		Cursor c = cr.query(uri, projection, null, null,
				MediaStore.Images.Media.DATE_MODIFIED + " desc");        
		if(c == null || c.getCount() < 1) {
			return null; // no cursor or no record
		}
		
		int column_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		String path = null;
		if (c.moveToFirst()) {
			path = c.getString(column_index);
			if (c !=null ) {
				c.close();
				c = null;
			}
			return new File(path);
		}
		return null;
	}
	
	/**
	 * Drawable 객체를 byte 배열로 변환시키는 함수
	 * @param drawable 변환할 Drawable 객체
	 * @return 변환된 byte 배열
	 */
	public static byte[] getBytesFromDrawable(Drawable drawable) {
		if (drawable == null) 
			return null;
		
		byte[] imageInByte = null;
		try {
			int iconWidth = drawable.getIntrinsicWidth();
			int iconHeight = drawable.getIntrinsicHeight();
			Bitmap bitmap = Bitmap.createBitmap(iconWidth, iconHeight, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);  
			drawable.setBounds(0, 0, iconWidth, iconHeight);  
			drawable.draw(canvas); 
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); 
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream); 
			imageInByte = byteArrayOutputStream.toByteArray();
			bitmap.recycle();
		} catch (Exception e) {
			Logger.e(TAG, "Drawable translate error...", e);
		}
		/**
		 * nerine 12-07-04
		 * Out of Memory 예외 처리 
		 */
		catch (OutOfMemoryError e){
			Logger.e(TAG, "Out Of Memory...", e);
		}
		
		return imageInByte;
	}
	
	/**
	 * 이미지 스케일 처리
	 * @param bm
	 * @param scalingFactor
	 * @return
	 */
	public static Bitmap ScaleBitmap(Bitmap bm, float scalingFactor) {
		int scaleHeight = (int) (bm.getHeight() * scalingFactor);
		int scaleWidth = (int) (bm.getWidth() * scalingFactor);

		return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
	}
}
