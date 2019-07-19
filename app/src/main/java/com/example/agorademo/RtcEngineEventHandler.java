package com.example.agorademo;

import android.app.Activity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class RtcEngineEventHandler extends IRtcEngineEventHandler {
    private static final String LOG_TAG = "RtcEngineEventHandler";
    private IRtcEngineEnv mRtcEngineEnv;

    public void registerRtcEngineEnv(IRtcEngineEnv env) {
        mRtcEngineEnv = env;
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onJoinChannelSuccess(channel, uid, elapsed);
        Log.i(LOG_TAG, "onJoinChannelSuccess");
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        super.onLeaveChannel(stats);
        Log.i(LOG_TAG, "onLeaveChannel");
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        super.onUserJoined(uid, elapsed);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        getActivity().runOnUiThread(() -> {
            getRemoteContainer().removeAllViews();
        });
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {
        super.onConnectionStateChanged(state, reason);
    }

    @Override
    public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
        super.onFirstLocalVideoFrame(width, height, elapsed);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        getActivity().runOnUiThread(() -> {

            SurfaceView surfaceView = (SurfaceView) getRemoteContainer().getChildAt(0);
            if (surfaceView != null) {
                Object tag = surfaceView.getTag();
                if (tag != null && (Integer) tag == uid) {
                    surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        super.onUserMuteVideo(uid, muted);
    }

    @Override
    public void onUserEnableVideo(int uid, boolean enabled) {
        super.onUserEnableVideo(uid, enabled);
    }

    @Override
    public void onUserEnableLocalVideo(int uid, boolean enabled) {
        super.onUserEnableLocalVideo(uid, enabled);
    }

    @Override
    public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
        super.onVideoSizeChanged(uid, width, height, rotation);
    }

    @Override
    public void onFirstRemoteAudioDecoded(int uid, int elapsed) {
        getActivity().runOnUiThread(() -> {
            ViewGroup container = getRemoteContainer();
            if (container.getChildCount() >= 1) {
                return;
            }

            SurfaceView surfaceView =
                getRtcEngine().CreateRendererView(mRtcEngineEnv.getClientActivity().getBaseContext());
            container.addView(surfaceView);
            getRtcEngine().setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
            surfaceView.setTag(uid);
        });
    }

    @Override
    public void onLocalVideoStateChanged(int localVideoState, int error) {
        super.onLocalVideoStateChanged(localVideoState, error);
    }

    private RtcEngine getRtcEngine() {
        return mRtcEngineEnv.getRtcEngine();
    }

    private Activity getActivity() {
        return mRtcEngineEnv.getClientActivity();
    }

    private ViewGroup getRemoteContainer() {
        return mRtcEngineEnv.getRemoteContainer();
    }

    public interface IRtcEngineEnv {
        ViewGroup getRemoteContainer();

        Activity getClientActivity();

        RtcEngine getRtcEngine();
    }
}
