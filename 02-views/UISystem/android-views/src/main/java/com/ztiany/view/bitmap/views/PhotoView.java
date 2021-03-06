package com.ztiany.view.bitmap.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.OverScroller;

import com.ztiany.view.R;
import com.ztiany.view.utils.UnitConverter;

import androidx.annotation.Nullable;

public class PhotoView extends View {

    private static final float IMAGE_WIDTH = UnitConverter.dpToPx(300);

    private Bitmap bitmap;
    private Paint paint;

    float originalOffsetX;
    float originalOffsetY;

    private float smallScale;
    private float bigScale;

    private float currentScale;

    private float OVER_SCALE_FACTOR = 1.5f;

    private GestureDetector gestureDetector;

    private boolean isEnlarge;

    private ObjectAnimator scaleAnimator;

    private float offsetX;
    private float offsetY;

    private OverScroller overScroller;

    private ScaleGestureDetector scaleGestureDetector;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public static Bitmap getPhoto(Resources res, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, R.drawable.img_girl_01, options);
        options.inJustDecodeBounds = false;
        options.inDensity = options.outWidth;
        options.inTargetDensity = width;
        return BitmapFactory.decodeResource(res, R.drawable.img_girl_01, options);
    }

    private void init(Context context) {
        bitmap = getPhoto(getResources(), (int) IMAGE_WIDTH);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gestureDetector = new GestureDetector(context, new PhotoGestureDetector());
         gestureDetector.setIsLongpressEnabled(false);
        overScroller = new OverScroller(context);
        scaleGestureDetector = new ScaleGestureDetector(context, new PhotoScaleGestureListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float scaleFaction = (currentScale - smallScale) / (bigScale - smallScale);

        // ?????????????????????small??????scaleFaction = 0????????????
        canvas.translate(offsetX * scaleFaction, offsetY * scaleFaction);

        canvas.scale(currentScale, currentScale, getWidth() / 2f, getHeight() / 2f);

        canvas.drawBitmap(bitmap, originalOffsetX, originalOffsetY, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        originalOffsetX = (getWidth() - bitmap.getWidth()) / 2f;
        originalOffsetY = (getHeight() - bitmap.getHeight()) / 2f;

        if ((float) bitmap.getWidth() / bitmap.getHeight() > (float) getWidth() / getHeight()) {
            // smallScale ????????????
            smallScale = (float) getWidth() / bitmap.getWidth();
            bigScale = (float) getHeight() / bitmap.getHeight() * OVER_SCALE_FACTOR;
        } else {
            smallScale = (float) getHeight() / bitmap.getHeight();
            bigScale = (float) getWidth() / bitmap.getWidth() * OVER_SCALE_FACTOR;
        }
        currentScale = smallScale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // ?????????????????????????????????
        boolean result = scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()) {
            result = gestureDetector.onTouchEvent(event);
        }

        return result;
    }

    class PhotoGestureDetector extends GestureDetector.SimpleOnGestureListener {

        // up ???????????????????????????????????????????????? --- up?????????????????????????????????????????????????????????????????????
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        // ?????? -- ??????300ms??????
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        /**
         * ?????? -- move
         *
         * @param e1        ????????????
         * @param e2        ?????????
         * @param distanceX ????????? - ?????????
         * @param distanceY
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isEnlarge) {
                offsetX -= distanceX;
                offsetY -= distanceY;
                fixOffsets();
                invalidate();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        /**
         * up ???????????? -- ??????50dp/s
         *
         * @param velocityX x??????????????????????????????/s???
         * @param velocityY
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isEnlarge) {
                overScroller.fling((int) offsetX, (int) offsetY, (int) velocityX, (int) velocityY,
                        -(int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                        (int) (bitmap.getWidth() * bigScale - getWidth()) / 2,
                        -(int) (bitmap.getHeight() * bigScale - getHeight()) / 2,
                        (int) (bitmap.getHeight() * bigScale - getHeight()) / 2,
                        300, 300);
                postOnAnimation(new FlingRunner());
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        // ??????100ms?????? -- ??????????????????
        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        // ??????????????? onDown ??????????????????
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // ????????????????????????down????????????????????????????????? -- 40ms -- 300ms
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isEnlarge = !isEnlarge;
            if (isEnlarge) {
                offsetX = (e.getX() - getWidth() / 2f) -
                        (e.getX() - getWidth() / 2f) * bigScale / smallScale;
                offsetY = (e.getY() - getHeight() / 2f) -
                        (e.getY() - getHeight() / 2f) * bigScale / smallScale;
                fixOffsets();
                getScaleAnimation().start();
            } else {
                getScaleAnimation().reverse();
            }
            return super.onDoubleTap(e);
        }

        // ??????????????????down???move???up ?????????
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        // ?????????????????????????????????????????????down???up??????????????????
        // ??????300ms??????TAP??????
        // 300ms???????????? -- ????????????TAP -- onSingleTapConfirmed
        // 300ms ???????????? --- ????????????????????????????????????
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }
    }

    class FlingRunner implements Runnable {

        @Override
        public void run() {
            if (overScroller.computeScrollOffset()) {
                offsetX = overScroller.getCurrX();
                offsetY = overScroller.getCurrY();
                invalidate();
                // ??????????????????????????????
                postOnAnimation(this);
            }
        }
    }

    private void fixOffsets() {
        offsetX = Math.min(offsetX, (bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsetX = Math.max(offsetX, -(bitmap.getWidth() * bigScale - getWidth()) / 2);
        offsetY = Math.min(offsetY, (bitmap.getHeight() * bigScale - getHeight()) / 2);
        offsetY = Math.max(offsetY, -(bitmap.getHeight() * bigScale - getHeight()) / 2);
    }

    private ObjectAnimator getScaleAnimation() {
        if (scaleAnimator == null) {
            scaleAnimator = ObjectAnimator.ofFloat(this, "currentScale", 0);
        }
        scaleAnimator.setFloatValues(smallScale, bigScale);
        return scaleAnimator;
    }

    public float getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale(float currentScale) {
        this.currentScale = currentScale;
        invalidate();
    }

    class PhotoScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

        float initScale;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if ((currentScale > smallScale && !isEnlarge)
                    || (currentScale == smallScale && !isEnlarge)) {
                isEnlarge = !isEnlarge;
            }
            // ????????????
            currentScale = initScale * detector.getScaleFactor();
            invalidate();
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            initScale = currentScale;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

}
