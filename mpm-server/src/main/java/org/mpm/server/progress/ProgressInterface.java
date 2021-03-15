package org.mpm.server.progress;

import org.nutz.lang.util.NutMap;

public interface ProgressInterface {

    NutMap getProgress();

    boolean isFinished();

    NutMap getFinishedProgress();

    int getTotal();

    int getCount();
}
