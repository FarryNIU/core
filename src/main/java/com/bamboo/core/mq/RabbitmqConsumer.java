package com.bamboo.core.mq;

import com.bamboo.core.mq.bean.BookRequest;
import com.bamboo.core.service.BookService;
import com.bamboo.core.util.JsonConvertor;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class RabbitmqConsumer {
    @Autowired
    private BookService bookService;

    @RabbitListener(queues = "${bamboo.rabbitmq.queue}")
    public void handleMessage(String messageBody, Channel channel, Message message) throws IOException {
        log.info("mq收到消息: '" + messageBody + "'");
        BookRequest bookRequest = JsonConvertor.convertJsonToObject(messageBody, BookRequest.class);
        boolean success = processMessage(bookRequest);
        if (success) {
            // 成功处理，确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } else {
            // 处理失败，拒绝消息并丢弃
            // requeue参数为true时消息会重新放回队列
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    private boolean processMessage(BookRequest bookRequest) {
        return bookService.book(bookRequest);
    }
}