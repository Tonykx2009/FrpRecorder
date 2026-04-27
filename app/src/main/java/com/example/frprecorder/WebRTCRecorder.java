package com.example.webrtcrecorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import org.webrtc.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import org.webrtc.SurfaceTextureHelper;

public class WebRTCRecorder {
    private final Context ctx;
    private final String dir;
    private final int w, h, durSec;
    private final long maxSize;
    private final boolean mute;

    private MediaRecorder mr;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running;

    private final CameraVideoCapturer capturer;

    public WebRTCRecorder(Context ctx, EglBase egl, SurfaceViewRenderer sv,
                          String dir, int w, int h, int durSec, long maxSize, boolean mute) {
        this.ctx = ctx;
        this.dir = dir;
        this.w = w;
        this.h = h;
        this.durSec = durSec;
        this.maxSize = maxSize;
        this.mute = mute;

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(ctx).createInitializationOptions());

        capturer = new Camera1Enumerator(false).createCapturer(
                new Camera1Enumerator(false).getDeviceNames()[0], null);

        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", egl.getEglBaseContext());
        capturer.initialize(surfaceTextureHelper, this, sv);
        capturer.startCapture(w, h, 30);
    }

    public void start() {
        running = true;
        startNewSegment();
    }

    private void startNewSegment() {
        if (!running) return;
        cleanOldVideos();

        try {
            mr = new MediaRecorder();
            if (!mute) mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            mr.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mr.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if (!mute) mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mr.setVideoSize(w, h);
            mr.setVideoFrameRate(30);
            mr.setVideoEncodingBitRate(4 * 1024 * 1024);

            String path = dir + "rec_" + System.currentTimeMillis() + ".mp4";
            mr.setOutputFile(path);
            mr.prepare();
            mr.start();

            handler.postDelayed(this::rotateSegment, durSec * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rotateSegment() {
        try {
            mr.stop();
            mr.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startNewSegment();
    }

    private void cleanOldVideos() {
        File[] files = new File(dir).listFiles((d, n) -> n.endsWith(".mp4"));
        if (files == null || files.length == 0) return;

        long total = 0;
        for (File f : files) total += f.length();

        if (total > maxSize) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            files[0].delete();
        }
    }

    public void stop() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        try {
            capturer.stopCapture();
            capturer.dispose();
            mr.stop();
            mr.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
