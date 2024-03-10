package org.mpm.server.entity;

import java.time.LocalDate;
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
@Table("t_activity")
public class EntityActivity {

    @Id
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double latitude; // 纬度
    private Double longitude; // 经度
}
