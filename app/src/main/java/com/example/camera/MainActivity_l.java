package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.example.camera.Model.DrawInfo;
import com.example.camera.utils.DrawHelper;
import com.example.camera.utils.face.RecognizeColor;
import com.example.camera.widget.FaceRectView;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/*
* 不使用接口来点击触发响应
* */

public class MainActivity_l extends AppCompatActivity {


    private static final String TAG = "MainActivity_l";
    /*
     * arc_whq
     * */

    /*
     * 1.12人脸框
     * */
    private DrawHelper drawHelper;
    private FaceRectView faceRectView;

    public Rect rect1;

    String image_test;////用于保存图片
    public Rect fmrect;


    public byte[] nv21;
    public Camera.Size previewSize;
    private FaceEngine faceEngine;
    private TextView face_num;
    private TextView age;
    private TextView gender;
    private TextView liveness;
    private int cameraPosition = 1;//0代表前置摄像头，1代表后置摄像头
    private Integer rgbCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private List<FaceInfo> faceInfoList;
    public static final int ASF_FACE_DETECT = 0x00000001; //人脸检测
    public static final int ASF_FACE_RECOGNITION = 0x00000004; //人脸特征
    public static final int ASF_AGE = 0x00000008; //年龄
    public static final int ASF_GENDER = 0x00000010; //性别
    public static final int ASF_FACE3DANGLE = 0x00000020; //3D角度
    public static final int ASF_LIVENESS = 0x00000080; //RGB活体
    public static final int ASF_IR_LIVENESS = 0x00000400; //IR活体
    private int scale = 32;
    private int maxFaceNum = 5;
    // 如下的组合，初始化的功能包含：人脸检测、人脸识别、RGB活体检测、年龄、性别、人脸3D角度
    private int initMask = FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_LIVENESS | FaceEngine.ASF_AGE |
            FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_FACE_RECOGNITION;
    private int processMask;

    /*
     * whq
     * */


    private static final int REQUEST_CAMERA = 0x01;

    private CameraSurfaceView mCameraSurfaceView;
    private Button mBtnInit;
    private Button mBtnSwitch;
    private Button mBtnfocus;
    private Button mBtnfacerect;



    private int mOrientation;

    // CameraSurfaceView 容器包装类
    private FrameLayout mAspectLayout;
    private boolean mCameraRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_l);
        // Android 6.0相机动态权限检查
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_CAMERA);
        }


        /*
         * 用于保存图片
         * */
        String rootDir = MainActivity_l.this.getExternalFilesDir("pic").getAbsolutePath();
        image_test = rootDir + "/test.png";
    }

    /**
     * 初始化View
     */
    private void initView() {
        mAspectLayout = (FrameLayout) findViewById(R.id.layout_aspect);
        mCameraSurfaceView = new CameraSurfaceView(this);
        mAspectLayout.addView(mCameraSurfaceView);
        mOrientation = CameraUtils.calculateCameraPreviewOrientation(MainActivity_l.this);
        mBtnInit = (Button) findViewById(R.id.btn_init);
        mBtnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init();
            }
        });
        mBtnSwitch = (Button) findViewById(R.id.btn_switch);
        mBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
        mBtnfocus = findViewById(R.id.btn_focus);
        mBtnfacerect = findViewById(R.id.btn_facerect);
        mBtnfacerect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity_l.this, Main2Activity.class);

                int[] rect_d = new int[]{ rect1.top,rect1.left,rect1.bottom,rect1.right};
//                intent.putExtra("rect_top",rect1.top);
//                intent.putExtra("rect_left",rect1.left);
//                intent.putExtra("rect_bottom",rect1.bottom);
//                intent.putExtra("rect_right",rect1.right);
                intent.putExtra("rect",rect_d);
                startActivity(intent);

            }
        });

        face_num = findViewById(R.id.face_num);
        age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);
        liveness = findViewById(R.id.liveness);
        fmrect = new Rect();

        /*
        * 1.13人脸识别框
        * */
        faceRectView = findViewById(R.id.face_rect_view);//




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // 相机权限
            case REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraRequested = true;
                    initView();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraRequested) {
            CameraUtils.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraUtils.stopPreview();
    }


    /**
     * 初始化-whq
     */
    private void init() {
        /*
         * 激活
         * */

        /*
         * whq*/
//        String APP_ID = "71DgVpgMy4B9qDbxfeDpSyo5kLBUryRZ6As9ASkfcqP3";
//        String SDK_KEY = "64LzebCQ1dPLaUZsfDZ2rsQggFacgWSeJeqnptqWGivX";
        /*
         * mez
         * */
        String APP_ID = "jmXWgG7SVQB6bjqeP28HShbFxC7gwzPPf4tkThv1LZS";
        String SDK_KEY = "7VpMWe5RRaSuiQCjBcE6e2mF5NcnyuqehJuKsx5S79Yr";

        int code = FaceEngine.activeOnline(MainActivity_l.this, APP_ID, SDK_KEY);
        if (code == ErrorInfo.MOK) {
            Log.i(TAG, "activeOnline success");
            Toast.makeText(MainActivity_l.this, "激活成功", Toast.LENGTH_SHORT).show();
        } else if (code == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            Log.i(TAG, "already activated");
            Toast.makeText(MainActivity_l.this, "已经激活", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "activeOnline failed, code is : " + code);
        }

        /*
         * 初始化
         * */
        faceEngine = new FaceEngine();
//        int code_init = faceEngine.init(getApplicationContext(),DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_0_ONLY, scale,maxFaceNum, initMask);
        int code_init = faceEngine.init(getApplicationContext(), DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_ALL_OUT, scale, maxFaceNum, initMask);
        if (code_init != ErrorInfo.MOK) {
            Toast.makeText(this, "init failed, code is : " + code,
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "init success");
            Toast.makeText(MainActivity_l.this, "初始化成功", Toast.LENGTH_SHORT).show();
        }
        /*
         * 检测属性
         * */

        Camera camera = CameraUtils.getmCamera();

        /*
         * 1.12whq人脸框
         * */

        previewSize = camera.getParameters().getPreviewSize();
        drawHelper = new DrawHelper(previewSize.width, previewSize.height, mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight(), 90
                , 1, false, false, false);


        camera.setDisplayOrientation(90);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] nv21, Camera camera) {
                // 这里面的Bytes的数据就是NV21格式的数据
                previewSize = camera.getParameters().getPreviewSize();

                /*
                 * 显示人脸个数
                 * */
                faceInfoList = new ArrayList<>();
//                long start = System.currentTimeMillis();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height,
                        FaceEngine.CP_PAF_NV21, faceInfoList);
                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                    Log.i(TAG, "detectFaces, face num is : " + faceInfoList.size());
                    //  Toast.makeText(MainActivity.this,"检测成功",Toast.LENGTH_SHORT).show();

                } else {
                    Log.i(TAG, "no face detected, code is : " + code);
                    //   Toast.makeText(MainActivity.this,"检测失败",Toast.LENGTH_SHORT).show();
                }
                face_num.setText("人脸数：" + faceInfoList.size());

                /*
                 * 显示年龄
                 * */
                processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS;
                int faceProcessCode = faceEngine.process(nv21, previewSize.width, previewSize.height,
                        FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                if (faceProcessCode == ErrorInfo.MOK) {
                    Log.i(TAG, "process success");
                    //    Toast.makeText(MainActivity.this,"属性成功",Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "process failed, code is : " + faceProcessCode);
                    //   Toast.makeText(MainActivity.this,"属性失败",Toast.LENGTH_SHORT).show();
                }
                List<AgeInfo> ageInfoList = new ArrayList<>();
                int ageCode = faceEngine.getAge(ageInfoList);
                // 获取第一个人脸的年龄信息，多人脸情况进行循环即可
                if (ageCode == ErrorInfo.MOK && ageInfoList.size() > 0) {
                    if (ageInfoList.size() > 0) {
                        Log.i(TAG, "age of the first face is : " + ageInfoList.get(0).getAge());
//                        age.setText("年龄："+ageInfoList.get(0).getAge());
                    } else {
                        Log.i(TAG, "no face processed");
//                        age.setText("年龄：--");
                    }
                } else {
                    Log.i(TAG, "get age failed, code is : " + ageCode);
                }

                if (ageInfoList.size() == 1) {
                    age.setText("年龄：" + ageInfoList.get(0).getAge());
                }

                if (ageInfoList.size() == 2){

                        age.setText("年龄："+ageInfoList.get(0).getAge()+" ,"+ageInfoList.get(1).getAge());



                }
                if (ageInfoList.size() == 0) {
                    age.setText("年龄：--");
                }

                /*
                 * 显示性别
                 * */
                List<GenderInfo> genderInfoList = new ArrayList<>();
                int genderCode = faceEngine.getGender(genderInfoList);
                // 获取第一个人脸的性别信息，多人脸情况进行循环即可
                if (genderCode == ErrorInfo.MOK) {
                    if (genderInfoList.size() > 0) {
                        Log.i(TAG, "gender of the first face is : " +
                                genderInfoList.get(0).getGender());
                    } else {
                        Log.i(TAG, "no face processed");
                    }
                } else {
                    Log.i(TAG, "get gender failed, code is : " + genderCode);
                }

                if (genderInfoList.size() > 0 && genderInfoList.get(0).getGender() == 0) {
                    gender.setText("年龄：M");
                }
                if (genderInfoList.size() > 0 && genderInfoList.get(0).getGender() == 1) {
                    gender.setText("年龄：F");
                }


                if (genderInfoList.size() == 2  ){
                    gender.setText("年龄："+gen(genderInfoList.get(0).getGender())+gen(genderInfoList.get(1).getGender()));

                }

                if (genderInfoList.size() == 0) {
                    gender.setText("年龄：--");
                }

                /*
                 * 活体
                 * */
                List<LivenessInfo> livenessInfoList = new ArrayList<>();
                int livenessCode = faceEngine.getLiveness(livenessInfoList);
// RGB活体不支持多人脸，因此只能拿第1个活体信息
                if (livenessCode == ErrorInfo.MOK) {
                    if (livenessInfoList.size() > 0) {
                        Log.i(TAG, "liveness of the first face is : " +
                                livenessInfoList.get(0).getLiveness());
                    } else {
                        Log.i(TAG, "no face processed");
                    }
                } else {
                    Log.i(TAG, "get liveness failed, code is : " + livenessCode);
                }
                if (livenessInfoList.size() > 0 && livenessInfoList.get(0).getLiveness() == 1) {
                    liveness.setText("活体：Y");
                } else {
                    liveness.setText("活体：--");
                }

                if (livenessInfoList.size() == 2  ){
                    liveness.setText("活体："+Live(livenessInfoList.get(0).getLiveness())+Live(livenessInfoList.get(1).getLiveness()));

                }


                /*
                 * 1.12人脸框
                 * */
                if (faceRectView != null && drawHelper != null && faceInfoList.size()>0 && genderInfoList.size()>0 && ageInfoList.size()>0 && livenessInfoList.size()>0) {
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    //System.out.println("参数的数量是："+ faceInfoList.size());
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        drawInfoList.add(new DrawInfo(drawHelper.adjustRect(faceInfoList.get(i).getRect()), genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(), livenessInfoList.get(i).getLiveness(), RecognizeColor.COLOR_UNKNOWN, null));
                    }
                    drawHelper.draw(faceRectView, drawInfoList);
                }




                /*
                * 1.6日人脸框实现
                * */
                Rect rect = faceInfoList.get(0).getRect();

                //////这些新的坐标是对的；划重点
                rect1 = adjustRect(rect, previewSize.width, previewSize.height, mAspectLayout.getWidth(), mAspectLayout.getHeight(), 90, Camera.CameraInfo.CAMERA_FACING_FRONT, false, false, false);


                /*
                 * whq1.5日修改，我现在需要将bitmap提取出来，通过NV21来提取
                 * */

                Bitmap bitmap = nv21ToBitmap(nv21, previewSize.width, previewSize.height);


                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, rect.left, rect.top,rect.right - rect.left,rect.bottom - rect.top);

                ///存人脸框
                getRGBData(bitmap1);



                Log.i(TAG, "faceInfoList: " + faceInfoList);
                Log.i(TAG, "Rect的top: " + faceInfoList.get(0).getRect().top);
                Log.i(TAG, "Rect的left: " + faceInfoList.get(0).getRect().left);
                Log.i(TAG, "Rect的bottom: " + faceInfoList.get(0).getRect().bottom);
                Log.i(TAG, "Rect的right: " + faceInfoList.get(0).getRect().right);

                //打印新的坐标，这些新的坐标是对的；划重点
                Log.i(TAG, "Rect1的top: " + rect1.top);
                Log.i(TAG, "Rect1的left: " + rect1.left);
                Log.i(TAG, "Rect1的bottom: " + rect1.bottom);
                Log.i(TAG, "Rect1的right: " + rect1.right);




            }
        });

    }


    /*
     * whq1.5日修改，我现在需要将bitmap提取出来，通过NV21来提取
     * */

    /*
    * 显示男女
    * */
    public String gen(int ge){

        String re = " ";
        if(ge == 0){
            re = "M";

        }
        if(ge == 1){
            re = "F";

        }
        return re;

    }

    /*
     * 显示男女
     * */
    public String Live(int ge){

        String re = " ";
        if(ge == 0){
            re = "Y";

        }
        if(ge == 1){
            re = "N";

        }
        return re;

    }


    private static Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 80, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


    /*
     *whq 保存照片
     * */

    private void getRGBData(Bitmap bitmap) {
        File file = new File(image_test);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    /*
    * 聚焦whq
    * */
   public void focus(View view){
       CameraUtils.focus();
   }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (mCameraSurfaceView != null) {
            CameraUtils.switchCamera(1 - CameraUtils.getCameraID(), mCameraSurfaceView.getHolder());
            // 切换相机后需要重新计算旋转角度
           mOrientation = CameraUtils.calculateCameraPreviewOrientation(MainActivity_l.this);

        }
    }



    /*
    * 1.6日虹软人脸框设计
    * */

    public Rect adjustRect(Rect ftRect,int mpreviewWidth,int mpreviewHeight, int mcanvasWidth,int mcanvasHeight,int mcameraDisplayOrientation,int mcameraId,boolean misMirror,boolean mmirrorHorizontal,boolean mmirrorVertical) {

        int previewWidth = mpreviewWidth;
        int previewHeight = mpreviewHeight;
        int canvasWidth = mcanvasWidth;
        int canvasHeight = mcanvasHeight;
        int cameraDisplayOrientation = mcameraDisplayOrientation;
        int cameraId = mcameraId;
        boolean isMirror = false;
        boolean mirrorHorizontal = false;
        boolean mirrorVertical = false;

        if (ftRect == null) {
            return null;
        }

        Rect rect = new Rect(ftRect);
        float horizontalRatio;
        float verticalRatio;
        if (cameraDisplayOrientation % 180 == 0) {
            horizontalRatio = (float) canvasWidth / (float) previewWidth;
            verticalRatio = (float) canvasHeight / (float) previewHeight;
        } else {
            horizontalRatio = (float) canvasHeight / (float) previewWidth;
            verticalRatio = (float) canvasWidth / (float) previewHeight;
        }
        rect.left *= horizontalRatio;
        rect.right *= horizontalRatio;
        rect.top *= verticalRatio;
        rect.bottom *= verticalRatio;

        Rect newRect = new Rect();
        switch (cameraDisplayOrientation) {
            case 0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                } else {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                }
                newRect.top = rect.top;
                newRect.bottom = rect.bottom;
                break;
            case 90:
                newRect.right = canvasWidth - rect.top;
                newRect.left = canvasWidth - rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                } else {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                }
                break;
            case 180:
                newRect.top = canvasHeight - rect.bottom;
                newRect.bottom = canvasHeight - rect.top;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                } else {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                }
                break;
            case 270:
                newRect.left = rect.top;
                newRect.right = rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                } else {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                }
                break;
            default:
                break;
        }

        return newRect;
    }




    /*
    * 1.6日bitmap转NV21
    * */

    public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
        if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
            int[] argb = new int[width * height];
            src.getPixels(argb, 0, width, 0, 0, width, height);
            return argbToNv21(argb, width, height);
        } else {
            return null;
        }
    }

    /**
     * ARGB数据转化为NV21数据
     *
     * @param argb   argb数据
     * @param width  宽度
     * @param height 高度
     * @return nv21数据
     */
    private static byte[] argbToNv21(int[] argb, int width, int height) {
        int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int index = 0;
        byte[] nv21 = new byte[width * height * 3 / 2];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
                int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
                int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
                }
                ++index;
            }
        }
        return nv21;
    }
}