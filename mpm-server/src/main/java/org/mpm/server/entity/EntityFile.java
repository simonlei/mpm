package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("t_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityFile {

    @Id
    private Long id;
    @Column
    private String name;
    /**
     * 导入时的path，可以用来查找目录下的所有文件
     */
    @Column
    private String path;
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
