package org.mpm.server.util;

import org.nutz.boot.AppContext;
import org.nutz.ioc.Ioc;

public class MyUtils {

    public static Long getPrefixLong(String s) {
        String prefix = "";
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '9' && prefix.length() <= 10) {
                prefix += c;
            } else {
                break;
            }
        }
        if (prefix.length() == 0) {
            return -1l;
        }
        return Long.parseLong(prefix);
    }

    public static Ioc getIoc() {
        return AppContext.getDefault().getIoc();
    }

    public static <T> T getByType(Class<T> clz) {
        return AppContext.getDefault().getIoc().getByType(clz);
    }
}
