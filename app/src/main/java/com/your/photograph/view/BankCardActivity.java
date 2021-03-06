package com.your.photograph.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.bumptech.glide.Glide;
import com.google.android.cameraview.CameraImpl;
import com.hjq.bar.TitleBar;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.your.photograph.R;
import com.your.photograph.bean.BankCardEntry;
import com.your.photograph.util.BitmapUtils;
import com.your.photograph.util.Constant;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.pqpo.smartcameralib.MaskView;
import me.pqpo.smartcameralib.SmartCameraView;
import me.pqpo.smartcameralib.SmartScanner;

public class BankCardActivity extends AppCompatActivity {

    @BindView(R.id.title)
    TitleBar title;
    @BindView(R.id.camera_view)
    SmartCameraView mCameraView;
    @BindView(R.id.img_bg)
    ImageView imgBg;

    private boolean granted = false;
    private Context mContext;

    private String idCardSide;
    int RESULT_OK=200;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card);
        mContext=this;
        ButterKnife.bind(this);

        initAccessToken();
        requestPermissions();
        intView();
        initMaskView();
        initScannerParams();
        initCameraView();

    }

    private void intView(){
        title.setLeftIcon(R.mipmap.bar_icon_back_white);
        //判断传递过来的方向
        idCardSide=getIntent().getStringExtra(Constant.SCANNING_TYPE);
        if (idCardSide.equals(Constant.BANK_CARD_BG)){
            Glide.with(mContext).load(R.mipmap.img_bank_bg).into(imgBg);
        }
    }
    /**
     * 请求权限 同时开启相机
     */
    private void requestPermissions(){
        new RxPermissions(this).request(
                Manifest.permission.CAMERA
                ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                ,Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) { }
                    @Override
                    public void onNext(Boolean granted) {
                        BankCardActivity.this.granted = granted;
                        if (granted) {
                            MaskView maskView = (MaskView) mCameraView.getMaskView();
                            maskView.setShowScanLine(true);
                            mCameraView.start();
                            mCameraView.startScan();
                        } else {
                            Toast.makeText(BankCardActivity.this, "请开启相机权限！", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onComplete() {}
                });
    }

    private void initScannerParams() {
        SmartScanner.DEBUG = true;
        /*
          canny 算符阈值
          1. 低于阈值1的像素点会被认为不是边缘；
          2. 高于阈值2的像素点会被认为是边缘；
          3. 在阈值1和阈值2之间的像素点,若与第2步得到的边缘像素点相邻，则被认为是边缘，否则被认为不是边缘。
         */
        SmartScanner.cannyThreshold1 = 20; //canny 算符阈值1
        SmartScanner.cannyThreshold2 = 50; //canny 算符阈值2
        /*
         * 霍夫变换检测线段参数
         * 1. threshold: 最小投票数，要检测一条直线所需最少的的曲线交点，增大该值会减少检测出的线段数量。
         * 2. minLinLength: 能组成一条直线的最少点的数量, 点数量不足的直线将被抛弃。
         * 3. maxLineGap: 能被认为在一条直线上的点的最大距离，若出现较多断断续续的线段可以适当增大该值。
         */
        SmartScanner.houghLinesThreshold = 130;
        SmartScanner.houghLinesMinLineLength = 80;
        SmartScanner.houghLinesMaxLineGap = 10;
        /*
         * 高斯模糊半径，用于消除噪点，必须为正奇数。
         */
        SmartScanner.gaussianBlurRadius = 3;

        // 检测范围比例, 比例越小表示待检测物体要更靠近边框
        SmartScanner.detectionRatio = 0.1f;
        // 线段最小长度检测比例
        SmartScanner.checkMinLengthRatio = 0.8f;
        // 为了提高性能，检测的图片会缩小到该尺寸之内
        SmartScanner.maxSize = 300;
        // 检测角度阈值
        SmartScanner.angleThreshold = 5;
        // don't forget reload params
        SmartScanner.reloadParams();
    }

    private void initCameraView() {
        mCameraView.getSmartScanner().setPreview(true);
        mCameraView.setOnScanResultListener(new SmartCameraView.OnScanResultListener() {
            @Override
            public boolean onScanResult(SmartCameraView smartCameraView, int result, byte[] yuvData) {
                Bitmap previewBitmap = smartCameraView.getPreviewBitmap();
                if (previewBitmap != null) {
                }
                return false;
            }
        });

        mCameraView.addCallback(new CameraImpl.Callback() {
            @Override
            public void onCameraOpened(CameraImpl camera) {
                super.onCameraOpened(camera);
            }

            @Override
            public void onPictureTaken(CameraImpl camera, byte[] data) {
                super.onPictureTaken(camera, data);
                mCameraView.cropJpegImage(data, new SmartCameraView.CropCallback() {
                    @Override
                    public void onCropped(Bitmap cropBitmap) {
                        if (cropBitmap != null) {
                            showPicture(cropBitmap);
                        }
                    }
                });
            }

        });
    }

    private void initMaskView() {
        final MaskView maskView = (MaskView) mCameraView.getMaskView();
        maskView.setMaskLineColor(0x0000adb5);
        maskView.setShowScanLine(true);
        maskView.setScanLineGradient(0xff0096ff, 0x000096ff);
        maskView.setMaskLineWidth(2);
        maskView.setMaskRadius(5);
        maskView.setScanSpeed(6);
        maskView.setScanGradientSpread(80);
        mCameraView.post(new Runnable() {
            @Override
            public void run() {
                int width = mCameraView.getWidth();
                int height = mCameraView.getHeight();
                if (width < height) {
                    maskView.setMaskSize((int) (width * 0.7f), (int) (width * 0.9f / 0.80));
                    maskView.setMaskOffset(0, (int)(width * 0.0));
                } else {
                    maskView.setMaskSize((int) (width * 0.7f), (int) (width * 0.9f / 0.80));
                }
            }
        });
        mCameraView.setMaskView(maskView);

    }

    private void showPicture(Bitmap bitmap) {
        try {
            String imgPath= BitmapUtils.saveBitmapToSDCard(bitmap,"bank_card");
            recIDCard(imgPath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (granted) {
            mCameraView.start();
            mCameraView.startScan();
        }
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
        mCameraView.stopScan();
    }

    //请求Token
    private void initAccessToken(){
        OCR.getInstance(mContext).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
            }
            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError子类SDKError对象
            }
        }, getApplicationContext());
    }

    //银行卡识别
    public void recIDCard(String filePath) {
        // 银行卡识别参数设置
        BankCardParams param = new BankCardParams();
        param.setImageFile(new File(filePath));
        // 调用银行卡识别服务
        OCR.getInstance(mContext).recognizeBankCard(param, new OnResultListener<BankCardResult>() {
            @Override
            public void onResult(BankCardResult result) {
                // 调用成功，返回BankCardResult对象
               if (TextUtils.isEmpty(result.getBankName())||TextUtils.isEmpty(result.getBankCardNumber())){
                    Toast.makeText(mContext,"请扫描银行卡照片",Toast.LENGTH_SHORT).show();
               }else {
                   BankCardEntry bankCardEntry=new BankCardEntry();
                   bankCardEntry.setBankName(result.getBankName());
                   bankCardEntry.setBankCardNumber(result.getBankCardNumber());
                   bankCardEntry.setBankType(result.getBankCardType().toString());
                   Intent intent=new Intent();
                   intent.putExtra(Constant.BANK_CARD_BG,bankCardEntry);
                   setResult(RESULT_OK,intent);
                   finish();
               }
            }
            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
            }
        });
    }
}
