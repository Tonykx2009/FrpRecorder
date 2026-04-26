package com.example.webrtcrecorder;

import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileInputStream;
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
    public Response serve(IHTTPSession session) {
        Map<String, String> params = session.getParms();
        String pwd = params.get("pwd");
        
        if (pwd == null || !pwd.equals(password)) {
            return newFixedLengthResponse(
                Response.Status.UNAUTHORIZED,
                "text/html;charset=utf-8",
                "<h2>请输入正确密码</h2>"
            );
        }

        String uri = session.getUri();
        if (uri.equals("/")) {
            return buildListResponse();
        }
        return buildVideoResponse(new File(videoDir + uri));
    }

    private Response buildListResponse() {
        File[] files = new File(videoDir).listFiles((d, n) -> n.endsWith(".mp4"));
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=utf-8></head><body><h3>视频列表</h3><ul>");
        
        if (files != null) {
            for (File f : files) {
                html.append("<li><a href=\"").append(f.getName()).append("\">")
                    .append(f.getName()).append("</a></li>");
            }
        }
        
        html.append("</ul></body></html>");
        return newFixedLengthResponse(
            Response.Status.OK,
            "text/html;charset=utf-8",
            html.toString()
        );
    }

    private Response buildVideoResponse(File file) {
        try {
            return newFixedLengthResponse(
                Response.Status.OK,
                "video/mp4",
                new FileInputStream(file),
                file.length()
            );
        } catch (Exception e) {
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain;charset=utf-8",
                "文件不存在"
            );
        }
    }
}
