package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private final InputStream is;
    private final List<String> message;
    public String method;
    public String path;
    public String httpVersion;

    public Request(InputStream is) throws IOException {
        this.is = is;
        message = new ArrayList<>();

        // 存储 http 请求报文
//        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//        String line;
//        while ((line = br.readLine()) != null) {
//            message.add(line);
//        }
//        br.close();

        try {
            // 处理基本请求参数
            message.add(new BufferedReader(new InputStreamReader(is)).readLine());
            String[] baseParams = message.get(0).split(" ");
            method = baseParams[0].trim();
            path = baseParams[1].trim();
            httpVersion = baseParams[2].trim();
        } catch (Exception e) {
            System.out.println("1231232131231212123");
            e.printStackTrace();
        }
    }

    public List<String> getMessage() {
        return message;
    }

    public InputStream getStream() {
        return is;
    }
}
