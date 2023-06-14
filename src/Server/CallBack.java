package Server;

@FunctionalInterface
public interface CallBack {
    void callback(Request req, Response res);
}
