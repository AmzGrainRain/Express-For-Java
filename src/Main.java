import Server.Server;
import Server.Session;

public class Main {
    public static void main(String[] args) {
        Server app = new Server(80);

        // 设置静态目录
        app.staticDir("D:\\Repo\\Express-For-Java\\static");

        // 测试 post 请求
        app.post("/test-post", (req, res) -> {
            StringBuilder sb = new StringBuilder();
            if (req.params != null) {
                req.params.forEach((key, value) -> {
                    sb.append(key).append(" - ").append(value).append("\n");
                });
            }
            sb.append("=========\n");
            if (req.body != null) {
                req.body.forEach((key, value) -> {
                    sb.append(key).append(" - ").append(value).append("\n");
                });
            }
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            // 发送文本消息
            res.send(sb.toString());
        });

        // 测试 get 请求
        app.get("/test-get", (req, res) -> {
            StringBuilder sb = new StringBuilder();
            if (req.params != null) {
                req.params.forEach((key, value) -> {
                    sb.append(key).append(" - ").append(value).append("\n");
                });
            }
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            // 发送文本消息
            res.send(sb.toString());
        });

        // 设置一个 session
        app.post("/set-session", (req, res) -> {
            // Session
            Session session = new Session();
            res.setSession(session);
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            // 发送文本消息
            res.send("<h1>成功设置 session</h1>");
        });

        // 获取请求中携带的 session
        app.get("/get-session", (req, res) -> {
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");

            try {
                long sessionID = Long.parseLong(req.headersMap.get("Cookie"));
                if (!Session.sessionMap.containsKey(sessionID)) {
                    res.send("没有获取到 session");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                Session s = Session.sessionMap.get(sessionID);
                sb.append("Session ID: ").append(sessionID).append("\n");
                sb.append("Session Expire Time: ").append(s.expireTime).append("\n");
                sb.append("Session Attributes:\n");
                s.attribute.forEach((key, value) -> {
                    sb.append(key).append(": ").append(value).append("\n");
                });
                res.send(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
                res.send("获取 session 失败");
            }
        });

        app.listen();
    }
}
