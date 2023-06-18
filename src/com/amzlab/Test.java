package com.amzlab;

// 导入阿里巴巴的 fastjson2 用于解析 josn 字符串
import com.alibaba.fastjson2.JSON;

// 可能传入的 json 格式
class User {
    public String name;
    public String age;
    public String address;
}

public class Test {
    private static void start(int threadNumber, String staticDir) {
        Server app = new Server(threadNumber);

        // 设置静态目录
        app.staticDirectory(staticDir);

        // 中间件测试（匹配所有以 /view/ 开头的地址）
        System.out.println("中间件测试（匹配所有以 /view/ 开头的地址）");
        app.use((req, res) -> {
            // 返回 true 代表继续匹配路由
            if (!req.path.matches("^/view/.*?")) return true;
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            // 发送文本消息
            res.send("被中间件拦截的请求");
            // 返回 false 代表停止路由匹配
            return false;
        });

        // 测试 post 请求
        System.out.println("post 接口：/test-post");
        app.post("/test-post", (req, res) -> {
            StringBuilder sb = new StringBuilder();
            // 获取明文参数
            if (req.params != null) {
                req.params.forEach((key, value) -> {
                    sb.append(key).append(" - ").append(value).append("\n");
                });
            }
            sb.append("=========\n");
            // 获取 multpart/form-data 表单数据
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
        System.out.println("get 接口：/test-get");
        app.get("/test-get", (req, res) -> {
            // 获取明文参数
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
        System.out.println("下发 session：/set-session");
        app.post("/set-session", (req, res) -> {
            // 创建一个 Session
            Session session = new Session();
            // 为 session 添加一个属性
            session.setAttribute("userName", "test");
            // 设置 session
            res.setSession(session);
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            // 发送文本消息
            res.send("成功设置 session");
        });

        // 获取请求中携带的 session
        System.out.println("解析 session：/get-session");
        app.get("/get-session", (req, res) -> {
            // 设置响应头
            res.setHeader("Content-Type", "text/html; charset=utf-8");

            try {
                // 检查请求头中是否携带 Cookie
                if (!req.headersMap.containsKey("Cookie")) {
                    res.send("请求头中没有 Cookie ");
                    return;
                }

                // 从请求头中获取 session id, session 是一个时间戳
                long sessionID = Long.parseLong(req.headersMap.get("Cookie"));

                // 从 session map 里查询是否存在这个 session
                if (!Session.sessionMap.containsKey(sessionID)) {
                    res.send("没有获取到 session");
                    return;
                }

                // 获取 session 内部存储的信息
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

        // 测试 json 数据解析
        System.out.println("接收 json 数据：/test-json");
        app.post("/test-json", (req, res) -> {
            // json 字符串转 User 对象
            User u = JSON.parseObject(req.jsonString, User.class);

            String sb = "name: " + u.name + "\n" +
                    "age: " + u.age + "\n" +
                    "address: " + u.address + "\n";
            res.send(sb);
        });

        // 监听端口
        app.listen(80);
    }

    public static void main(String[] args) {
        start(6, "D:\\Repo\\Express-For-Java\\static");
    }
}
