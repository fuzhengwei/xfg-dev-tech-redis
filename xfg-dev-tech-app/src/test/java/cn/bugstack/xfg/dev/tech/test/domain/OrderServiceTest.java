package cn.bugstack.xfg.dev.tech.test.domain;

import cn.bugstack.xfg.dev.tech.domain.order.model.aggregate.OrderAggregate;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.OrderEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.SKUEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.entity.UserEntity;
import cn.bugstack.xfg.dev.tech.domain.order.model.valobj.DeviceVO;
import cn.bugstack.xfg.dev.tech.domain.order.service.IOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTest {

    @Resource
    private IOrderService orderService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private final AtomicLong totalExecutionTime = new AtomicLong(); // 记录总耗时

    @Test
    public void test_createOrder() throws InterruptedException {
        String sku = RandomStringUtils.randomNumeric(9);
        int count = 1;

        orderService.initSkuCount(sku, count);

        for (int i = 0; i < count; i++) {
            threadPoolExecutor.execute(() -> {
                UserEntity userEntity = UserEntity.builder()
                        .userId("小傅哥")
                        .userName("xfg".concat(RandomStringUtils.randomNumeric(3)))
                        .userMobile("+86 13521408***")
                        .build();

                SKUEntity skuEntity = SKUEntity.builder()
                        .sku(sku)
                        .skuName("《手写MyBatis：渐进式源码实践》")
                        .quantity(1)
                        .unitPrice(BigDecimal.valueOf(128))
                        .discountAmount(BigDecimal.valueOf(50))
                        .tax(BigDecimal.ZERO)
                        .totalAmount(BigDecimal.valueOf(78))
                        .build();

                DeviceVO deviceVO = DeviceVO.builder()
                        .ipv4("127.0.0.1")
                        .ipv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334".getBytes())
                        .machine("IPhone 14 Pro")
                        .location("shanghai")
                        .build();

                long threadBeginTime = System.currentTimeMillis(); // 记录线程开始时间

                // 耗时:4毫秒
//                 String orderId = orderService.createOrder(new OrderAggregate(userEntity, skuEntity, deviceVO));
                // 耗时:106毫秒
//                String orderId = orderService.createOrderByLock(new OrderAggregate(userEntity, skuEntity, deviceVO));
                // 耗时:4毫秒
                String orderId = orderService.createOrderByNoLock(new OrderAggregate(userEntity, skuEntity, deviceVO));
                long took = System.currentTimeMillis() - threadBeginTime;
                totalExecutionTime.addAndGet(took); // 累加线程耗时
                log.info("写入完成 {} 耗时 {} (ms)", orderId, took / 1000);
            });
        }

        new Thread(() -> {
            while (true) {
                if (threadPoolExecutor.getActiveCount() == 0) {
                    log.info("执行完毕，总耗时：{} (ms)", (totalExecutionTime.get() / 1000));
//                    log.info("执行完毕，总耗时:{}", "\r\033[31m" + (totalExecutionTime.get() / 1000) + "\033[0m");
                    break;
                }
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        // 等待
        new CountDownLatch(1).await();

    }

    @Test
    public void test_queryOrder() {
        OrderEntity orderEntity = orderService.queryOrder("60711088280");
        log.info("测试结果：{}", JSON.toJSONString(orderEntity));
    }

}
