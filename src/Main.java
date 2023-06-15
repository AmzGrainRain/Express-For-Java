import Server.Server;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Server app = new Server(80);
        app.staticDir("D:\\Repo\\Express-For-Java\\static");

        app.get("/login", (req, res) -> {
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            res.send("<h1>您传入了" + req.params.get("asd") + "</h1>");
        });

        app.post("/register", (req, res) -> {
            res.setHeader("Content-Type", "text/html; charset=utf-8");
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
