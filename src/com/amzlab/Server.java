package com.amzlab;

import com.amzlab.Interface.CallBack;
import com.amzlab.Interface.Middleware;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    private Integer httpPort;
    private Integer httpsPort;
    private boolean sslKey;
    private boolean sslCert;
    private boolean onlyHttps;
    private final ThreadPoolExecutor threadPool;
    private final Map<String, CallBack> GET;
    private final Map<String, CallBack> POST;
    private final Set<Middleware> PRE;
    private String staticPath;

    /**
     * 构造器
     *
     * @param threadNum 最大线程数
     */
    public Server(int threadNum) {
        // http/https 端口
        this.httpPort = null;
        this.httpsPort = null;
        // ssl 私钥是否已经设置
        sslKey = false;
        // ssl 公钥是否已经设置
        sslCert = false;
        // 是否将所有 http 请求重定向到 https
        onlyHttps = false;
        // 创建线程池
        // 最小线程数量为传入的线程数量的一半
        // 最大线程数量为传入的线程数量
        // 空闲线程将在 10 分钟后关闭
        threadPool = new ThreadPoolExecutor(threadNum / 2, threadNum, 10, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        // GET 请求路由
        GET = new HashMap<>();
        // POST 请求路由
        POST = new HashMap<>();
        // 中间件
        PRE = new HashSet<>();
        // 静态 WEB 服务器根目录
        staticPath = null;
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

//    /**
//     * 设置服务器 ssl
//     *
//     * @param key  私钥
//     * @param cert 公钥
//     */
//    public void setSSL(File key, File cert) {
//        try {
//            if (key.isFile() && key.canRead() && cert.isFile() && cert.canRead()) {
//                System.setProperty("javax.net.ssl.keyStore", key.getAbsolutePath());
//                System.setProperty("javax.net.ssl.trustStore", cert.getAbsolutePath());
//                sslKey = true;
//                sslCert = true;
//            } else throw new Exception("私钥或证书不存在/无法读取, 已禁用 Only HTTPS 重定向功能.");
//        } catch (Exception e) {
//            e.printStackTrace();
//            sslKey = false;
//            sslCert = false;
//            onlyHttps = false;
//        }
//    }

    /**
     * 设置静态目录
     *
     * @param path 绝对路径
     */
    public void staticDirectory(String path) {
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

    public void listen(int httpPort) {
        this.httpPort = httpPort;
        this.httpsPort = null;
        this.onlyHttps = false;

        try (ServerSocket ss = new ServerSocket(httpPort)) {
            System.out.println("Server running at http://127.0.0.1:" + httpPort + ".");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * 启动服务器
//     *
//     * @param httpPort  http 服务器端口
//     * @param httpsPort https 服务器端口 [null 代表不启用]
//     * @param onlyHttps 是否重定向所有 HTTP 请求到 HTTPS 服务器
//     */
//    public void listen(int httpPort, Integer httpsPort, boolean onlyHttps) {
//        this.httpPort = httpPort;
//        this.httpsPort = httpsPort;
//        this.onlyHttps = onlyHttps;
//
//        // 启用了 https 服务器
//        if (httpsPort != null) new Thread(() -> {
//            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
//            try (SSLServerSocket ss = (SSLServerSocket) factory.createServerSocket(httpsPort)) {
//                System.out.println("Server running at https://127.0.0.1:" + httpsPort + ".");
//                while (true) {
//                    Socket client = ss.accept();
//                    Request req = new Request(client.getInputStream());
//                    Response res = new Response(client.getOutputStream());
//                    // 请求头解析失败
//                    if (!req.ok) {
//                        res.setStatus(400).end();
//                        continue;
//                    }
//                    // 匹配路由
//                    process(req, res);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
//
//        if (httpsPort != null && onlyHttps) { // 启用了 https 服务器并且启用了 only https
//            new Thread(() -> {
//                try (ServerSocket ss = new ServerSocket(httpsPort)) {
//                    System.out.println("Redirecting all HTTP requests to HTTPS...");
//                    while (true) {
//                        Socket client = ss.accept();
//                        Request req = new Request(client.getInputStream());
//                        OutputStream os = client.getOutputStream();
//                        String sb = "HTTP/1.1 301 Moved Permanently\nLocation: " + req.fullPath;
//
//                        os.write(sb.getBytes());
//                        os.flush();
//                        os.close();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        } else if (httpsPort != null) { // 启用了 https 服务器并且禁用了 only https
//            new Thread(() -> {
//                try (ServerSocket ss = new ServerSocket(httpPort)) {
//                    System.out.println("Server running at http://127.0.0.1:" + httpPort + ".");
//                    while (true) {
//                        Socket client = ss.accept();
//                        Request req = new Request(client.getInputStream());
//                        Response res = new Response(client.getOutputStream());
//                        // 请求头解析失败
//                        if (!req.ok) {
//                            res.setStatus(400).end();
//                            continue;
//                        }
//                        // 匹配路由
//                        process(req, res);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
//    }
}
