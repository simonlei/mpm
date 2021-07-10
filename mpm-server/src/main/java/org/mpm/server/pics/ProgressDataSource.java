package org.mpm.server.pics;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.mpm.server.progress.ProgressInterface;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ProgressDataSource {

    private static Map<String, ProgressInterface> tasks = new ConcurrentHashMap<>();

    public static String addTask(ProgressInterface importer) {
        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, importer);
        return taskId;
    }

    @GetMapping("/api/getProgress/{taskId}")
    public NutMap getProgress(@PathVariable("taskId") String taskId) {
        log.info("taskId " + taskId);
        ProgressInterface importer = tasks.get(taskId);
        if (importer != null) {
            if (importer.isFinished()) {
                tasks.remove(taskId);
                return importer.getFinishedProgress();
            }
            return importer.getProgress();
        } else {
            return Lang.map("progress", 100);
        }
    }
}
