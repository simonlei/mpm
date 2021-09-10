package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Table;

@Table("photo_tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityPhotoTags {

    private Long id;
    private Long photoId;
    private String name;
}
