package org.mpm.server.pics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagsController {

    @Autowired
    TagsService tagsService;

    @PostMapping("/api/getAllTags")
    public String getAllTags() {
        return tagsService.getAllTags();
    }
}
