package de.hska.lkit.demo.redis.model;

import org.springframework.core.NamedThreadLocal;

public abstract class SimpleSecurity {

    private static final ThreadLocal<UserInfo> user = new NamedThreadLocal<>("microblog-id");
    private static class UserInfo {
        String name;
        String uid;
    }
    public static void setUser(String name, String uid) {
        UserInfo userInfo = new UserInfo();
        userInfo.name = name;
        userInfo.uid = uid;
        user.set(userInfo);
    }
    public static boolean isUserSignedIn(String name) {
        UserInfo userInfo = user.get();
        System.out.println(userInfo.name);
        return userInfo != null && userInfo.name.equals(name);
    }
    public static boolean isSignedIn() {
        UserInfo userInfo = user.get();
        return userInfo != null;
    }
    public static String getName() {
        UserInfo userInfo = user.get();
        return userInfo.name;
    }
    public static String getUid() {
        UserInfo userInfo = user.get();
        return userInfo.uid;
    }
}
