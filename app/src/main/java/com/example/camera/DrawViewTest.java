package com.example.camera;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.graphics.Color;
import android.util.AttributeSet;

public class DrawViewTest extends View{

    private Paint mPaint;

    public DrawViewTest(Context context) {
        super(context);
        init();
    }


    public DrawViewTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawViewTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }



    private void init(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);          //抗锯齿
        mPaint.setColor(Color.YELLOW);        //画笔颜色,红色
        mPaint.setStyle(Paint.Style.STROKE);  //画笔风格
        mPaint.setTextSize(36);             //绘制文字大小，单位px
        mPaint.setStrokeWidth(5);           //画笔粗细
    }

    //重写该方法，在这里绘图
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawable(canvas);
        invalidate();
    }

    public void drawable(Canvas canvas) {



        canvas.drawColor(Color.RED);
//        // 画实心圆
//        canvas.drawCircle(200, 200, 100, mPaint);
        // 画矩形

        canvas.drawRect(107, 708, 822, 1404, mPaint);

    }

}