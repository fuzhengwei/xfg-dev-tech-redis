package cn.bugstack.xfg.dev.tech.infrastructure.trigger.mq;

import cn.bugstack.xfg.dev.tech.types.RedisTopic;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RedisTopic(topic = "testRedisTopic02")
public class RedisTopicListener02 implements MessageListener<String> {

    @Override
    public void onMessage(CharSequence channel, String msg) {
        log.info("02-监听消息(Redis 发布/订阅): {}", msg);
    }

}
