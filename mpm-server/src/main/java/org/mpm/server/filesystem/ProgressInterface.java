package org.mpm.server.filesystem;

import org.nutz.lang.util.NutMap;

public interface ProgressInterface {

    NutMap getProgress();

    boolean isFinished();

    NutMap getFinishedProgress();
}
