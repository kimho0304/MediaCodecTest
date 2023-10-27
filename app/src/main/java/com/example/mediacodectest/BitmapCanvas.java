package com.example.mediacodectest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BitmapCanvas extends SurfaceView implements SurfaceHolder.Callback {
    private final static String TAG = "BitmapCanvas_Debug";
    private static int cnt = 0;
    public static Bitmap resultBitmap = null;
    public BitmapCanvas(Context context) {
        super(context);
        getHolder().addCallback(this);
        setWillNotDraw(false);
    }
    public BitmapCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        setWillNotDraw(false);
    }

    public BitmapCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setWillNotDraw(false);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "drawing cnt: " + (++cnt));
        Log.i(TAG, "bitmap info: " + resultBitmap.getByteCount());
        /*Matrix mat=new Matrix();
        int width = resultBitmap.getWidth();
        int height = resultBitmap.getHeight();
        float h= (float) height;
        float w= (float) width;
        mat.setTranslate( 500, 500 );
        mat.setScale(800/w ,800/h);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(resultBitmap, mat, null);*/
        canvas.drawColor(Color.BLACK);
        canvas.drawRect(new Rect(10,10,200,200), new Paint());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas(null);
            synchronized (holder) {
                draw(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }
}