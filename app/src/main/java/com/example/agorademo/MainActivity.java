package com.example.agorademo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.agora.rtc.RtcEngine;

public class MainActivity extends AppCompatActivity implements RtcEngineEventHandler.IRtcEngineEnv {
    private static final String LOG_TAG = "MainActivity";
    private static final int PERMISSION_REQ_ID = 22;

    // permission WRITE_EXTERNAL_STORAGE is not mandatory for Agora RTC SDK,
    // just incase if you wanna save logs to external sdcard
    private static final String[] REQUESTED_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private AgoraEngineHelper engineHelper;
    private FrameLayout containerFl, remoteContainerFl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engineHelper = AgoraEngineHelper.getInstance(getApplicationContext());
        setContentView(R.layout.activity_main);
        containerFl = findViewById(R.id.local_video_view_container);
        remoteContainerFl = findViewById(R.id.remote_video_view_container);
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(
            REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[2],
            PERMISSION_REQ_ID)) {
            initAgoraEngineAndJoinChannel();
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission: " + permission + " " + requestCode);
        //ContextCompat可以用于检查权限，ActivityCompat用于请求权限
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public ViewGroup getRemoteContainer() {
        return remoteContainerFl;
    }

    @Override
    public Activity getClientActivity() {
        return this;
    }

    @Override
    public RtcEngine getRtcEngine() {
        return engineHelper.getRtcEngine();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(LOG_TAG, "onRequestPermissionResult: requestCode" + requestCode + " " + grantResults[0]);
        switch (requestCode) {
            case PERMISSION_REQ_ID:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED
                    || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    showLongToast("Need permissions "
                        + Manifest.permission.RECORD_AUDIO
                        + "/"
                        + Manifest.permission.CAMERA
                        + "/"
                        + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    finish();
                    break;
                }

                initAgoraEngineAndJoinChannel();
                break;
            default:
                break;
        }
    }

    private void initAgoraEngineAndJoinChannel() {
        if (engineHelper != null) {
            engineHelper.registerRtcClientEnv(this);
            engineHelper.initializeAgoraEngine();//创建rtcEngine
            engineHelper.setupVideoProfile();//初始化Video的配置
            engineHelper.setupLocalVideo(containerFl);//设置SurfaceView
            engineHelper.startPreview();
            engineHelper.joinChannel();//以上初始化完成之后， 开始加入频道
        }
    }

    private void showLongToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    public void onLocalVideoMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        engineHelper.muteLocalVideoStream(iv.isSelected());
        if (containerFl != null) {
            SurfaceView sf = (SurfaceView) containerFl.getChildAt(0);
            sf.setZOrderMediaOverlay(!iv.isSelected());
            sf.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
        }
    }

    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }
        engineHelper.muteLocalAudioStream(iv.isSelected());
    }

    public void onSwitchCameraClicked(View view) {
        engineHelper.switchCamera();
    }

    public void onEncCallClicked(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        engineHelper.leaveChannel();
        engineHelper.shutDown();
    }
}
