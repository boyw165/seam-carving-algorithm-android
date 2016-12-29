// Copyright (c) 2016-present boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.my.seams_carv.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.my.seams_carv.util.CvUtil;
import com.my.seams_carv.util.ImageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PhotoView extends ImageView {

    final static String TAG = "xyz";

    Paint mBoundPaint;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBoundPaint = new Paint();
        mBoundPaint.setColor(Color.GREEN);
        mBoundPaint.setStrokeWidth(15.f);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);

//        canvas.drawLine(getLeft(), getTop(),
//                        getRight(), getBottom(),
//                        mBoundPaint);
    }

    @Override
    public void setImageURI(final Uri uri) {
        if (uri == null) return;

        Observable
            .create(new ObservableOnSubscribe<Bitmap>() {
                @Override
                public void subscribe(ObservableEmitter<Bitmap> e)
                    throws Exception {
                    e.onNext(sampleBitmap(uri));
                    e.onComplete();
                }
            })
            .subscribeOn(Schedulers.computation())
            .debounce(200, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.computation())
            .map(new Function<Bitmap, Bitmap>() {
                @Override
                public Bitmap apply(Bitmap bitmap) throws Exception {
                    Log.d("xyz", "bmp width=" + bitmap.getWidth() + ", height=" + bitmap.getHeight());
                    // FIXME: We enlarge the size by 1.5 times in the horizontal
                    // FIXME: direction temporarily.
                    int finalWidth = (int) (2.f * bitmap.getWidth());
                    int finalHeight = bitmap.getHeight();
                    boolean which = true;
                    int[][] tmp0 = new int[finalWidth][finalHeight];
                    int[][] tmp1 = new int[finalWidth][finalHeight];
                    int[][] energyMap = new int[finalWidth][finalHeight];

                    CvUtil.toColors(bitmap, tmp0);

                    for (int i = 0; i < finalWidth - bitmap.getWidth(); ++i) {
                        Log.d("xyz", "seam#" + i);
                        int validWidth = bitmap.getWidth() + i;
                        int validHeight = bitmap.getHeight();

                        if (which) {
                            CvUtil.calcEnergyMap(tmp0, energyMap, validWidth, validHeight);
                            CvUtil.addSeam(tmp0,
                                           tmp1,
                                           CvUtil.findVerticalSeam(energyMap, validWidth));
                        } else {
                            CvUtil.calcEnergyMap(tmp1, energyMap, validWidth, validHeight);
                            CvUtil.addSeam(tmp1,
                                           tmp0,
                                           CvUtil.findVerticalSeam(energyMap, validWidth));
                        }

                        which = !which;
                    }

                    // Convert int[][] to a bitmap.
                    return which ? CvUtil.toBitmap(tmp0): CvUtil.toBitmap(tmp1);
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Bitmap>() {
                @Override
                public void onNext(Bitmap bitmap) {
                    Log.d("xyz", "Display the bitmap.");
                    // Display the bitmap.
                    setImageBitmap(bitmap);
                }

                @Override
                public void onError(Throwable e) {
                    // DO NOTHING.
                }

                @Override
                public void onComplete() {
                    // DO NOTHING.
                }
            });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private Bitmap sampleBitmap(final Uri uri) {
        InputStream stream = null;

        try {
            stream = getContext().getContentResolver()
                                 .openInputStream(uri);
            final Rect imgSize = ImageUtil.getOriginalBitmapSize(
                getContext().getContentResolver(),
                uri);
            final int viewMinDimen = Math.min(
                getWidth() - getPaddingLeft() - getPaddingRight(),
                getHeight() - getPaddingTop() - getPaddingBottom());
            final int imgMaxDimen = Math.max(imgSize.width(), imgSize.height());

            // Determine the sampling factor.
            final BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = viewMinDimen < imgMaxDimen ?
                1 << (2 * imgMaxDimen / viewMinDimen) :
                1;
            // FIXME: Called like 4 times.
            Log.w(TAG, "inSampleSize=" + opt.inSampleSize);

            return BitmapFactory.decodeStream(
                stream,
                null,
                opt);
        } catch (Exception e) {
            Log.w(TAG, "Unable to open content: " + uri, e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.w(TAG, "Unable to close content: " + uri, e);
                }
            }
        }

        return null;
    }
}
