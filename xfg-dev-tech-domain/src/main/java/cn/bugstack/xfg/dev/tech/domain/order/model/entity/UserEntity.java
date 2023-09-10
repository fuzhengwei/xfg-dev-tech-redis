package cn.bugstack.xfg.dev.tech.domain.order.model.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户实体
 * @create 2023-09-03 14:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    /** 用户编号 */
    private String userId;
    /** 用户姓名 */
    private String userName;
    /** 用户电话 */
    private String userMobile;

}
