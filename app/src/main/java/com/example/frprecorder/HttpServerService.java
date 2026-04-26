package com.example.webrtcrecorder;

import org.nanohttpd.NanoHTTPD;
import org.nanohttpd.Request;
import org.nanohttpd.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class HttpServerService extends NanoHTTPD {

    private final String videoDir;
    private final String password;

    public HttpServerService(String videoDir, String password) {
        super(8080);
        this.videoDir = videoDir;
        this.password = password;
    }

    @Override
    public Response serve(Request request) {
        Map<String, String> params = request.getParams();
        String pwd = params.get("pwd");

        if (pwd == null || !pwd.equals(password)) {
            return newFixedLengthResponse(Response.Status.UNAUTHORIZED,
                    "text/html; charset=utf-8", "请输入正确密码");
        }

        String uri = request.getUri();
        if ("/".equals(uri)) {
            return buildFileListResponse();
        }

        File targetFile = new File(videoDir, uri);
        return buildVideoResponse(targetFile);
    }

    private Response buildFileListResponse() {
        File dir = new File(videoDir);
        File[] files = dir.listFiles((d, n) -> n.endsWith(".mp4"));

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=utf-8></head><body><h3>视频列表</h3><ul>");

        if (files != null) {
            for (File f : files) {
                html.append("<li><a href=\"").append(f.getName()).append("\">")
                        .append(f.getName()).append("</a></li>");
            }
        }

        html.append("</ul></body></html>");
        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", html.toString());
    }

    private Response buildVideoResponse(File file) {
        try {
            if (!file.exists() || !file.getAbsolutePath().startsWith(videoDir)) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND,
                        "text/plain; charset=utf-8", "文件不存在");
            }
            return newFixedLengthResponse(Response.Status.OK,
                    "video/mp4", new FileInputStream(file), file.length());
        } catch (IOException e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                    "text/plain; charset=utf-8", "读取失败");
        }
    }
}
