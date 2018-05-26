package de.hska.lkit.demo.redis.model;

import org.springframework.core.NamedThreadLocal;

public abstract class SimpleSecurity {

    private static final ThreadLocal<UserInfo> user = new NamedThreadLocal<UserInfo>("microblog-id");
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
        return userInfo != null && userInfo.name.equals(name);
    }
    public static boolean isSignedIn() {
        //to do
        return false;
    }
    public static String getName() {
        //to do
        return null;
    }
    public static String getUid() {
        //to do
        return null;
    }
}
