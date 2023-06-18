package com.amzlab.Interface;

import com.amzlab.Request;
import com.amzlab.Response;

@FunctionalInterface
public interface Middleware {
    /**
     * 中间件函数
     * @param req 请求体
     * @param res 响应体
     * @return 返回值决定路由是否继续匹配
     */
    boolean call (Request req, Response res);
}
