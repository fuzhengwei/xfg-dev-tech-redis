package cn.bugstack.xfg.dev.tech.domain.order.repository;

import cn.bugstack.xfg.dev.tech.domain.order.model.aggregate.OrderAggregate;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.OrderEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单仓储接口
 * @create 2023-09-03 14:41
 */
public interface IOrderRepository {

    long initSkuCount(String sku, long count);

    String createOrderByNoLock(OrderAggregate orderAggregate);

    String createOrderByLock(OrderAggregate orderAggregate);

    String createOrder(OrderAggregate orderAggregate);

    OrderEntity queryOrder(String orderId);

    String payOrder(String orderId);

}
