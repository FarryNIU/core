package com.bamboo.core.mq;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RabbitmqConsumer {

    @RabbitListener(queues = "${bamboo.rabbitmq.queue}")
    public void handleMessage(String messageBody, Channel channel, Message message) throws IOException {
        // 1. 处理消息内容，例如打印
        System.out.println(" [x] 收到消息: '" + messageBody + "'");

        // 2. 业务处理
        boolean success = processMessage(messageBody);

        // 3. 手动进行消息确认 (可选，需配置为手动模式)
        if (success) {
            // 成功处理，确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            // 处理失败，拒绝消息并重新入队（或丢弃）
            // requeue参数为true时消息会重新放回队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    private boolean processMessage(String body) {
        // 你的业务逻辑
        return true;
    }
}