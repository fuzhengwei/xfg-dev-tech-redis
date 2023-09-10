package cn.bugstack.xfg.dev.tech.domain.order.service;

import cn.bugstack.xfg.dev.tech.domain.order.model.aggregate.OrderAggregate;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.OrderEntity;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单服务接口
 * @create 2023-09-03 14:39
 */
public interface IOrderService {

    long initSkuCount(String sku, long count);

    String createOrderByNoLock(OrderAggregate orderAggregate);

    String createOrderByLock(OrderAggregate orderAggregate);

    String createOrder(OrderAggregate orderAggregate);

    OrderEntity queryOrder(String orderId);

    String payOrder(String orderId);

}
