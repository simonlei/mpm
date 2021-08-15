package org.mpm.server.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.nutz.mapl.Mapl;

public class MyUtils {

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
        } catch (Exception e) {
            return null;
        }
    }

    public static Long parseLong(String str, Long defaultValue) {
        try {
            return Long.parseLong("" + str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
