package com.manridy.iband.view.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.applib.utils.LogUtil;
import com.manridy.iband.BuildConfig;
import com.manridy.iband.R;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.Watch;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class CameraActivity extends BaseActionActivity {

    final static private int RESULT_IMAGE = 1;

    @BindView(R.id.iv_camera_start)
    ImageView ivCameraStart;
    @BindView(R.id.sv_camera)
    SurfaceView svCamera;
    @BindView(R.id.iv_capture)
    ImageView ivCapture;
    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.iv_switch)
    ImageView ivSwitch;
    @BindView(R.id.rl_view)
    RelativeLayout rlView;

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean isBackCameraOn = true;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        setStatusBarColor(Color.parseColor("#2196f3"));
        tbMenu.setText(R.string.hint_photo);
        setTitleBar(getString(R.string.title_camera));
    }

    @Override
    protected void initVariables() {
        registerEventBus();
    }

    @Override
    protected void initListener() {
        ivCameraStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                svCamera.setVisibility(View.VISIBLE);
                ivCapture.setVisibility(View.VISIBLE);
                ivSwitch.setVisibility(View.VISIBLE);
                ibandApplication.service.watch.sendCmd(BleCmd.setCameraViewOnOff(1), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {

                    }

                    @Override
                    public void onFailure(BleException exception) {

                    }
                });
                mSurfaceHolder = svCamera.getHolder();
                mSurfaceHolder.addCallback(SurfaceHolderCallback);
            }
        });

        ivCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFastDoubleClick()) {
                    capture();
                }
            }
        });

        svCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(null);
            }
        });

        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ibandApplication.service.watch.sendCmd(BleCmd.setCameraViewOnOff(0), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                    }
                    @Override
                    public void onFailure(BleException exception) {
                    }
                });
                open();
            }
        });
    }

    private void open() {
//        File file =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        File parentFlie = new File(file.getParent());
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setDataAndType(Uri.fromFile(parentFlie), "*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        startActivity(intent);

//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Uri contentUri = FileProvider.getUriForFile(getBaseContext(), BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
//            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
//        } else {
//            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        startActivity(intent);




//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri uri = Uri.fromFile(getOutputDir());
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(uri, "image/*");
//        startActivityForResult(intent,CameraActivity.RESULT_CANCELED);

        Intent albumIntent = new Intent(Intent.ACTION_PICK, null);
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(albumIntent,CameraActivity.RESULT_CANCELED);
    }

    SurfaceHolder.Callback SurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            setStartPreview(mCamera, mSurfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (mSurfaceHolder.getSurface() == null) {
                return;
            }
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setStartPreview(mCamera, mSurfaceHolder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            //解决java.lang.RuntimeException: Camera is being used after Camera.release() was called异常
            holder.removeCallback(this);
            releaseCamera();
        }
    };

    /**
     * 初始化相机
     *
     * @return camera
     */
    private Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = Camera.open(Camera.getNumberOfCameras() - 1);
        }
        return camera;
    }

    /**
     * 检查是否具有相机功能
     *
     * @param context context
     * @return 是否具有相机功能
     */
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * 在SurfaceView中预览相机内容
     *
     * @param camera camera
     * @param holder SurfaceHolder
     */
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
//            camera.setDisplayOrientation(180);
            setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
            Camera.Parameters params = mCamera.getParameters();
            params.setPictureFormat(ImageFormat.JPEG);
            float height = 0, width = 0;
//        params.setPictureSize(1024,768);
//        parameters(mCamera);
//        params.setPreviewSize(1280, 720);
//        params.setPictureSize(1280, 720);
            List<Camera.Size> pictureSizeList = params.getSupportedPictureSizes();
            for (Camera.Size size : pictureSizeList) {
                LogUtil.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
            }
            /**从列表中选取合适的分辨率*/
            Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
            if (null == picSize) {
                LogUtil.i(TAG, "null == picSize");
                picSize = params.getPictureSize();
            }
            LogUtil.i(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
            // 根据选出的PictureSize重新设置SurfaceView大小
            float w = picSize.width;
            float h = picSize.height;


            Camera.Size pSize = params.getPictureSize();
            pSize.width = 1280;
            pSize.height = 720;
            for (Camera.Size size : pictureSizeList) {
                LogUtil.i(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
                if(pSize.height<size.height&&1920>=size.height){
                    pSize = size;
                }
            }
            params.setPictureSize(pSize.width,pSize.height);
//            params.setPictureSize(1280, 720);

//            svCamera.setLayoutParams(new FrameLayout.LayoutParams((int) (height*(h/w)), (int) height));

            // 获取摄像头支持的PreviewSize列表
            List<Camera.Size> previewSizeList = params.getSupportedPreviewSizes();

            for (Camera.Size size : previewSizeList) {
                LogUtil.i(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
            }
            Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
            if (null != preSize) {
                LogUtil.i(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
                params.setPreviewSize(preSize.width, preSize.height);
            }

            params.setJpegQuality(100); // 设置照片质量
            if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
            }

            mCamera.cancelAutoFocus();//自动对焦。
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 拍照
     */
    public void capture() {

        // 使用自动对焦功能

        mCamera.takePicture(null, null, mPictureCallback);

    }

    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        LogUtil.i(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                break;
            }
        }

        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Camera回调，通过data[]保持图片数据信息
     */
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            LogUtil.d(TAG, "onPictureTaken() called with: data = [" + data + "], camera = [" + camera + "]");
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                LogUtil.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap oldBitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length);
                Matrix matrix = new Matrix();
                matrix.setRotate(isBackCameraOn?90:270);//判断前后摄像头进行旋转
                if (!isBackCameraOn) {//前置摄像头左右镜像反转
                    matrix.postScale(-1,1);
                }
                Bitmap newBitmap = Bitmap.createBitmap(oldBitmap, 0, 0,
                        oldBitmap.getWidth(), oldBitmap.getHeight(),
                        matrix, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] newData = baos.toByteArray();
                fos.write(newData);
                fos.close();
                sync(pictureFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
            ibandApplication.service.watch.sendCmd(BleCmd.setCameraNotify(0), new BleCallback() {
                @Override
                public void onSuccess(Object o) {

                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
            showToast(getString(R.string.hint_save_success) + pictureFile.getPath().toString());
        }
    };


    private File getOutputMediaFile() {
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(picDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    private File getOutputDir(){
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(picDir.getPath());
    }

    /**
     * 切换前后摄像头
     *
     * @param view view
     */
    public void switchCamera(View view) {
        int cameraCount;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        // 遍历可用摄像头
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (isBackCameraOn) {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    releaseCamera();
                    mCamera = Camera.open(i);
                    setStartPreview(mCamera, mSurfaceHolder);
                    isBackCameraOn = false;
                    break;
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    releaseCamera();
                    mCamera = Camera.open(i);
                    setStartPreview(mCamera, mSurfaceHolder);
                    isBackCameraOn = true;
                    break;
                }
            }
        }
    }

    public void switchCamera() {
        int cameraCount;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        // 遍历可用摄像头
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (isBackCameraOn) {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    releaseCamera();
                    mCamera = Camera.open(i);
                    setStartPreview(mCamera, mSurfaceHolder);
                    isBackCameraOn = false;
                    break;
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    releaseCamera();
                    mCamera = Camera.open(i);
                    setStartPreview(mCamera, mSurfaceHolder);
                    isBackCameraOn = true;
                    break;
                }
            }
        }
    }


    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void sync(File pictureFile) {
//        try {
//            MediaStore.Images.Media.insertImage(mContext.getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(pictureFile);
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.ACTION_CAMERA_CAPTURE) {
            showToast(getString(R.string.hint_camera_start));
            capture();
        } else if (event.getWhat() == EventGlobal.ACTION_CAMERA_EXIT) {
            showToast(getString(R.string.hint_camera_exit));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera != null){
            Log.i(TAG,"mCamera != null");
        }
        if (this.checkCameraHardware(this) && (mCamera == null)) {
            mCamera = getCamera();
            if (mSurfaceHolder != null) {
                Log.i(TAG,"mSurfaceHolder != null");
                mSurfaceHolder = null;
                mSurfaceHolder = svCamera.getHolder();
                mSurfaceHolder.addCallback(SurfaceHolderCallback);
                mCamera = getCamera();
                setStartPreview(mCamera, mSurfaceHolder);
                Watch.getInstance().sendCmd(BleCmd.setCameraViewOnOff(1), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
//                        Log.i(TAG,"isBackCameraOn:"+isBackCameraOn);
//                        if(!isBackCameraOn){
//                            Log.i(TAG,"isBackCameraOn:"+isBackCameraOn);
//                            switchCamera();
//                        }
                    }
                    @Override
                    public void onFailure(BleException exception) {
                        hanler.post(release);
                    }
                });
            }else{
                Log.i(TAG,"mSurfaceHolder = null");
            }
        }
    }

    Handler hanler = new Handler();

    Runnable release = new Runnable() {
        @Override
        public void run() {
            Watch.getInstance().sendCmd(BleCmd.setCameraViewOnOff(0), new BleCallback() {
                @Override
                public void onSuccess(Object o) {

                }
                @Override
                public void onFailure(BleException exception) {
                    hanler.post(release);
                }
            });
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
//        hanler.post(release);
        Watch.getInstance().sendCmd(BleCmd.setCameraViewOnOff(0), new BleCallback() {
            @Override
            public void onSuccess(Object o) {

            }
            @Override
            public void onFailure(BleException exception) {
                hanler.post(release);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Watch.getInstance().sendCmd(BleCmd.setCameraViewOnOff(0), new BleCallback() {
//            @Override
//            public void onSuccess(Object o) {
//
//            }
//
//            @Override
//            public void onFailure(BleException exception) {
//
//            }
//        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        switch (requestCode){
//            case CameraActivity.RESULT_IMAGE :
//                if(resultCode==RESULT_OK&&data!=null){
//                    //判断手机系统版本号
//                    handlerImageOnKitKat(data);
//                }
//                break;
//            default:
//                break;
//        }

        startActivity(CameraActivity.class);
        finish();
    }
}

//    private void handlerImageOnKitKat(Intent data){
//        String imagePath=null;
//        Uri uri=data.getData();
//        if(DocumentsContract.isDocumentUri(this,uri)){
//            //如果是document类型的Uri,则通过document id处理
//            String docId=DocumentsContract.getDocumentId(uri);
//            if("com.android.providers.media.documents".equals(uri.getAuthority())){
//                String id=docId.split(":")[1];//解析出数字格式的id
//                String selection=MediaStore.Images.Media._ID+"="+id;
//                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
//            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
//                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
//                imagePath=getImagePath(contentUri,null);
//            }
//        }else if("content".equalsIgnoreCase(uri.getScheme())){
//            //如果是content类型的URI，则使用普通方式处理
//            imagePath=getImagePath(uri,null);
//        }else if("file".equalsIgnoreCase(uri.getScheme())){
//            //如果是file类型的Uri,直接获取图片路径即可
//            imagePath=uri.getPath();
//        }
//        startPhotoZoom(uri);
//    }