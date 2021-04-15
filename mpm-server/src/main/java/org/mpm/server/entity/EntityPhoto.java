package org.mpm.server.entity;


import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.EL;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Prev;
import org.nutz.dao.entity.annotation.Table;

@Table("t_photos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityPhoto {

    @Id
    private Long id;
    @Name
    @Prev(els = @EL("uuid(32)"))
    private String name; // 只是COS上的文件名
    private Long size = 0l;
    private Integer width = 0;
    private Integer height = 0;
    private String md5;
    private String sha1;
    private Boolean trashed = false;
    private Boolean star = false;
    private String description = "";
    private Double latitude; // 纬度
    private Double longitude; // 经度
    private String address; // 定位到的地址
    private Date takenDate; // 拍照时间
    private Integer mediaType;
    private Double duration; // 视频长度
}
