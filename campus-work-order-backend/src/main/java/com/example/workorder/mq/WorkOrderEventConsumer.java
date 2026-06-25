package com.example.workorder.mq;

import com.example.workorder.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkOrderEventConsumer {

    // 当前消费者用于异步审计输出；后续可在这里接入站内信、短信或数据分析。
    @RabbitListener(queues = RabbitMqConfig.WORK_ORDER_QUEUE)
    public void consume(WorkOrderChangedEvent event) {
        log.info("Consumed work-order event: orderId={}, operatorId={}, operation={}, status={}->{}, occurredAt={}",
                event.orderId(), event.operatorId(), event.operation(), event.beforeStatus(),
                event.afterStatus(), event.occurredAt());
    }
}
