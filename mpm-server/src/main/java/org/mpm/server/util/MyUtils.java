package org.mpm.server.util;

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
}
