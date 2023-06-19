package com.amzlab;

import java.util.HashMap;
import java.util.Map;

public class Session {
    public final String uid;
    public final long activeTime;
    private final long expireTime;
    public final Map<String, String> attributeMap;

    public Session(String uid) {
        this.uid = uid;
        // session 携带的属性
        attributeMap = new HashMap<>();
        // 以当前的时间戳作为激活时间
        activeTime = System.currentTimeMillis();
        // 过期时间为一小时后
        expireTime = activeTime + 3600000;
    }

    /**
     * 设置属性
     *
     * @param k 键
     * @param v 值
     */
    public void setAttribute(String k, String v) {
        attributeMap.put(k, v);
    }

    /**
     * 删除属性
     * @param k 键
     */
    public void delAttribude(String k) {
        attributeMap.remove(k);
    }

    /**
     * session 是否过期
     *
     * @return 布尔
     */
    public boolean isValid() {
        return System.currentTimeMillis() < expireTime;
    }

    public static Map<String, Session> sessionMap = new HashMap<>();
}
