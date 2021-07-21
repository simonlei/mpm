package org.mpm.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.nutz.mapl.Mapl;

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

    public static Integer parseInt(Object str, Integer defaultValue) {
        try {
            return Integer.parseInt("" + str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String cell(Object obj, String path) {
        try {
            Object cell = Mapl.cell(obj, path);
            return cell == null ? "" : "" + cell;
        } catch (Exception e) {
            return "";
        }
    }

    public static Date parseDate(String str, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }
}
