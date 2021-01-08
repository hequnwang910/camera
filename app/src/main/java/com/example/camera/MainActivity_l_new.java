package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
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
import com.example.camera.model.DrawInfo;
import com.example.camera.util.DrawHelper;
import com.example.camera.util.camera.CameraHelper;
import com.example.camera.util.camera.CameraListener;
import com.example.camera.util.face.RecognizeColor;
import com.example.camera.widget.FaceRectView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity_l_new extends AppCompatActivity  {

    /*
    * 显示人脸框
    * */

    private FaceRectView faceRectView;
    private DrawHelper drawHelper;
    private CameraHelper cameraHelper;




    private static final String TAG = "MainActivity_l_new";
    /*
     * arc_whq
     * */
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
    private int initMask = FaceEngine.ASF_FACE_DETECT |FaceEngine.ASF_LIVENESS | FaceEngine.ASF_AGE |
            FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE |FaceEngine.ASF_FACE_RECOGNITION  ;
    private int processMask;

    /*
     * whq
     * */



    private static final int REQUEST_CAMERA = 0x01;

    private CameraSurfaceView mCameraSurfaceView;
    private Button mBtnInit;
    private Button mBtnSwitch;
    private Button mBtnfocus;


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
        setContentView(R.layout.activity_main_l_new);
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
    }

    /**
     * 初始化View
     */
    private void initView() {
        mAspectLayout = (FrameLayout) findViewById(R.id.layout_aspect);;
        mCameraSurfaceView = new CameraSurfaceView(this);
        mAspectLayout.addView(mCameraSurfaceView);
        mOrientation = CameraUtils.calculateCameraPreviewOrientation(MainActivity_l_new.this);
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
        mBtnfocus =findViewById(R.id.btn_focus);

        face_num = findViewById(R.id.face_num);
        age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);
        liveness = findViewById(R.id.liveness);

        /*
        * 显示人脸框
        * */
        faceRectView = findViewById(R.id.face_rect_view);





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

        int code = FaceEngine.activeOnline(MainActivity_l_new.this, APP_ID, SDK_KEY);
        if(code == ErrorInfo.MOK){
            Log.i(TAG, "activeOnline success");
            Toast.makeText(MainActivity_l_new.this,"激活成功",Toast.LENGTH_SHORT).show();
        }else if(code == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED){
            Log.i(TAG, "already activated");
            Toast.makeText(MainActivity_l_new.this,"已经激活",Toast.LENGTH_SHORT).show();
        }else{
            Log.i(TAG, "activeOnline failed, code is : " + code);
        }

        /*
         * 初始化
         * */
        faceEngine = new FaceEngine();
//        int code_init = faceEngine.init(getApplicationContext(),DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_0_ONLY, scale,maxFaceNum, initMask);
        int code_init = faceEngine.init(getApplicationContext(), DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_ALL_OUT, scale,maxFaceNum, initMask);
        if (code_init != ErrorInfo.MOK) {
            Toast.makeText(this, "init failed, code is : " + code,
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "init success");
            Toast.makeText(MainActivity_l_new.this,"初始化成功",Toast.LENGTH_SHORT).show();
        }
        /*
         * 检测属性
         * */

        Camera camera = CameraUtils.getmCamera();
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
                * 显示人脸框
                * */
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                CameraListener cameraListener = new CameraListener() {

                    @Override
                    public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {

                        drawHelper = new DrawHelper(previewSize.width, previewSize.height, mCameraSurfaceView.getWidth(), mCameraSurfaceView.getHeight(), displayOrientation
                                , cameraId, isMirror, false, false);

                    }

                    @Override
                    public void onPreview(byte[] data, Camera camera) {

                    }

                    @Override
                    public void onCameraClosed() {
                        Log.i(TAG, "onCameraClosed: ");

                    }

                    @Override
                    public void onCameraError(Exception e) {
                        Log.i(TAG, "onCameraError: " + e.getMessage());

                    }

                    @Override
                    public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                        if (drawHelper != null) {
                            drawHelper.setCameraDisplayOrientation(displayOrientation);
                        }
                        Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);

                    }
                };
                cameraHelper = new CameraHelper.Builder()
                        .previewViewSize(new Point(mCameraSurfaceView.getMeasuredWidth(), mCameraSurfaceView.getMeasuredHeight()))
                        .rotation(getWindowManager().getDefaultDisplay().getRotation())
                        .specificCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT)
                        .isMirror(false)
                        .previewOn(mCameraSurfaceView)
                        .cameraListener(cameraListener)
                        .build();
                cameraHelper.init();
                cameraHelper.start();



                /*
                 * 显示人脸个数
                 * */
                faceInfoList = new ArrayList<>();
//                long start = System.currentTimeMillis();
                int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height,
                        FaceEngine.CP_PAF_NV21, faceInfoList);
                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                    Log.i(TAG, "detectFaces, face num is : "+ faceInfoList.size());
                    //  Toast.makeText(MainActivity.this,"检测成功",Toast.LENGTH_SHORT).show();

                } else {
                    Log.i(TAG, "no face detected, code is : " + code);
                    //   Toast.makeText(MainActivity.this,"检测失败",Toast.LENGTH_SHORT).show();
                }
                face_num.setText("人脸数："+faceInfoList.size());

                /*
                 * 显示年龄
                 * */
                processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER |FaceEngine.ASF_FACE3DANGLE |FaceEngine.ASF_LIVENESS;
                int faceProcessCode = faceEngine.process(nv21, previewSize.width, previewSize.height,
                        FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                if (faceProcessCode == ErrorInfo.MOK) {
                    Log.i(TAG, "process success");
                    //    Toast.makeText(MainActivity.this,"属性成功",Toast.LENGTH_SHORT).show();
                }else{
                    Log.i(TAG, "process failed, code is : " + faceProcessCode);
                    //   Toast.makeText(MainActivity.this,"属性失败",Toast.LENGTH_SHORT).show();
                }
                List<AgeInfo> ageInfoList = new ArrayList<>();
                int ageCode = faceEngine.getAge(ageInfoList);
                // 获取第一个人脸的年龄信息，多人脸情况进行循环即可
                if (ageCode == ErrorInfo.MOK && ageInfoList.size() > 0) {
                    if (ageInfoList.size() > 0){
                        Log.i(TAG, "age of the first face is : " + ageInfoList.get(0).getAge());
//                        age.setText("年龄："+ageInfoList.get(0).getAge());
                    }else{
                        Log.i(TAG, "no face processed");
//                        age.setText("年龄：--");
                    }
                }else {
                    Log.i(TAG, "get age failed, code is : " + ageCode);
                }

                if(ageInfoList.size() == 1){
                    age.setText("年龄："+ageInfoList.get(0).getAge());
                }
                if (ageInfoList.size() == 0){
                    age.setText("年龄：--");
                }

                /*
                 * 显示性别
                 * */
                List<GenderInfo> genderInfoList = new ArrayList<>();
                int genderCode = faceEngine.getGender(genderInfoList);
                // 获取第一个人脸的性别信息，多人脸情况进行循环即可
                if (genderCode == ErrorInfo.MOK) {
                    if (genderInfoList.size() > 0){
                        Log.i(TAG, "gender of the first face is : " +
                                genderInfoList.get(0).getGender());
                    }else{
                        Log.i(TAG, "no face processed");
                    }
                }else {
                    Log.i(TAG, "get gender failed, code is : " + genderCode);
                }

                if (genderInfoList.size() > 0&& genderInfoList.get(0).getGender() == 0){
                    gender.setText("年龄：男");
                }
                if (genderInfoList.size() > 0&& genderInfoList.get(0).getGender() == 1){
                    gender.setText("年龄：女");
                }
                if (genderInfoList.size() == 0){
                    gender.setText("年龄：--");
                }

                /*
                 * 活体
                 * */
                List<LivenessInfo> livenessInfoList = new ArrayList<>();
                int livenessCode = faceEngine.getLiveness(livenessInfoList);
// RGB活体不支持多人脸，因此只能拿第1个活体信息
                if (livenessCode == ErrorInfo.MOK) {
                    if (livenessInfoList.size() > 0){
                        Log.i(TAG, "liveness of the first face is : " +
                                livenessInfoList.get(0).getLiveness());
                    }else{
                        Log.i(TAG, "no face processed");
                    }
                }else {
                    Log.i(TAG, "get liveness failed, code is : " + livenessCode);
                }
                if (livenessInfoList.size() > 0&& livenessInfoList.get(0).getLiveness()==1){
                    liveness.setText("活体：是");
                }
                else {
                    liveness.setText("活体：--");
                }




                /*
                * 显示人脸框
                * */

                if (faceRectView != null && drawHelper != null) {
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        drawInfoList.add(new DrawInfo(drawHelper.adjustRect(faceInfoList.get(i).getRect()), genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(), livenessInfoList.get(i).getLiveness(), RecognizeColor.COLOR_UNKNOWN, null));
                    }
                    drawHelper.draw(faceRectView, drawInfoList);
                }

















            }
        });

    }


    /*
     * 聚焦whq
     * */
    public void focus_new(View view){
        CameraUtils.focus();
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (mCameraSurfaceView != null) {
            CameraUtils.switchCamera(1 - CameraUtils.getCameraID(), mCameraSurfaceView.getHolder());
            // 切换相机后需要重新计算旋转角度
            mOrientation = CameraUtils.calculateCameraPreviewOrientation(MainActivity_l_new.this);

        }
    }



    /*
     *显示人脸框
     * */

    @Override
    protected void onDestroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        super.onDestroy();
    }
}