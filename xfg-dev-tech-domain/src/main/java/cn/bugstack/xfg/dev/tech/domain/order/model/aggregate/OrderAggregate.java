package cn.bugstack.xfg.dev.tech.domain.order.model.aggregate;

import cn.bugstack.xfg.dev.tech.domain.order.model.entity.SKUEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.UserEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.valobj.DeviceVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单聚合对象
 * @create 2023-09-03 14:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAggregate {

    /** 用户实体  */
    private UserEntity userEntity;
    /** 商品实体  */
    private SKUEntity skuEntity;
    /** 设备实体  */
    private DeviceVO deviceVO;

}
