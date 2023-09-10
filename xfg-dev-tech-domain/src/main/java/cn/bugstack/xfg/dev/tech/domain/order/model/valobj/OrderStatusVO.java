package cn.bugstack.xfg.dev.tech.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单状态；0 创建、1完成、2掉单、3关单
 * @create 2023-08-12 08:50
 */
@Getter
@AllArgsConstructor
public enum OrderStatusVO {

    CREATE(0, "创建"),
    COMPLETE(1,"完成"),
    DISPATCH(2,"调单"),
    CLOSE(3,"关单"),
    ;

    private final Integer code;
    private final String desc;

}
