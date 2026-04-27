package com.example.frprecorder;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpServerService {
    private final int PORT = 8080;
    private final String videoDir;
    private final String password;
    private ServerSocket serverSocket;
    private boolean isRunning;

    public HttpServerService(String videoDir, String password) {
        this.videoDir = videoDir;
        this.password = password;
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                isRunning = true;
                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    new Handler(socket).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (Exception ignored) {}
    }

    private class Handler extends Thread {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream out = socket.getOutputStream();

                String line = in.readLine();
                if (line == null || !line.startsWith("GET")) {
                    send404(out);
                    return;
                }

                String path = line.split(" ")[1];
                Map<String, String> params = parseParams(path);

                if (!password.equals(params.get("pwd"))) {
                    sendHtml(out, "<h2>密码错误</h2>");
                    return;
                }

                File dir = new File(videoDir);
                if (path.equals("/")) {
                    sendFileList(out, dir);
                    return;
                }

                File file = new File(dir, path.substring(1));
                if (file.exists() && file.isFile()) {
                    sendVideo(out, file);
                    return;
                }

                send404(out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception ignored) {}
            }
        }

        private Map<String, String> parseParams(String path) {
            Map<String, String> map = new HashMap<>();
            if (path.contains("?")) {
                String query = path.split("\\?")[1];
                for (String pair : query.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) map.put(kv[0], kv[1]);
                }
            }
            return map;
        }

        private void sendHtml(OutputStream out, String html) throws Exception {
            String resp = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n\r\n" + html;
            out.write(resp.getBytes());
            out.flush();
        }

        private void sendFileList(OutputStream out, File dir) throws Exception {
            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><meta charset=utf-8></head><body><h3>视频列表</h3><ul>");
            File[] files = dir.listFiles(f -> f.getName().endsWith(".mp4"));
            if (files != null) {
                for (File f : files) {
                    sb.append("<li><a href=\"").append(f.getName()).append("\">").append(f.getName()).append("</a></li>");
                }
            }
            sb.append("</ul></body></html>");
            sendHtml(out, sb.toString());
        }

        private void sendVideo(OutputStream out, File file) throws Exception {
            out.write("HTTP/1.1 200 OK\r\nContent-Type: video/mp4\r\n\r\n".getBytes());
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) out.write(buffer, 0, len);
            fis.close();
            out.flush();
        }

        private void send404(OutputStream out) throws Exception {
            out.write("HTTP/1.1 404 Not Found\r\n\r\nFile not found".getBytes());
            out.flush();
        }
    }
}
