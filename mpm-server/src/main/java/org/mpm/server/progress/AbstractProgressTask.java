package org.mpm.server.progress;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

public abstract class AbstractProgressTask implements ProgressInterface {

    private int total = -1;
    private int count = 0;

    @Override
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public int getCount() {
        return count;
    }

    public void countInc() {
        count++;
    }

    public boolean isFinished() {
        return calcProgress() >= 100;
    }

    @Override
    public NutMap getProgress() {
        return Lang.map("count", getCount()).setv("total", getTotal())
                .setv("progress", calcProgress());
    }

    @Override
    public NutMap getFinishedProgress() {
        return Lang.map("count", total).setv("total", total).setv("progress", 100);
    }

    int calcProgress() {
        if (getTotal() == -1) {
            return -1; // not start
        } else if (getTotal() == 0) {
            return 100;
        }
        return getCount() * 100 / getTotal();
    }
}
