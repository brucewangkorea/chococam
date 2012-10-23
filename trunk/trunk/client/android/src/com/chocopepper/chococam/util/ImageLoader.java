package com.chocopepper.chococam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.chocopepper.chococam.R;

public class ImageLoader {

	public static interface OnLoadFinishedListener {
		public void whenLoadFinished(ImageView imageView, Bitmap bitmap);
	}
	private OnLoadFinishedListener mOnFinishedListener = null;
	public void setOnLoadFinishedListener(OnLoadFinishedListener listener) {
		mOnFinishedListener = listener;
	}

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	Activity mContext;
	boolean mbRotateAutomatically = true;
	boolean imageResizeCheck = false;
	public void setAutomaticRotateImage(boolean b) {
		mbRotateAutomatically = b;
	}

	public void setImageResize(boolean b) {
		imageResizeCheck = b;
	}

	int stub_id = R.drawable.stub;
	public void setStubImageResourceId(int id) {
		stub_id = id;
	}

	public void DisplayImage(String url, ImageView imageView) {
		if (url == null || url.equals(Constants.SERVER_IMAGE_PATH)) {
			Logger.d("IMAGE_LOADER", "URL is invalid - only the server address");
			return;
		}
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);

			if (mOnFinishedListener != null) {
				mOnFinishedListener.whenLoadFinished(imageView, bitmap);
			}
		} else {
			queuePhoto(url, imageView);
		}
	}
	/**
	 * 12-09-17 nerine 이미지 스케일링용
	 * 
	 * @param url
	 * @param imageView
	 * @param mContext
	 */
	public void DisplayImage(String url, ImageView imageView, Activity mContext) {
		this.mContext = mContext;
		if (url == null || url.equals(Constants.SERVER_IMAGE_PATH)) {
			Logger.d("IMAGE_LOADER", "URL is invalid - only the server address");
			return;
		}

		try {
			imageViews.put(imageView, url);
			Bitmap bitmap = memoryCache.get(url);
			if (bitmap != null) {

				// Get display width from device
				int displayWidth = this.mContext.getWindowManager()
						.getDefaultDisplay().getWidth();

				// Get margin to use it for calculating to max width of the
				// ImageView
//				LayoutParams layoutParams = imageView.getLayoutParams();
//				int leftMargin = layoutParams..leftMargin;
//				int rightMargin = layoutParams.rightMargin;
				// Calculate the max width of the imageView
				int imageViewWidth = displayWidth;// - (leftMargin + rightMargin);
				// Calculate scaling factor and return it
				float scalingFactor = ((float) imageViewWidth / (float) bitmap
						.getWidth());

				// Create a new bitmap with the scaling factor
				Bitmap newBitmap = ImageUtil.ScaleBitmap(bitmap, scalingFactor);

				imageView.setImageBitmap(newBitmap);

				if (mOnFinishedListener != null) {
					mOnFinishedListener.whenLoadFinished(imageView, bitmap);
				}
			} else {
				queuePhoto(url, imageView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	public Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// 20120-05-18 brucewang
			// 스케일링 하지 않고 원본 사이즈 그대로 반환 합니다.
			// return BitmapFactory.decodeStream(new FileInputStream(f) );
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 1000;
			int width_tmp = o.outWidth;
			int scale = 1;
			if (width_tmp > REQUIRED_SIZE) {
				scale = 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}
	// BitmapFactory.Options o = new BitmapFactory.Options();
	// o.inJustDecodeBounds = true;
	// BitmapFactory.decodeStream(new FileInputStream(f),null,o);
	//
	// //Find the correct scale value. It should be the power of 2.
	// final int REQUIRED_SIZE=70;
	// int width_tmp=o.outWidth, height_tmp=o.outHeight;
	// int scale=1;
	// while(true){
	// if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	// break;
	// width_tmp/=2;
	// height_tmp/=2;
	// scale*=2;
	// }
	//
	// //decode with inSampleSize
	// BitmapFactory.Options o2 = new BitmapFactory.Options();
	// o2.inSampleSize=scale;
	// return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	// } catch (FileNotFoundException e) {}
	// return null;
	// }

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;
		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;
		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null) {
				if (bitmap.getWidth() > 240) {
					if (mbRotateAutomatically) {
						if (bitmap.getHeight() < bitmap.getWidth()) {
							bitmap = imgRotate(bitmap);
						}
					}
				}

				/**
				 * 이미지 리사이즈를 플래그가 들어오면 이미지를 리사이즈 시킨다
				 */
				if (imageResizeCheck) {
					// Get display width from device
					int displayWidth = mContext.getWindowManager()
							.getDefaultDisplay().getWidth();

					// Get margin to use it for calculating to max width of the
					// ImageView
					FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) photoToLoad.imageView
							.getLayoutParams();
					int leftMargin = layoutParams.leftMargin;
					int rightMargin = layoutParams.rightMargin;
					// Calculate the max width of the imageView
					int imageViewWidth = displayWidth
							- (leftMargin + rightMargin);
					// Calculate scaling factor and return it
					float scalingFactor = ((float) imageViewWidth / (float) bitmap
							.getWidth());

					// Create a new bitmap with the scaling factor
					Bitmap newBitmap = ImageUtil.ScaleBitmap(bitmap,
							scalingFactor);
					photoToLoad.imageView.setImageBitmap(newBitmap);
				} else {
					photoToLoad.imageView.setImageBitmap(bitmap);
				}

				if (mOnFinishedListener != null) {
					mOnFinishedListener.whenLoadFinished(photoToLoad.imageView,
							bitmap);
				}
			} else
				photoToLoad.imageView.setImageResource(stub_id);
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	private Bitmap imgRotate(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();

		Matrix matrix = new Matrix();
		matrix.postRotate(90);

		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height,
				matrix, true);
		bmp.recycle();

		return resizedBitmap;
	}

}
