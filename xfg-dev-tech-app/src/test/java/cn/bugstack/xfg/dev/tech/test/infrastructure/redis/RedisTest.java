package cn.bugstack.xfg.dev.tech.test.infrastructure.redis;

import cn.bugstack.xfg.dev.tech.domain.order.model.entity.OrderEntity;
import cn.bugstack.xfg.dev.tech.infrastructure.redis.IRedisService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.RedissonMultiLock;
import org.redisson.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {

    @Resource
    private IRedisService redissonService;

    @Test
    public void test_getValue() {
        OrderEntity value = redissonService.getValue("60711088280");
        log.info("测试结果:{}", JSON.toJSONString(value));
    }

    @Test
    public void test_remove() {
        redissonService.remove("60711088280");
    }

    /**
     * 可重入锁，加锁和解锁。Redisson的分布式可重入锁RLock Java对象实现了java.util.concurrent.locks.Lock接口，同时还支持自动过期解锁。
     * lock.lock();
     * lock.lock(10, TimeUnit.SECONDS);
     * lock.tryLock(3, 10, TimeUnit.SECONDS);
     * lock.lockAsync();
     * lock.lockAsync(10, TimeUnit.SECONDS);
     * Future<Boolean> res = lock.tryLockAsync(3, 10, TimeUnit.SECONDS);
     */
    @Test
    public void test_ReentrantLock() throws Exception {
        RLock lock = redissonService.getLock("");
        try {
            // 1. 最常见的使用方法
            lock.lock();

        } finally {
            lock.unlock();
        }
    }

    /**
     * 公平锁；保证了当多个Redisson客户端线程同时请求加锁时，优先分配给先发出请求的线程。
     * fairLock.lock();
     * fairLock.lock(10, TimeUnit.SECONDS); - 自动解锁，无需unlock方法手动解锁
     * fairLock.tryLock(100, 10, TimeUnit.SECONDS); - 尝试加锁，waitTime = 等待时间、leaseTime = 过期自动解锁。根据拿到锁的结果做业务
     * fairLock.lockAsync(); - 异步加锁
     * fairLock.lockAsync(10, TimeUnit.SECONDS); - 异步加锁，自动解锁
     * Future<Boolean> res = fairLock.tryLockAsync(3, 10, TimeUnit.SECONDS); - 异步加锁，尝试加锁，自动解锁
     */
    @Test
    public void test_fairLock() throws InterruptedException {
        RLock fairLock = redissonService.getFairLock("");
        try {
            // 1. 普通加锁
            fairLock.lock();

            // 2. 自动解锁，无需unlock方法手动解锁
            fairLock.lock(10, TimeUnit.SECONDS);

            // 3. 尝试加锁，waitTime = 等待时间、leaseTime = 过期自动解锁
            boolean res = fairLock.tryLock(100, 10, TimeUnit.SECONDS);
        } finally {
            fairLock.unlock();
        }
    }

    /**
     * RedissonMultiLock、RedissonRedLock 都可以将不同实例的多个 RLock 对象关联为一个联锁
     */
    @Test
    public void test_multiLock() throws InterruptedException {
        // redissonService，可以是3个不同的客户端实例，如；redissonService01、redissonService02、redissonService03
        RLock lock1 = redissonService.getLock("lock1");
        RLock lock2 = redissonService.getLock("lock2");
        RLock lock3 = redissonService.getLock("lock3");

        RedissonMultiLock multiLock = new RedissonMultiLock(lock1, lock2, lock3);

        // 联锁
        try {
            multiLock.lock();

            boolean res = multiLock.tryLock(100, 10, TimeUnit.SECONDS);
        } finally {
            multiLock.unlock();
        }

        // 红锁
        RedissonMultiLock redLock = new RedissonMultiLock(lock1, lock2, lock3);
        try {
            redLock.lock();

            boolean res = redLock.tryLock(100, 10, TimeUnit.SECONDS);
        } finally {
            redLock.unlock();
        }
    }

    /**
     * 读写锁
     */
    @Test
    public void test_readWriteLock() throws InterruptedException {
        RReadWriteLock lock = redissonService.getReadWriteLock("");

        lock.readLock().lock();
        lock.writeLock().lock();

        lock.readLock().lock(10, TimeUnit.SECONDS);
        lock.writeLock().lock(10, TimeUnit.SECONDS);

        lock.readLock().tryLock(100, 10, TimeUnit.SECONDS);
        lock.writeLock().tryLock(100, 10, TimeUnit.SECONDS);

        lock.writeLock().unlock();
        lock.readLock().unlock();
    }

    /**
     * 信号量
     */
    @Test
    public void test_semaphore() throws InterruptedException {
        RSemaphore rSemaphore = redissonService.getSemaphore("");

        rSemaphore.acquire();
        rSemaphore.acquire(10);
        rSemaphore.tryAcquire();
        rSemaphore.tryAcquire(10);
        rSemaphore.tryAcquire(100, 10, TimeUnit.SECONDS);

        rSemaphore.tryAcquireAsync();
        rSemaphore.release();
        rSemaphore.release(10);

        rSemaphore.releaseAsync(10);
    }

    @Test
    public void test_getPermitExpirableSemaphore() throws InterruptedException {
        RPermitExpirableSemaphore semaphore = redissonService.getPermitExpirableSemaphore("");
        String acquireId = semaphore.acquire();
        semaphore.acquire(10);
        semaphore.tryAcquire();
        semaphore.tryAcquire(10);
        semaphore.tryAcquire(100, 10, TimeUnit.SECONDS);

        semaphore.tryAcquireAsync();

        semaphore.release(acquireId);
    }

    @Test
    public void test_getCountDownLatch() throws InterruptedException {
        RCountDownLatch latch = redissonService.getCountDownLatch("");
        latch.trySetCount(1);
        latch.await();
    }

    @Test
    public void test_getBloomFilter() {
        // 创建一个布隆过滤器，使用默认的误判率和预计元素数量
        RBloomFilter<String> bloomFilter = redissonService.getBloomFilter("xfg-dev-tech-bloom");
        // 初始化布隆过滤器，设置预计元素数量为10000，误判率为0.03
        bloomFilter.tryInit(10000, 0.03);
        // 添加元素到布隆过滤器
        bloomFilter.add("1");
        bloomFilter.add("2");
        // 验证元素是否存在
        log.info("测试结果 {}", bloomFilter.contains("1"));
        log.info("测试结果 {}", bloomFilter.contains("3"));
    }

    @Test
    public void test_getQueue() throws InterruptedException {
        RQueue<String> queue = redissonService.getQueue("xfg-dev-tech-queue");

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                queue.add(RandomStringUtils.randomNumeric(4));
            }
        }).start();

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("测试结果 {}", queue.poll());
            }
        }).start();

        // 等待
        new CountDownLatch(1).await();
    }

    /**
     * 延迟队列场景应用；https://mp.weixin.qq.com/s/jJ0vxdeKXHiYZLrwDEBOcQ
     */
    @Test
    public void test_getDelayedQueue() throws InterruptedException {
        RBlockingQueue<Object> blockingQueue = redissonService.getBlockingQueue("xfg-dev-tech-task");
        RDelayedQueue<Object> delayedQueue = redissonService.getDelayedQueue(blockingQueue);

        new Thread(() -> {
            try {
                while (true){
                    Object take = blockingQueue.take();
                    log.info("测试结果 {}", take);
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        int i = 0;
        while (true){
            delayedQueue.offerAsync("测试" + ++i, 100L, TimeUnit.MILLISECONDS);
            Thread.sleep(1000L);
        }

    }

}
