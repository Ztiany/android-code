package com.hencoder.hencoderpracticedraw1.practice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Practice8DrawArcView extends View {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mWidth;
    private int mCenterX;
    private int mCenterY;
    private RectF mRect = new RectF();

    public Practice8DrawArcView(Context context) {
        super(context);
    }

    public Practice8DrawArcView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice8DrawArcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w / 6;
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        练习内容：使用 canvas.drawArc() 方法画弧形和扇形
        mRect.set(mCenterX - mWidth, mCenterY - mWidth/2, mCenterX + mWidth, mCenterY + mWidth/2);
        //step1
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(mRect, 180, 60, false, mPaint);
        //step2
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawArc(mRect, 250, 90, true, mPaint);
        //step3
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawArc(mRect, 30, 120, false, mPaint);
    }
}
