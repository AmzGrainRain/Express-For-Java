import Server.Server;

public class Main {
    public static void main(String[] args) {
        Server app = new Server(80);
        app.staticDir("D:\\Repo\\Express-For-Java\\static");

        app.get("/login", (req, res) -> {
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            res.send("<h1>您传入了" + req.body.get("asd") + "</h1>");
        });

        app.get("/register", (req, res) -> {
            res.setHeader("Content-Type", "text/html; charset=utf-8");
            res.send("<h1>注册页面</h1>");
        });

        app.listen();
    }
}
