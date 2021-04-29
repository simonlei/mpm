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
}
