package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("t_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityFile {

    @Id
    private Long id;
    @Column
    private String name;
    @Column
    private String description;
    @Column
    private Boolean isFolder;
    @Column
    private Long rootId;
    @Column
    private Long parentId;
    @Column
    private Long photoId;
    @Column
    private String photoName;

}
