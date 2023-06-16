package Server;

import java.util.HashMap;
import java.util.Map;

public class Session {
    public final long id;
    public final long expireTime;
    public final Map<String, String> attribute;

    public Session() {
        // session 携带的属性
        attribute = new HashMap<>();

        // 以当前的时间戳作为 session id
        id = System.currentTimeMillis();

        // 过期时间为一小时后
        expireTime = id + 3600000;
    }

    /**
     * 设置属性
     *
     * @param k 键
     * @param v 值
     */
    public void setAttribute(String k, String v) {
        attribute.put(k, v);
    }

    /**
     * session 是否过期
     *
     * @return 布尔
     */
    public boolean isValid() {
        return System.currentTimeMillis() < expireTime;
    }

    public static Map<Long, Session> sessionMap = new HashMap<>();
}
