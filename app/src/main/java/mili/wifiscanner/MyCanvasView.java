/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mili.wifiscanner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.io.InputStream;

/**
 * Custom view that follows touch events to draw on a canvas.
 */

public class MyCanvasView extends View {

    private static final String TAG = "MyCanvasView";

    private Paint mPaint;
    private Paint mPointPaint;
    private Paint mDrawPaint;
    private Path mPath;
    private Path mDrawPath;
    private int mDrawColor;
    private int mBackgroundColor;
    private Canvas mExtraCanvas;
    private Bitmap mExtraBitmap;
    private Rect mFrame;

    private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.f;
    private Drawable mImage;
    private Drawable[] mImages = new Drawable[4];


//    MyCanvasView(Context context) {
//        this(context, null);
//    }

    public MyCanvasView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.d(TAG, "construction");

        mBackgroundColor = ResourcesCompat.getColor(getResources(),
                R.color.black, null);
        mDrawColor = ResourcesCompat.getColor(getResources(),
                R.color.purple_700, null);


        // Holds the path we are currently drawing.
        mPath = new Path();
        mDrawPath = new Path();
        // Set up the paint with which to draw.
        mPaint = new Paint();
        mPaint.setColor(mDrawColor);
        // Smoothes out edges of what is drawn without affecting shape.
        mPaint.setAntiAlias(true);
        // Dithering affects how colors with higher-precision
        // than the device are down-sampled.
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE); // default: FILL
        mPaint.setStrokeJoin(Paint.Join.ROUND); // default: MITER
        mPaint.setStrokeCap(Paint.Cap.ROUND); // default: BUTT
        mPaint.setStrokeWidth(12); // default: Hairline-width (really thin)
        mPointPaint = new Paint(mPaint);
        mPointPaint.setStrokeWidth(25);
        mDrawPaint = new Paint(mPaint);
        mDrawPaint.setColor(ResourcesCompat.getColor(getResources(),
                R.color.teal_700, null));

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        // load image
        try {
            // get input stream
            InputStream ims = context.getAssets().open("minespark.jpg");
            // load image as Drawable
            mImage = Drawable.createFromStream(ims, null);
            // set image to ImageView
            // mImage.setImageDrawable(d);

            for (int i = 1; i<5; i++) {
                // get input stream
                ims = context.getAssets().open("minespark"+ i + ".jpg");
                // load image as Drawable
                mImages[i-1] = Drawable.createFromStream(ims, null);
            }
        }
        catch(IOException ex) {
            return;
        }
    }

    public void setImage(int i) {
        if (i < mImages.length) {
            mImage = mImages[i];
        }
        mExtraCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    /**
     * Note: Called whenever the view changes size.
     * Since the view starts out with no size, this is also called after
     * the view has been inflated and has a valid size.
     */
    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // Create bitmap, create canvas with bitmap, fill canvas with color.
        mExtraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mExtraCanvas = new Canvas(mExtraBitmap);
//        mExtraCanvas.drawColor(mBackgroundColor);

        // Calculate the rect a frame around the picture.
        int inset = 40;
        mFrame = new Rect(inset, inset, width - inset, height - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.save();
//        canvas.scale(scaleFactor, scaleFactor);


//        canvas.restore();
        Rect imageBounds = canvas.getClipBounds();  // Adjust this for where you want it
        mImage.setBounds(imageBounds);
        mImage.draw(canvas);

        // Draw the bitmap that has the saved path.
        canvas.drawBitmap(mExtraBitmap, 0, 0, null);


        // Draw a frame around the picture.
        canvas.drawRect(mFrame, mPaint);
    }

    // Variables for the latest x,y values,
    // which are the starting point for the next path.
    private float mX, mY;
    // Don't draw every single pixel.
    // If the finger has has moved less than this distance, don't draw.
    private static final float TOUCH_TOLERANCE = 4;

    // The following methods factor out what happens for different touch events,
    // as determined by the onTouchEvent() switch statement.
    // This keeps the switch statement
    // concise Drawand and easier to change what happens for each event.

    private void touchStart(float x, float y) {
        mDrawPath.moveTo(x, y);
        mX = x;
        mY = y;

//        mX = (float) Math.random()*getWidth();
//        mY = (float) Math.random()*getHeight();
//        Log.d(TAG, getScreenHeight() + " " + getScreenWidth() + " " + mX + " " + mY);
//        mExtraCanvas.drawPoint(mX,mY, mPointPaint);
//        mExtraCanvas.drawPath(mPath, mPaint);
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            mDrawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            // Draw the path in the extra bitmap to save it.
            mExtraCanvas.drawPath(mDrawPath, mDrawPaint);
        }
    }

    public void update(float x, float y, float xx, float yy) {
        x += getWidth()/2;
        y += getHeight()/2;
        xx += getWidth()/2;
        yy += getHeight()/2;



        mPath.moveTo(x, y);
        mPath.quadTo(x, y, xx, yy);

        mExtraCanvas.drawPath(mPath, mPaint);
        invalidate();
        mPath.reset();
    }

    private void touchUp() {
        // Reset the path so it doesn't get drawn again.
        mDrawPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        // Invalidate() is inside the case statements because there are many
        // other types of motion events passed into this listener,
        // and we don't want to invalidate the view for those.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                // No need to invalidate because we are not drawing anything.
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
            default:
                // do nothing
        }
        scaleDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
            invalidate();
            return true;
        }
    }
    // Get the width of the screen
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    // Get the height of the screen
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}
