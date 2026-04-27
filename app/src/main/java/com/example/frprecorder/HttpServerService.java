package com.example.frprecorder;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpServerService {
    private static final int PORT = 8080;
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
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();
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

    private class ClientHandler extends Thread {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 OutputStream out = socket.getOutputStream()) {

                String requestLine = in.readLine();
                if (requestLine == null || !requestLine.startsWith("GET")) {
                    sendResponse(out, "400 Bad Request", "text/plain", "Invalid Request");
                    return;
                }

                String[] parts = requestLine.split(" ");
                String path = parts.length > 1 ? parts[1] : "/";
                Map<String, String> params = parseParams(path);

                if (!password.equals(params.get("pwd"))) {
                    sendResponse(out, "200 OK", "text/html; charset=utf-8", "<h2>请输入正确密码</h2>");
                    return;
                }

                File dir = new File(videoDir);
                if (path.equals("/") || path.startsWith("/?")) {
                    sendFileList(out, dir);
                    return;
                }

                File file = new File(dir, path.substring(1));
                if (file.exists() && file.isFile()) {
                    sendVideo(out, file);
                    return;
                }

                sendResponse(out, "404 Not Found", "text/plain", "File not found");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception ignored) {}
            }
        }

        private Map<String, String> parseParams(String path) {
            Map<String, String> params = new HashMap<>();
            int queryIndex = path.indexOf('?');
            if (queryIndex != -1) {
                String query = path.substring(queryIndex + 1);
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        params.put(kv[0], kv[1]);
                    }
                }
            }
            return params;
        }

        private void sendFileList(OutputStream out, File dir) throws Exception {
            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset=utf-8></head><body><h3>视频列表</h3><ul>");
            File[] files = dir.listFiles(f -> f.getName().endsWith(".mp4"));
            if (files != null) {
                for (File f : files) {
                    html.append("<li><a href=\"").append(f.getName()).append("\">").append(f.getName()).append("</a></li>");
                }
            }
            html.append("</ul></body></html>");
            sendResponse(out, "200 OK", "text/html; charset=utf-8", html.toString());
        }

        private void sendVideo(OutputStream out, File file) throws Exception {
            out.write("HTTP/1.1 200 OK\r\nContent-Type: video/mp4\r\n\r\n".getBytes());
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
            out.flush();
        }

        private void sendResponse(OutputStream out, String status, String type, String content) throws Exception {
            String response = "HTTP/1.1 " + status + "\r\nContent-Type: " + type + "\r\n\r\n" + content;
            out.write(response.getBytes());
            out.flush();
        }
    }
}
