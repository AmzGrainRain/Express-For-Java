import Server.Server;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // 实例化
        Server app = new Server(80);
        // 设置静态目录
        app.staticDir("D:\\Repo\\Express-For-Java\\static");

        // 设置一个 get 请求类型的路由
        app.get("/login", (req, res) -> {
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            // 发送文本消息
            res.send("<h1>您传入了" + req.params.get("asd") + "</h1>");
        });

        // 设置一个 post 请求类型的路由
        app.post("/register", (req, res) -> {
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            // 返回数据
            if (req.body.size() != 0) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> pair : req.body.entrySet()) {
                    sb.append(pair.getKey()).append(" -> ").append(pair.getValue());
                }
                res.send(sb.toString());
            } else res.send("没有传入参数");
        });

        app.listen();
    }
}
