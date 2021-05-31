package org.mpm.client;

public class Utils {

    public static int getDegree(int rotate) {
        if (rotate == 360) {
            return 0;
        }
        if (rotate > 360) {
            return rotate - 360;
        }
        if (rotate < -360) {
            return rotate + 360;
        }
        return rotate;
    }

    public static int getInt(Object obj, int defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt("" + obj);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
