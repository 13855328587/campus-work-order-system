package com.example.workorder.mq;

import com.example.workorder.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    // 只在数据库事务提交成功后发送，避免回滚工单产生无效消息。
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(WorkOrderChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.WORK_ORDER_EXCHANGE,
                    RabbitMqConfig.WORK_ORDER_ROUTING_KEY,
                    event
            );
            log.info("Published work-order event: orderId={}, operation={}", event.orderId(), event.operation());
        } catch (AmqpException exception) {
            // 消息队列属于异步扩展，连接异常不能推翻已成功提交的工单事务。
            log.error("Failed to publish work-order event: orderId={}, operation={}",
                    event.orderId(), event.operation(), exception);
        }
    }
}
