package com.amzlab;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final OutputStream os;
    private final Map<String, String> headers;
    private final Map<String, String> mimes;
    private int status = 200;

    public Response(OutputStream os) {
        this.os = os;
        headers = new HashMap<>();

        mimes = new HashMap<>();
        mimes.put(".html", "text/html");
        mimes.put(".htm", "text/html");
        mimes.put(".shtml", "text/html");
        mimes.put(".css", "text/css");
        mimes.put(".jpg", "image/jpeg");
        mimes.put(".jpeg", "image/jpeg");
        mimes.put(".png", "image/png");
        mimes.put(".svg", "image/gif");
        mimes.put(".gif", "image/gif");
        mimes.put(".mp3", "audio/mpeg");
        mimes.put(".ogg", "audio/ogg");
        mimes.put(".mp4", "video/mp4");
        mimes.put(".json", "application/json");
        mimes.put(".js", "application/javascript");
    }

    /**
     * [模板]创建响应报文
     * @return 一个 StringBuilder
     */
    public StringBuilder createResponseMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append("\n");

        return sb;
    }

    /**
     * 设置请求头
     *
     * @param k 请求头类型
     * @param v 请求头值
     * @return 链式调用
     */
    public Response setHeader(String k, String v) {
        headers.put(k, v);
        return this;
    }

    /**
     * 设置 session
     *
     * @param session Session 对象
     */
    public void setSession(Session session) {
        // 会话 ID
        String sessionString = "JSESSIONID=" + session.id + "; " +
                // 会话过期时间
                "expire=" + session.expireTime + "; " +
                // 防止 xss 攻击
                "HttpOnly";
        // 存起来这个 session
        Session.sessionMap.put(session.id, session);
        // 要求客户端设置 session
        setHeader("Set-Cookie", sessionString);
    }

    /**
     * 设置 HTTP 状态码
     *
     * @param status 状态码
     * @return 链式调用
     */
    public Response setStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * 发送文件并结束响应
     *
     * @param f 文件
     */
    public void sendFile(File f) {
        // 获取文件扩展名
        String filePath = f.getName();
        String extName = filePath.substring(filePath.lastIndexOf('.'));
        // 匹配正确的 MIME
        // 详见：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Basics_of_HTTP/MIME_types
        setHeader("Content-Type", mimes.getOrDefault(extName, "application/octet-stream"));

        try {
            StringBuilder sb = createResponseMessage();

            for (Map.Entry<String, String> p : headers.entrySet()) {
                sb.append(p.getKey());
                sb.append(": ");
                sb.append(p.getValue());
                sb.append("\n");
            }
            // 两个换行用来间隔响应参数和响应体
            sb.append("\n");

            // 响应体
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();

            os.write(sb.toString().getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送文本并结束响应
     *
     * @param text 文本
     */
    public void send(String text) {
        try {
            StringBuilder sb = createResponseMessage();

            // 如果没有设置 Content-Type 则默认 text/html; charset=utf-8
            if (!headers.containsKey("Content-Type")) {
                setHeader("Content-Type", "text/html; charset=utf-8");
            }

            for (Map.Entry<String, String> p : headers.entrySet()) {
                sb.append(p.getKey());
                sb.append(": ");
                sb.append(p.getValue());
                sb.append("\n");
            }
            // 两个换行用来间隔响应参数和响应体
            sb.append("\n");

            // 响应体
            sb.append(text);

            os.write(sb.toString().getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 响应 json 格式数据
     * @param jsonString json 字符串
     */
    public void json(String jsonString) {
        try {
            StringBuilder sb = createResponseMessage();

            for (Map.Entry<String, String> p : headers.entrySet()) {
                sb.append(p.getKey()).append(": ").append(p.getValue()).append("\n");
            }
            sb.append("Content-Type: application/json; charset=utf-8\n");
            // 两个换行用来间隔响应参数和响应体
            sb.append("\n");

            // 添加 json 载荷
            sb.append(jsonString);

            os.write(sb.toString().getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 结束响应
     */
    public void end() {
        try {
            StringBuilder sb = createResponseMessage();

            for (Map.Entry<String, String> p : headers.entrySet()) {
                sb.append(p.getKey()).append(": ").append(p.getValue()).append("\n");
            }

            os.write(sb.toString().getBytes());
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
