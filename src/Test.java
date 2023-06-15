import java.io.*;
import java.net.*;
import java.util.regex.*;

public class Test {

    // 定义一个端口号
    public static final int PORT = 8080;

    // 定义一个边界的正则表达式
    public static final Pattern BOUNDARY_PATTERN = Pattern.compile("boundary=(.+)");

    // 定义一个表单项的正则表达式
    public static final Pattern FORM_ITEM_PATTERN = Pattern.compile("Content-Disposition: form-data; name=\"(.+)\"(; filename=\"(.+)\")?");

    public static void main(String[] args) throws IOException {
        // 创建一个服务器
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server started at port " + PORT);
        while (true) {
            // 接收一个客户端的请求
            Socket socket = server.accept();
            System.out.println("Accepted a connection from " + socket.getRemoteSocketAddress());
            // 创建一个线程来处理请求
            new Thread(new MultipartHandler(socket)).start();
        }
    }

    // 定义一个内部类来实现Runnable接口，用于处理multipart/form-data请求
    static class MultipartHandler implements Runnable {

        private Socket socket;

        public MultipartHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // 获取输入流和输出流
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                // 读取请求行和请求头
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = reader.readLine();
                System.out.println(line); // 打印请求行
                String boundary = null; // 定义一个边界变量
                while (!(line = reader.readLine()).isEmpty()) {
                    System.out.println(line); // 打印请求头
                    Matcher matcher = BOUNDARY_PATTERN.matcher(line); // 尝试匹配边界
                    if (matcher.find()) {
                        boundary = matcher.group(1); // 如果匹配成功，获取边界值
                    }
                }
                if (boundary == null) {
                    // 如果没有找到边界，说明不是multipart/form-data请求，返回400错误
                    out.write("HTTP/1.1 400 Bad Request\r\n".getBytes());
                    out.write("Content-Type: text/plain\r\n\r\n".getBytes());
                    out.write("The request is not multipart/form-data\r\n".getBytes());
                    out.flush();
                    return;
                }
                // 读取请求体的数据，按照边界进行分割
                StringBuilder requestBody = new StringBuilder(); // 定义一个字符串变量来存储请求体的数据
                while (!(line = reader.readLine()).startsWith(boundary)) {
                    requestBody.append(line).append("\r\n"); // 读取一行数据，并追加到字符串变量中，加上回车换行符
                }
                System.out.println(requestBody.toString()); // 打印请求体的数据

                // 返回200成功响应
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write("Content-Type: text/plain\r\n\r\n".getBytes());
                out.write("The request is handled successfully\r\n".getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close(); // 关闭连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
