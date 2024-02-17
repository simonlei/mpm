package org.mpm.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String account;
    private String name;
    private String salt;
    private String passwd;
    private Long faceId;
    private Boolean isAdmin;
}
