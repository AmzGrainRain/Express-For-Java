package com.amzlab;

import java.io.*;
import java.util.*;

public class Request {
    private final InputStream is;
    public List<String> headers;
    public Map<String, String> headersMap;
    public Map<String, String> body;
    public String jsonString;
    public Map<String, String> params;
    public String method = "null";
    public String fullPath = "";
    public String path;
    public String httpVersion = "";
    public boolean ok;

    public Request(InputStream is) {
        this.is = is;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        // 处理 HTTP 报文
        ok = parseHttpMessage(br);
        // 处理 cookie
        parseCookie();
        // 解析 GET/POST 参数
        if (ok && method.equalsIgnoreCase("POST")) ok = parsePOST(br);
        // 打印日志
        if (ok) logger();
    }

    /**
     * 解析 HTTP 请求报文和转换 url
     *
     * @param br BufferedReader
     * @return 是否处理完成
     */
    private boolean parseHttpMessage(BufferedReader br) {
        /*
            一个关于 BufferedReader 的大坑
            readLine 方法只有遇到换行符才会停止并返回 null
            如果遇到空行，则会死循环（其内部有一个循环）
            如果在这里 close 掉 br, 则外部的 socket 也会被关闭。
        */
        try {
            // 解析 http 请求报文
            this.headers = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                headers.add(line);
            }
            if (headers.size() == 0) return false;

            // 转换成键值对
            this.headersMap = new HashMap<>();
            for (int i = 1; i < headers.size(); ++i) {
                String[] kv = headers.get(i).split(":");
                headersMap.put(kv[0].trim(), kv[1].trim());
            }

            String[] baseParams = headers.get(0).split(" ");
            // 请求方式
            method = baseParams[0].trim();
            // 请求路径处理
            fullPath = baseParams[1].trim();
            // HTTP 版本
            httpVersion = baseParams[2].trim();

            // 请求参数处理
            int i = fullPath.lastIndexOf('?');
            if (i != -1) {
                path = fullPath.substring(0, i);
                String[] pairs = fullPath.substring(i + 1).split("&");
                // 转换为键值对
                this.params = new HashMap<>();
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    params.put(kv[0], kv[1]);
                }
            } else path = fullPath;

            return true;
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解析 cookie id
     */
    private void parseCookie() {
        if (!headersMap.containsKey("Cookie") || !headersMap.get("Cookie").matches("^JSESSIONID=.*")) return;
        headersMap.put("Cookie", headersMap.get("Cookie").split("=")[1]);
    }

    /**
     * 处理 POST 请求携带的 multipart/form-data 格式表单
     *
     * @param br BufferedReader
     * @return 是否处理完成
     */
    private boolean parsePOST(BufferedReader br) {
        try {
            // 是否携带荷载
            if (!headersMap.containsKey("Content-Length")) return false;

            // 根据 Content-Length 读取报文
            char[] buffer = new char[Integer.parseInt(headersMap.get("Content-Length"))];
            br.read(buffer, 0, buffer.length);

            // 内容类型
            String contentType = headersMap.get("Content-Type").toLowerCase();

            // multipart/form-data
            if (contentType.contains("multipart/form-data")) {
                String[] types = contentType.split("; ");
                body = FormParser.parseFormData(new String(buffer), types[1].split("=")[1]);
                return true;
            }

            // application/json
            if (contentType.contains("application/json")) {
                jsonString = new String(buffer);
                return true;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 控制台输出函数
     */
    private void logger() {
        System.out.println('[' + method + "] " + fullPath);
    }

    /**
     * 获取输入流实例
     *
     * @return 输入流
     */
    public InputStream getInputStream() {
        return is;
    }
}
