package com.example.agorademo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class AgoraEngineHelper {
    private static final String TAG = "AgoraEngineHelper";
    private RtcEngine mEngine;
    private static Context mContext;
    private RtcEngineEventHandler eventHandler = new RtcEngineEventHandler();

    public static class Holder {
        private static final AgoraEngineHelper INSTANCE = new AgoraEngineHelper();
    }

    private AgoraEngineHelper() {
    }

    public static AgoraEngineHelper getInstance(Context context) {
        mContext = context;
        return Holder.INSTANCE;
    }

    public void registerRtcClientEnv(RtcEngineEventHandler.IRtcEngineEnv env) {
        if (env != null) {
            eventHandler.registerRtcEngineEnv(env);
        }
    }

    public RtcEngine getRtcEngine() {
        return mEngine;
    }

    public void initializeAgoraEngine() {
        try {
            mEngine = RtcEngine.create(mContext, mContext.getString(R.string.agora_app_id), eventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("Need to check rtc sdk init fetal error\n" + Log.getStackTraceString(e));
        }
    }

    //Video的初始化准备
    public void setupVideoProfile() {
        if (mEngine != null) {
            mEngine.enableVideo();
            mEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15, VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
        }
    }

    public void setupLocalVideo(ViewGroup container) {
        if (container != null) {
            SurfaceView surfaceView = RtcEngine.CreateRendererView(mContext);
            surfaceView.setZOrderMediaOverlay(true);
            container.addView(surfaceView);
            mEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
        }
    }

    public void startPreview() {
        if (mEngine != null) {
            mEngine.startPreview();
        }
    }

    public void setChannelProfile(int channelMode) {
        if (mEngine != null) {
            mEngine.setChannelProfile(channelMode);
        }
    }

    public void joinChannel() {
        String token = mContext.getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token)) {
            token = null;
        }

        setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        mEngine.joinChannel(token, "channel1", "Extra Data", 0);
    }

    public void leaveChannel() {
        if (mEngine != null) {
            mEngine.stopPreview();
            mEngine.leaveChannel();
        }
    }

    public void shutDown() {
        RtcEngine.destroy();
        mEngine = null;
    }

    public void switchCamera() {
        if (mEngine != null) {
            mEngine.switchCamera();
        }
    }

    public void muteLocalAudioStream(boolean muted) {
        if (mEngine != null) {
            mEngine.muteLocalAudioStream(muted);
        }
    }

    public void muteLocalVideoStream(boolean muted) {
        if (mEngine != null) {
            mEngine.muteLocalVideoStream(muted);
        }
    }
}
