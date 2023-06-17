import java.io.*;
import java.net.*;

public class 获取POST请求携带的数据 {

    public static void main(String[] args) {
        try {
            // 创建一个ServerSocket对象，监听8080端口
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("服务器启动，等待客户端连接...");

            // 接受客户端的连接请求，返回一个Socket对象
            Socket socket = serverSocket.accept();
            System.out.println("客户端连接成功，地址为：" + socket.getInetAddress());

            // 获取输入输出流
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // 读取请求行
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String requestLine = reader.readLine();
            System.out.println("请求行：" + requestLine);

            // 读取请求头
            String headerLine = null;
            int contentLength = 0; // 用于保存Content-Length的值
            while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
                System.out.println("请求头：" + headerLine);
                // 如果是Content-Length字段，获取其值
                if (headerLine.startsWith("Content-Length")) {
                    contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
                }
            }

            // 读取请求体
            char[] buffer = new char[contentLength];
            reader.read(buffer);
            String requestBody = new String(buffer);
            System.out.println("请求体：" + requestBody);

            // 响应客户端
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println("HTTP/1.1 200 OK"); // 响应行
            writer.println("Content-Type: text/plain"); // 响应头
            writer.println(); // 空行
            writer.println("Hello, client!"); // 响应体
            writer.flush();

            // 关闭资源
            writer.close();
            reader.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
