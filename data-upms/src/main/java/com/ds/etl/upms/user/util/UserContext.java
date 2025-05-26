package com.ds.etl.upms.user.util;

/**
 * @Author: yc
 * @CreateTime: 2025-05-23
 * @Description:
 * @Version: 1.0
 */
public class UserContext {
    private static final ThreadLocal<Integer> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentAccount = new ThreadLocal<>();

    public static void setCurrentUserId(Integer userId) {
        currentUserId.set(userId);
    }

    private static Integer getCurrentUserId() {
        return currentUserId.get();
    }

    public static void setCurrentAccount(String account) {
        currentAccount.set(account);
    }

    private static String getCurrentAccount() {
        return currentAccount.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentAccount.remove();
    }
}