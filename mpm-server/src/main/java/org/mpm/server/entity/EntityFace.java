package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("t_face")
public class EntityFace {

    @Id
    Long id;
    /**
     * 后面提供修改人名的能力
     */
    String name;
    /**
     * 腾讯云返回的 face id
     */
    String faceId;
    /**
     * personId = photo.id + face.id
     */
    String personId;
    Long selectedFace;
}
