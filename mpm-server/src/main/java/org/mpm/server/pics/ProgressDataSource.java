package org.mpm.server.pics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.filesystem.ProgressInterface;
import org.nutz.lang.util.NutMap;

@Slf4j
public class ProgressDataSource {

    private static Map<String, ProgressInterface> tasks = new ConcurrentHashMap<>();

    public static void addTask(String taskId, ProgressInterface importer) {
        tasks.put(taskId, importer);
    }

    public NutMap getProgress(String taskId) {
        log.info("taskId " + taskId);
        ProgressInterface importer = tasks.get(taskId);
        if (importer != null) {
            if (importer.isFinished()) {
                tasks.remove(taskId);
            }
            return importer.getProgress();
        }
        return importer.getFinishedProgress();
    }


}
