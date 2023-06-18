package com.amzlab.Interface;

import com.amzlab.Request;
import com.amzlab.Response;

@FunctionalInterface
public interface CallBack {
    void call(Request req, Response res);
}
