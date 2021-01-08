package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class Main2Activity extends AppCompatActivity {

    private static final String TAG = "Main2Activity";

    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        Intent intent = getIntent();
        int[] rects = intent.getIntArrayExtra("rect");
        Rect rect = new Rect();
        rect.set(rects[1],rects[0],rects[3],rects[2]);
        Log.i(TAG, "onCreate: "+rects[0]);
        Log.i(TAG, "onCreate: "+rects[1]);
        Log.i(TAG, "onCreate: "+rects[2]);
        Log.i(TAG, "onCreate: "+rects[3]);

        setContentView(new DrawViewTest(Main2Activity.this));
    }

    public void drawrect(Rect rect){

        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bootom = rect.bottom;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);          //抗锯齿
        mPaint.setColor(Color.YELLOW);        //画笔颜色,红色
        mPaint.setStyle(Paint.Style.STROKE);  //画笔风格
        mPaint.setTextSize(36);             //绘制文字大小，单位px
        mPaint.setStrokeWidth(5);           //画笔粗细

        Canvas canvas = new Canvas();
        canvas.drawColor(Color.RED);

        canvas.drawRect(left, top, right, bootom, mPaint);

    }

}
