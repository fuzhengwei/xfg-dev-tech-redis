package cn.bugstack.xfg.dev.tech.infrastructure.persistent.repository;

import cn.bugstack.xfg.dev.tech.domain.order.model.aggregate.OrderAggregate;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.OrderEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.SKUEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.UserEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.valobj.DeviceVO;
import cn.bugstack.xfg.dev.tech.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.xfg.dev.tech.domain.order.repository.IOrderRepository;
import cn.bugstack.xfg.dev.tech.infrastructure.persistent.dao.IUserOrderDao;
import cn.bugstack.xfg.dev.tech.infrastructure.persistent.po.UserOrderPO;
import cn.bugstack.xfg.dev.tech.infrastructure.redis.IRedisService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单仓储实现
 * @create 2023-09-03 14:42
 */
@Slf4j
@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IRedisService redissonService;
    @Resource
    private IUserOrderDao userOrderDao;
    @Resource
    private RTopic testRedisTopic;

    @Resource(name = "testRedisTopic02")
    private RTopic testRedisTopic02;

    @Resource(name = "testRedisTopic03")
    private RTopic testRedisTopic03;

    @Override
    public long initSkuCount(String sku, long count) {
        return redissonService.incrBy(sku, count);
    }

    @Override
    public String createOrderByNoLock(OrderAggregate orderAggregate) {
        UserEntity userEntity = orderAggregate.getUserEntity();
        SKUEntity skuEntity = orderAggregate.getSkuEntity();

        // 模拟锁商品库存
        long decrCount = redissonService.decr(skuEntity.getSku());
        if (decrCount < 0) return "已无库存[初始化的库存和使用库存，保持一致。orderService.initSkuCount(\"13811216\", 10000);]";
        String lockKey = userEntity.getUserId().concat("_").concat(String.valueOf(decrCount));

        RLock lock = redissonService.getLock(lockKey);

        try {
            lock.lock();
            return createOrder(orderAggregate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String createOrderByLock(OrderAggregate orderAggregate) {
        RLock lock = redissonService.getLock("create_order_lock_".concat(orderAggregate.getSkuEntity().getSku()));
        try {
            lock.lock();
            long decrCount = redissonService.decr(orderAggregate.getSkuEntity().getSku());
            if (decrCount < 0) return "已无库存[初始化的库存和使用库存，保持一致。orderService.initSkuCount(\"13811216\", 10000);]";
            return createOrder(orderAggregate);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String createOrder(OrderAggregate orderAggregate) {
        try {
            // 模拟数据库基础耗时在10毫秒以上
            Thread.sleep(120);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        UserEntity userEntity = orderAggregate.getUserEntity();
        SKUEntity skuEntity = orderAggregate.getSkuEntity();
        DeviceVO deviceVO = orderAggregate.getDeviceVO();
        String orderId = RandomStringUtils.randomNumeric(11);

        UserOrderPO userOrderPO = UserOrderPO.builder()
                .userName(userEntity.getUserName())
                .userId(userEntity.getUserId())
                .userMobile(userEntity.getUserMobile())
                .sku(skuEntity.getSku())
                .skuName(skuEntity.getSkuName())
                .orderId(orderId)
                .quantity(skuEntity.getQuantity())
                .unitPrice(skuEntity.getUnitPrice())
                .discountAmount(skuEntity.getDiscountAmount())
                .tax(skuEntity.getTax())
                .totalAmount(skuEntity.getTotalAmount())
                .orderDate(new Date())
                .orderStatus(OrderStatusVO.CREATE.getCode())
                .isDelete(0)
                .uuid(UUID.randomUUID().toString().replace("-", ""))
                .ipv4(deviceVO.getIpv4())
                .ipv6(deviceVO.getIpv6())
                .extData(JSON.toJSONString(deviceVO))
                .build();

        // 插入数据库
        userOrderDao.insert(userOrderPO);

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserName(userOrderPO.getUserName());
        orderEntity.setUserId(userOrderPO.getUserId());
        orderEntity.setUserMobile(userOrderPO.getUserMobile());
        orderEntity.setSku(userOrderPO.getSku());
        orderEntity.setSkuName(userOrderPO.getSkuName());
        orderEntity.setOrderId(userOrderPO.getOrderId());
        orderEntity.setQuantity(userOrderPO.getQuantity());
        orderEntity.setUnitPrice(userOrderPO.getUnitPrice());
        orderEntity.setDiscountAmount(userOrderPO.getDiscountAmount());
        orderEntity.setTax(userOrderPO.getTax());
        orderEntity.setTotalAmount(userOrderPO.getTotalAmount());
        orderEntity.setOrderDate(userOrderPO.getOrderDate());
        orderEntity.setOrderStatus(userOrderPO.getOrderStatus());
        orderEntity.setUuid(userOrderPO.getUuid());
        orderEntity.setDeviceVO(JSON.parseObject(userOrderPO.getExtData(), DeviceVO.class));

        // 设置到缓存
        redissonService.setValue(orderId, orderEntity);

        testRedisTopic.publish(JSON.toJSONString(orderEntity));

        testRedisTopic02.publish(JSON.toJSONString(orderEntity));
        testRedisTopic03.publish(JSON.toJSONString(orderEntity));

        return orderId;
    }

    @Override
    public OrderEntity queryOrder(String orderId) {
        OrderEntity orderEntity = redissonService.getValue(orderId);
        if (null == orderEntity) {
            UserOrderPO userOrderPO = userOrderDao.selectByOrderId(orderId);
            orderEntity = new OrderEntity();
            orderEntity.setUserName(userOrderPO.getUserName());
            orderEntity.setUserId(userOrderPO.getUserId());
            orderEntity.setUserMobile(userOrderPO.getUserMobile());
            orderEntity.setSku(userOrderPO.getSku());
            orderEntity.setSkuName(userOrderPO.getSkuName());
            orderEntity.setOrderId(userOrderPO.getOrderId());
            orderEntity.setQuantity(userOrderPO.getQuantity());
            orderEntity.setUnitPrice(userOrderPO.getUnitPrice());
            orderEntity.setDiscountAmount(userOrderPO.getDiscountAmount());
            orderEntity.setTax(userOrderPO.getTax());
            orderEntity.setTotalAmount(userOrderPO.getTotalAmount());
            orderEntity.setOrderDate(userOrderPO.getOrderDate());
            orderEntity.setOrderStatus(userOrderPO.getOrderStatus());
            orderEntity.setUuid(userOrderPO.getUuid());
            orderEntity.setDeviceVO(JSON.parseObject(userOrderPO.getExtData(), DeviceVO.class));
            // 设置到缓存
            redissonService.setValue(orderId, orderEntity);
        }
        return orderEntity;
    }

    /**
     * 一般我们可以开户、下单、支付等个人场景做分布式加锁处理。虽然我们有数据库幂等的仿重，但对于个人用户非竞争下，为了避免重复的操作。可以使用加锁来降低对数据库的资源占用。
     */
    @Override
    public String payOrder(String orderId) {
        RLock lock = redissonService.getLock("pay_order_lock_".concat(orderId));
        try {
            lock.lock();
            userOrderDao.updateOrderStatusByOrderId(orderId);
        } finally {
            lock.unlock();
        }
        return orderId;
    }

}
