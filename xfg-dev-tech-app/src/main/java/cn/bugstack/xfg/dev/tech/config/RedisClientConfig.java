package cn.bugstack.xfg.dev.tech.config;

import cn.bugstack.xfg.dev.tech.infrastructure.trigger.mq.RedisTopicListener01;
import cn.bugstack.xfg.dev.tech.types.RedisTopic;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description Redis 客户端，使用 Redisson <a href="https://github.com/redisson/redisson">Redisson</a>
 * @create 2023-09-09 16:51
 */
@Configuration
@EnableConfigurationProperties(RedisClientConfigProperties.class)
public class RedisClientConfig {

    @Bean("redissonClient")
    public RedissonClient redissonClient(ConfigurableApplicationContext applicationContext, RedisClientConfigProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        // config.setCodec(new RedisCodec());

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;

        RedissonClient redissonClient = Redisson.create(config);

        String[] beanNamesForType = applicationContext.getBeanNamesForType(MessageListener.class);
        for (String beanName : beanNamesForType) {
            MessageListener bean = applicationContext.getBean(beanName, MessageListener.class);

            Class<? extends MessageListener> beanClass = bean.getClass();

            if (beanClass.isAnnotationPresent(RedisTopic.class)) {
                RedisTopic redisTopic = beanClass.getAnnotation(RedisTopic.class);

                RTopic topic = redissonClient.getTopic(redisTopic.topic());
                topic.addListener(String.class, bean);

                ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
                beanFactory.registerSingleton(redisTopic.topic(), topic);
            }
        }

        return redissonClient;
    }

    /**
     * 手动配置
     */
    @Bean("testRedisTopic")
    public RTopic testRedisTopicListener(RedissonClient redissonClient, RedisTopicListener01 redisTopicListener) {
        RTopic topic = redissonClient.getTopic("xfg-dev-tech-topic");
        topic.addListener(String.class, redisTopicListener);
        return topic;
    }

}
