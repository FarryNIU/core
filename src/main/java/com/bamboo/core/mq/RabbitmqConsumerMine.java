package com.bamboo.core.mq;

import com.rabbitmq.client.*;

/**
 * 全手动实现
 */
public class RabbitmqConsumerMine {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {
        // 1. 创建连接工厂并配置
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ服务器地址
        factory.setUsername("guest"); // 默认用户名
        factory.setPassword("guest"); // 默认密码

        // 2. 建立连接和通道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            
            // 3. 声明队列（确保队列存在）
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] 等待接收消息。按 CTRL+C 退出");

            // 4. 定义消息接收后的处理回调
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] 收到消息：'" + message + "'");
                // 此处可添加业务处理逻辑
            };

            // 5. 定义消费被取消时的回调
            CancelCallback cancelCallback = consumerTag -> {
                System.out.println(" [!] 消息消费被中断，Consumer Tag: " + consumerTag);
            };

            // 6. 开始消费消息
            // 参数说明：队列名，是否自动确认，消息交付回调，取消回调
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback);
            
            // 保持主线程运行，持续监听
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}