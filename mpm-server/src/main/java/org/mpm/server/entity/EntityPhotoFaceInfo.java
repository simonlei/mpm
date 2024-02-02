package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("photo_face_info")
public class EntityPhotoFaceInfo {

    @Id
    Long id;
    Long photoId;
    Long faceId;
    Long x;
    Long y;
    Long width;
    Long height;
}
