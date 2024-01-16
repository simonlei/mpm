package org.mpm.server.cron;

import java.io.IOException;
import org.mpm.server.entity.EntityPhoto;

public interface PhotoTask {

    void dealPhoto(EntityPhoto photo) throws IOException;
}
