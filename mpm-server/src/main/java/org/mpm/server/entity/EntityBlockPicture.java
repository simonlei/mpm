package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("t_block_photos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityBlockPicture {

    @Id
    private Long id;
    private Long size;
    private String md5;
    private String sha1;

}
