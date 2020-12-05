package org.mpm.server.util;

import org.nutz.dao.pager.Pager;

public class ExplicitPager extends Pager {

    int offset;
    int count;

    public ExplicitPager(int offset, int count) {
        super();
        this.offset = offset;
        this.count = count;
    }

    public void setSelectCount(int count) {
        this.count = count;
    }

    @Override
    public int getPageSize() {
        return count;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}