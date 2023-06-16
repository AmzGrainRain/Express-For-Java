package com.amzlab;

@FunctionalInterface
public interface CallBack {
    void call(Request req, Response res);
}
