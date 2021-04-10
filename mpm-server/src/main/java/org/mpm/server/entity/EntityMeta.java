package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

@Table("t_metas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityMeta {

    @Id
    private Long id;
    @Name
    @Column("c_key")
    private String key;
    @Column("c_value")
    private String value;
}
