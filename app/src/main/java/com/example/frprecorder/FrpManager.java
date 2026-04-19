package com.example.webrtcrecorder;

import android.content.Context;
import java.io.*;

public class FrpManager {
    private final Context ctx;
    private Process process;
    private String publicUrl;

    // 临时公网 FRP 服务器（我已配置好）
    private static final String SERVER_IP   = "120.25.102.25";
    private static final int    SERVER_PORT = 7000;
    private static final String TOKEN       = "webrtc2025";
    private static final int    REMOTE_PORT = 18080;

    public FrpManager(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.publicUrl = "http://" + SERVER_IP + ":" + REMOTE_PORT;
    }

    public void start() {
        new Thread(() -> {
            try {
                writeFrpcIni();
                File frpc = extractFrpcFromAssets();

                process = new ProcessBuilder(
                        frpc.getAbsolutePath(),
                        "-c", new File(ctx.getFilesDir(), "frpc.ini").getAbsolutePath()
                ).start();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("start proxy success")) {
                        // 外网穿透成功
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        if (process != null) process.destroy();
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    private File extractFrpcFromAssets() throws IOException {
        File frpcFile = new File(ctx.getFilesDir(), "frpc");
        try (InputStream in = ctx.getAssets().open("frpc");
             FileOutputStream out = new FileOutputStream(frpcFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        frpcFile.setExecutable(true);
        return frpcFile;
    }

    private void writeFrpcIni() throws IOException {
        String ini = "[common]\n" +
                "server_addr = " + SERVER_IP + "\n" +
                "server_port = " + SERVER_PORT + "\n" +
                "token = " + TOKEN + "\n\n" +
                "[web]\n" +
                "type = tcp\n" +
                "local_ip = 127.0.0.1\n" +
                "local_port = 8080\n" +
                "remote_port = " + REMOTE_PORT + "\n";

        FileWriter fw = new FileWriter(new File(ctx.getFilesDir(), "frpc.ini"));
        fw.write(ini);
        fw.close();
    }
}
