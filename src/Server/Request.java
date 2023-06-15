package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final InputStream is;
    private final List<String> message;
    private final Map<String, String> messageMap;
    public String method;
    public String fullPath;
    public String path;
    public String httpVersion;
    public Map<String, String> body;
    public boolean ok = false;

    public Request(InputStream is) {
        this.is = is;
        message = new ArrayList<>();
        messageMap = new HashMap<>();
        method = "null";
        body = new HashMap<>();

        parseHttpMessage();

        if (method.equalsIgnoreCase("POST")) parsePOST();

        logger();
    }

    private void parseHttpMessage(){
        /*
            一个关于 BufferedReader 的大坑
            readLine 方法只有遇到换行符才会停止并返回 null
            如果遇到空行，则会死循环（其内部有一个循环）
            如果在这里 close 掉 br, 则外部的 socket 也会被关闭。
        */

        // 解析 http 请求报文
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                message.add(line);
            }
            if (message.size() > 0) ok = true;
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }

        if (!ok) return;
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
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                body.put(kv[0], kv[1]);
            }
        } else path = fullPath;

        // HTTP 版本
        httpVersion = baseParams[2].trim();
    }

    private void parsePOST() {
        if (!ok) return;

        /*
            multipart/form-data
            https://zhuanlan.zhihu.com/p/195726295
         */
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
