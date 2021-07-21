package org.mpm.server.remote;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cosClient", url = "https://${cos.bucket}.cos.${cos.region}.myqcloud.com")
public interface CosRemoteService {

    @GetMapping("/{name}?imageInfo")
    Map getImageInfo(@PathVariable String name);

    @GetMapping("/{name}?exif")
    Map getExifInfo(@PathVariable String name);
}
