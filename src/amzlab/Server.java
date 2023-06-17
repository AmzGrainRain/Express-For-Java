package amzlab;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private final ThreadPoolExecutor threadPool;
    private final int port;
    private final Map<String, CallBack> GET;
    private final Map<String, CallBack> POST;
    private final Set<Middleware> PRE;
    private String staticPath;

    /**
     * 构造器
     *
     * @param port 端口
     */
    public Server(int port) {
        threadPool = new ThreadPoolExecutor(2, 4, 30, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        this.port = port;
        staticPath = null;
        GET = new HashMap<>();
        POST = new HashMap<>();
        PRE = new HashSet<>();
    }

    /**
     * 构造器
     *
     * @param port      监听端口
     * @param threadNum 最大线程数
     */
    public Server(int port, int threadNum) {
        threadPool = new ThreadPoolExecutor(threadNum / 2, threadNum, 60, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        this.port = port;
        staticPath = null;
        GET = new HashMap<>();
        POST = new HashMap<>();
        PRE = new HashSet<>();
    }

    /**
     * 设置静态目录
     *
     * @param path 绝对路径
     */
    public void staticDir(String path) {
        this.staticPath = path;
    }

    /**
     * 简单的路由拦截
     *
     * @param cb 回调函数
     */
    public void use(Middleware cb) {
        PRE.add(cb);
    }

    /**
     * 设置 GET 路由
     *
     * @param route 路由
     * @param cb    回调函数
     */
    public void get(String route, CallBack cb) {
        GET.put(route, cb);
    }

    /**
     * 设置 POST 路由
     *
     * @param route 路由
     * @param cb    回调函数
     */
    public void post(String route, CallBack cb) {
        POST.put(route, cb);
    }

    /**
     * 静态文件匹配
     *
     * @param path 文件路径
     * @param res  响应实例
     * @return 是否匹配成功
     */
    private boolean matchStaticFile(String path, Response res) {
        if (staticPath == null) return false;

        path = staticPath + path;
        File f = new File(path);

        // 匹配目录
        if (f.isDirectory()) {
            if (path.charAt(path.length() - 1) != '/') path += '/';
            File nf = new File(path + "index.html");
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

    /**
     * GET 路由匹配
     *
     * @param req 请求实例
     * @param res 响应实例
     * @return 是否匹配成功
     */
    private boolean matchGET(Request req, Response res) {
        if (!req.method.equalsIgnoreCase("GET")) return false;
        if (!GET.containsKey(req.path)) return false;

        GET.get(req.path).call(req, res);
        return true;
    }

    /**
     * POST 路由匹配
     *
     * @param req 请求实例
     * @param res 响应实例
     * @return 是否匹配成功
     */
    private boolean matchPOST(Request req, Response res) {
        if (!req.method.equalsIgnoreCase("POST")) return false;
        if (!POST.containsKey(req.path)) return false;

        POST.get(req.path).call(req, res);
        return true;
    }

    /**
     * 处理路由匹配 (多线程)
     *
     * @param req 请求实例
     * @param res 响应实例
     */
    private void process(Request req, Response res) {
        // 向线程池提交 Runnable 任务
        threadPool.execute(() -> {
            // 中间件
            for (Middleware middleware : PRE) {
                if (!middleware.call(req, res)) return;
            }
            // 匹配到静态文件, 终止路由
            if (matchStaticFile(req.path, res)) return;
            // 没有匹配到路由
            if (!matchGET(req, res) && !matchPOST(req, res)) {
                // 返回404
                res.setStatus(404).end();
            }
        });
    }

    public void listen() {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Server running at http://127.0.0.1:" + port + ".");
            while (true) {
                Socket client = ss.accept();
                Request req = new Request(client.getInputStream());
                Response res = new Response(client.getOutputStream());

                // 请求头解析失败
                if (!req.ok) {
                    res.setStatus(400).end();
                    continue;
                }

                // 匹配路由
                process(req, res);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
