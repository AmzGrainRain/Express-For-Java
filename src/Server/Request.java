package Server;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private InputStream is = null;
    private List<String> message = null;
    private Map<String, String> messageMap = null;
    public Map<String, String> body = null;
    public Map<String, String> params = null;
    public String method = "null";
    public String fullPath = "";
    public String path = "";
    public String httpVersion = "";
    public boolean ok;

    public Request(InputStream is) {
        this.is = is;

        // 处理 HTTP 报文
        ok = parseHttpMessage();

        // 解析 GET/POST 参数
        ok = ok && method.equalsIgnoreCase("GET") ? parseGET() : parsePOST();

        // 打印日志
        if (ok) logger();
    }

    private boolean parseHttpMessage() {
        /*
            一个关于 BufferedReader 的大坑
            readLine 方法只有遇到换行符才会停止并返回 null
            如果遇到空行，则会死循环（其内部有一个循环）
            如果在这里 close 掉 br, 则外部的 socket 也会被关闭。
        */
        try {
            // 解析 http 请求报文
            this.message = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while (!(line = br.readLine()).isEmpty()) {
                message.add(line);
            }
            if (message.size() == 0) return false;

            // 转换成键值对
            this.messageMap = new HashMap<>();
            for (int i = 1; i < message.size(); ++i) {
                String[] kv = message.get(i).split(":");
                messageMap.put(kv[0].trim(), kv[1].trim());
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean parseGET() {
        try {
            String[] baseParams = message.get(0).split(" ");

            // 请求方式
            method = baseParams[0].trim();

            // 请求路径处理
            fullPath = baseParams[1].trim();

            // 请求参数处理
            int i = fullPath.lastIndexOf('?');
            if (i != -1) {
                path = fullPath.substring(0, i);
                String[] pairs = fullPath.substring(i + 1).split("&");
                // 转换为键值对
                this.body = new HashMap<>();
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    body.put(kv[0], kv[1]);
                }
            } else path = fullPath;

            // HTTP 版本
            httpVersion = baseParams[2].trim();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean parsePOST() {
        // [multipart/form-data] https://zhuanlan.zhihu.com/p/195726295
        try {
            String[] types = messageMap.get("Content-Type").split("; ");
            // 参数格式不匹配
            if (types.length != 2) return false;
            // 仅处理 multipart/form-data 类型的表单数据
            if (!types[0].equalsIgnoreCase("multipart/form-data")) return false;
            // 拿到分隔符
            String splitText = types[1].split("=")[1].trim();

            // 读取报文
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            do {
                b = bis.read();
            } while (b != -1);
            System.out.println(baos);
//            this.params = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void logger() {
        System.out.println('[' + method + "] " + fullPath);
    }

    public List<String> getMessage() {
        return message;
    }

    public InputStream getStream() {
        return is;
    }
}
