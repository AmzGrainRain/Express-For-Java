package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class Server {
    private final int port;
    private final Map<String, CallBack> GET;
    private final Map<String, CallBack> POST;
    private final Set<CallBack> PRE;
    private String staticPath;

    public Server(int port) {
        this.port = port;
        staticPath = "./static";
        GET = new HashMap<>();
        POST = new HashMap<>();
        PRE = new HashSet<>();
    }

    public void staticDir(String path) {
        this.staticPath = path;
    }

    public void use(CallBack cb) {
        PRE.add(cb);
    }

    public void get(String route, CallBack cb) {
        GET.put(route, cb);
    }

    public void post(String route, CallBack cb) {
        POST.put(route, cb);
    }

    private boolean matchStaticFile(String path, Response res) {
        File f = new File(staticPath + path);

        // 匹配目录
        if (f.isDirectory()) {
            File nf = new File(staticPath + path + "index.html");
            if (nf.isFile()) {
                res.sendFile(nf);
                return true;
            }
        }
        System.gc();

        // 匹配文件
        if (f.isFile()) {
            res.sendFile(f);
            return true;
        }

        return false;
    }

    private boolean matchGET(Request req, Response res) {
        if (!req.method.equalsIgnoreCase("GET")) return false;
        if (!GET.containsKey(req.path)) return false;

        GET.get(req.path).call(req, res);
        return true;
    }

    private boolean matchPOST(Request req, Response res) {
        if (!req.method.equalsIgnoreCase("POST")) return false;
        if (!POST.containsKey(req.path)) return false;

        POST.get(req.path).call(req, res);
        return true;
    }

    private void process(Request req, Response res) {
        // 多线程
        new Thread(() -> {
            // 中间件
            for (CallBack cb : PRE) {
                //if (!cb.call(req, res)) return;
            }
            // 匹配到静态文件, 终止路由
            if (matchStaticFile(req.path, res)) return;
            // 没有匹配到路由
            if (!matchGET(req, res) && !matchPOST(req, res)) {
                // 返回404
                res.setStatus(404).end();
            }
        }).start();
    }

    public void listen() {
        try (ServerSocket ss = new ServerSocket(port)) {
            while (true) {
                Socket client = ss.accept();
                Request req = new Request(client.getInputStream());
                Response res = new Response(client.getOutputStream());
                process(req, res);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
