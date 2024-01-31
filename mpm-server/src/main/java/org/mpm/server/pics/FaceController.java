package org.mpm.server.pics;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import org.nutz.lang.util.NutMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Service
@RestController
public class FaceController {

    @Autowired
    FaceService faceService;
    @Autowired
    HttpServletResponse response;

    @PostMapping("/api/getFaces")
    public List<NutMap> getFaces() {
        return faceService.getFaces();
    }

    @PostMapping("/api/getFacesWithName")
    public List<NutMap> getFacesWithName() {
        return faceService.getFacesWithName();
    }

    @PostMapping("/api/mergeFace")
    public Boolean mergeFace(@RequestBody MergeFaceParam param) throws TencentCloudSDKException {
        return faceService.mergeFace(param.from, param.to);
    }

    @PostMapping("/api/updateFace")
    public boolean updateFace(@RequestBody FaceUpdateParam face) {
        return faceService.updateFace(face);
    }

    @GetMapping(value = "/get_face_img/{id}/{photoId}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Object getFaceImg(@PathVariable("id") int id, @PathVariable("photoId") Long photoId) {
        response.setHeader("Cache-Control", "max-age=31536000");
        byte[] faceImg = faceService.getFaceImg(id, photoId);
        response.setHeader("content-length", "" + faceImg.length);
        return faceImg;
    }

    @Data
    public static class FaceUpdateParam {

        int faceId;
        String name;
        Long selectedFace;
        Boolean hidden;
    }

    @Data
    public static class MergeFaceParam {

        Long from;
        Long to;
    }
}
