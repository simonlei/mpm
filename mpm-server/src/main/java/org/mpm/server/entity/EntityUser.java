package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("t_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityUser {

    @Id
    private Long id;
    @Column
    private String account;
    @Column
    private String name;
    @Column
    private String salt;
    @Column
    private String passwd;
    @Column
    private Long faceId;
    @Column
    private Boolean isAdmin;
    
    private String signature;
}
