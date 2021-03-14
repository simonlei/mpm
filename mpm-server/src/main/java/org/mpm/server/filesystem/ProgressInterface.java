package org.mpm.server.filesystem;

import org.nutz.lang.util.NutMap;

public interface ProgressInterface {

    NutMap getProgress();

    default boolean isFinished() {
        return calcProgress() >= 100;
    }

    NutMap getFinishedProgress();

    int getTotal();

    int getCount();

    default int calcProgress() {
        if (getTotal() == -1) {
            return -1; // not start
        } else if (getTotal() == 0) {
            return 100;
        }
        return getCount() * 100 / getTotal();
    }
}
