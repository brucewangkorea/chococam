package com.chocopepper.chococam.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ImageViewRounded extends ImageView {

    public ImageViewRounded(Context context) {
        super(context);
    }

    public ImageViewRounded(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewRounded(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return; 
        }

        Bitmap fullSizeBitmap = drawable.getBitmap();
        int scaledWidth = getMeasuredWidth();
        int scaledHeight = getMeasuredHeight();
        
        if( fullSizeBitmap==null ){
        	//fullSizeBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ALPHA_8);
        	return;
        }

        // limit the image size
        if( scaledWidth<1 || scaledWidth>128 ){
        	scaledWidth = 128;
        }
        if( scaledHeight<1 || scaledHeight>128 ){
        	scaledHeight = 128;
        }

        Bitmap mScaledBitmap;
        if (scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight()) {
            mScaledBitmap = fullSizeBitmap;
        } else {
            mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap, scaledWidth, scaledHeight, true /* filter */);
        }

        Bitmap roundBitmap = ImageHelper.getRoundedCornerBitmap(getContext(), mScaledBitmap, 8, scaledWidth, scaledHeight,
                false, false, false, false);
        canvas.drawBitmap(roundBitmap, 0, 0, null);

    }

}
