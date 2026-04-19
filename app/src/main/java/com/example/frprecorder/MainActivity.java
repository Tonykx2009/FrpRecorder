package com.example.webrtcrecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    private SurfaceViewRenderer surfaceView;
    private Spinner spRes;
    private CheckBox cbMute;
    private EditText etDur, etMaxSize, etPwd;
    private Button btnStart, btnStop, btnShare;
    private TextView tvStatus;

    private WebRTCRecorder recorder;
    private HttpServerService httpServer;
    private FrpManager frpManager;
    private boolean isRecording = false;

    private final String VIDEO_DIR = Environment.getExternalStorageDirectory() + "/WebRTCRecords/";
    private final EglBase eglBase = EglBase.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initSpinner();
        createDir();
        reqPerm();
    }

    private void initView() {
        surfaceView = findViewById(R.id.surface_view);
        spRes = findViewById(R.id.sp_resolution);
        cbMute = findViewById(R.id.cb_mute);
        etDur = findViewById(R.id.et_duration);
        etMaxSize = findViewById(R.id.et_max_storage);
        etPwd = findViewById(R.id.et_pwd);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnShare = findViewById(R.id.btn_share);
        tvStatus = findViewById(R.id.tv_status);

        surfaceView.init(eglBase.getEglBaseContext(), null);

        etDur.setText("60");
        etMaxSize.setText("1024");
        etPwd.setText("123456");
    }

    private void initSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"480P", "720P", "1080P"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRes.setAdapter(adapter);
        spRes.setSelection(1);
    }

    private void createDir() {
        File dir = new File(VIDEO_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private void reqPerm() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 100);
    }

    public void startClick(View view) {
        if (isRecording) return;

        int dur = Integer.parseInt(etDur.getText().toString());
        long maxSize = Long.parseLong(etMaxSize.getText().toString()) * 1024 * 1024;
        String pwd = etPwd.getText().toString();
        boolean mute = cbMute.isChecked();

        String r = (String) spRes.getSelectedItem();
        int w = 1280, h = 720;
        if (r.equals("480P")) { w = 640; h = 480; }
        if (r.equals("1080P")) { w = 1920; h = 1080; }

        startService(new Intent(this, RecordService.class));

        recorder = new WebRTCRecorder(this, eglBase, surfaceView, VIDEO_DIR, w, h, dur, maxSize, mute);
        httpServer = new HttpServerService(VIDEO_DIR, pwd);
        frpManager = new FrpManager(this);

        try {
            httpServer.start();
            frpManager.start();
            recorder.start();
            isRecording = true;
            tvStatus.setText("录制中 · 外网已连接");
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        } catch (Exception e) {
            Toast.makeText(this, "启动失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void stopClick(View view) {
        if (!isRecording) return;

        if (recorder != null) recorder.stop();
        if (httpServer != null) httpServer.stop();
        if (frpManager != null) frpManager.stop();

        stopService(new Intent(this, RecordService.class));
        isRecording = false;
        tvStatus.setText("已停止");
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
    }

    public void shareClick(View view) {
        String pwd = etPwd.getText().toString();
        String link = frpManager.getPublicUrl() + "?pwd=" + pwd;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "视频回放（外网可看）：\n" + link);
        startActivity(Intent.createChooser(intent, "分享"));
        Toast.makeText(this, "链接已生成", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        stopClick(null);
        surfaceView.release();
        super.onDestroy();
    }
}
