package org.mpm.server.pics;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.filesystem.ProgressInterface;
import org.nutz.lang.util.NutMap;

@Slf4j
public class ProgressDataSource {

    private static Map<String, ProgressInterface> tasks = new ConcurrentHashMap<>();

    public static String addTask(ProgressInterface importer) {
        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, importer);
        return taskId;
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
