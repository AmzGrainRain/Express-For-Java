import Server.Server;

public class Main {
    public static void main(String[] args) {
        Server app = new Server(80);
        app.listen();
    }
}
