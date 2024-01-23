package org.mpm.server.pics;

import java.util.List;
import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Service
@RestController
public class FaceController {

    @Autowired
    FaceService faceService;

    @PostMapping("/api/getFaces")
    public List<NutMap> getFaces() {
        return faceService.getFaces();
    }

    @GetMapping(value = "/get_face_img/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Object getFaceImg(@PathVariable("id") int id) {
        return faceService.getFaceImg(id);
    }
}
