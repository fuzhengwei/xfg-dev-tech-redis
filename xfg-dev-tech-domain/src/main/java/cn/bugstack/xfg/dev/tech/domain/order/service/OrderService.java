package cn.bugstack.xfg.dev.tech.domain.order.service;

import cn.bugstack.xfg.dev.tech.domain.order.model.aggregate.OrderAggregate;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.OrderEntity;
import cn.bugstack.xfg.dev.tech.domain.order.repository.IOrderRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单服务
 * @create 2023-09-03 15:01
 */
@Service
public class OrderService implements IOrderService{

    @Resource
    private IOrderRepository orderRepository;

    @Override
    public long initSkuCount(String sku, long count) {
        return orderRepository.initSkuCount(sku, count);
    }

    @Override
    public String createOrderByNoLock(OrderAggregate orderAggregate) {
        return orderRepository.createOrderByNoLock(orderAggregate);
    }

    @Override
    public String createOrderByLock(OrderAggregate orderAggregate) {
        return orderRepository.createOrderByLock(orderAggregate);
    }

    @Override
    public String createOrder(OrderAggregate orderAggregate) {
        return orderRepository.createOrder(orderAggregate);
    }

    @Override
    public OrderEntity queryOrder(String orderId) {
        return orderRepository.queryOrder(orderId);
    }

    @Override
    public String payOrder(String orderId) {
        return orderRepository.payOrder(orderId);
    }

}
