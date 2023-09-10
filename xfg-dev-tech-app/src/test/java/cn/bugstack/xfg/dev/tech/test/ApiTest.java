package cn.bugstack.xfg.dev.tech.test;

import cn.bugstack.xfg.dev.tech.infrastructure.persistent.dao.IUserOrderDao;
import cn.bugstack.xfg.dev.tech.infrastructure.persistent.po.UserOrderPO;
import cn.bugstack.xfg.dev.tech.infrastructure.redis.IRedisService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 单元测试
 * @create 2023-09-03 14:08
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.print("\r\033[31m" + i + "\033[0m");
            try {
                Thread.sleep(1000); // 暂停1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Resource
    private IUserOrderDao userOrderDao;

    @Resource
    private IRedisService redissonService;

    @Test
    public void test_redissonClient() {
        redissonService.setValue("xfg", "test123");
        String xfg = redissonService.getValue("xfg");
        log.info("测试结果:{}", xfg);
    }


    @Test
    public void test_select() {
        UserOrderPO userOrderPO = userOrderDao.selectByOrderId("ORD001");
        log.info("测试结果: {}", JSON.toJSONString(userOrderPO));
    }

    @Test
    public void test_insert() {
        UserOrderPO userOrderPO = UserOrderPO.builder()
                .userName("小傅哥")
                .userId("xfg".concat(RandomStringUtils.randomNumeric(3)))
                .userMobile("+86 13521408***")
                .sku("13811216")
                .skuName("《手写MyBatis：渐进式源码实践》")
                .orderId(RandomStringUtils.randomNumeric(11))
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(128))
                .discountAmount(BigDecimal.valueOf(50))
                .tax(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(78))
                .orderDate(new Date())
                .orderStatus(0)
                .isDelete(0)
                .uuid(UUID.randomUUID().toString().replace("-", ""))
                .ipv4("127.0.0.1")
                .ipv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334".getBytes())
                .extData("{\"device\": {\"machine\": \"IPhone 14 Pro\", \"location\": \"shanghai\"}}")
                .build();

        userOrderDao.insert(userOrderPO);
    }

}
