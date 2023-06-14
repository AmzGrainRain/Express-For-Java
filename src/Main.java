import Server.Server;

public class Main {
    public static void main(String[] args) {
        Server app = new Server(80);
        app.staticDir("D:\\Repo\\Express-For-Java\\static");
        app.use((req, res) -> {
            res.setStatus(404).end();
        });

        app.listen();
    }
}
