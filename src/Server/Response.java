package Server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Response {
    private final OutputStream os;
    private final Map<String, String> headers;
    private final Map<String, String> mimes;
    private int status;

    public Response(OutputStream os) {
        this.os = os;
        headers = new HashMap<>();
        mimes = new HashMap<>() {
            {
                put(".html", "text/html");
                put(".htm", "text/html");
                put(".shtml", "text/html");
                put(".css", "text/css");

                put(".jpg", "image/jpeg");
                put(".jpeg", "image/jpeg");
                put(".png", "image/png");
                put(".svg", "image/gif");
                put(".gif", "image/gif");

                put(".mp3", "audio/mpeg");
                put(".ogg", "audio/ogg");
                put(".mp4", "video/mp4");

                put(".json", "application/json");
                put(".js", "application/javascript");
            }
        };
        status = 200;
    }

    public Response setHeader(String k, String v) {
        headers.put(k, v);
        return this;
    }

    public Response setStatus(int status) {
        this.status = status;
        return this;
    }

    public void sendFile(File f) {
        // 获取文件扩展名
        String filePath = f.getName();
        String extName = filePath.substring(filePath.lastIndexOf('.'));
        // 匹配正确的 MIME
        // 详见：https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Basics_of_HTTP/MIME_types
        setHeader("Content-Type", mimes.getOrDefault(extName, "application/octet-stream"));

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 ").append(status).append("\n");
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

    public void send(String text) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 ").append(status).append("\n");
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

    public void end() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 ").append(status).append("\n");
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
